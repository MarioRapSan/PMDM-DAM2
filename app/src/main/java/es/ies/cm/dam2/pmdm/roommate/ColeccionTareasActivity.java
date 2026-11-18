package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pantalla principal de la app: listado de tareas (se llega tras el login).
 *
 * - Menu lateral (Navigation Drawer) con las secciones: Tareas, Lista de la
 *   compra, Gastos compartidos, Companeros, Ajustes, tema, Acerca de y cerrar sesion.
 *   Todas se abren desde aqui y vuelven aqui. El boton Atras cierra el menu
 *   si esta abierto y, si no, sale de la app.
 * - RecyclerView con CardView (TareaRecyclerAdapter).
 * - Clic corto: abre la ficha. Clic largo: menu contextual Editar / Eliminar.
 * - Menu de opciones: Anadir, Buscar, Lista de la compra, Ordenar y Acerca de.
 * - ProgressBar con el porcentaje de tareas hechas.
 * Las tareas se leen de Room en un hilo secundario y se recargan en onResume,
 * asi al volver de otra pantalla nunca se ven datos obsoletos.
 */
public class ColeccionTareasActivity extends AppCompatActivity {

    public static final String EXTRA_USUARIO_ID = "extra_usuario_id";
    public static final String EXTRA_USUARIO_NOMBRE = "extra_usuario_nombre";

    /** Claves para conservar el estado al girar la pantalla. */
    private static final String ESTADO_MOSTRAR_HECHAS = "mostrar_hechas";
    private static final String ESTADO_ORDEN = "orden";

    /** Clave del dialogo de confirmacion de borrado. */
    private static final String CLAVE_CONFIRMAR_BORRADO = "confirmar_borrado_tarea";

    /** Criterios de ordenacion del listado. */
    private static final int ORDEN_ALTA = 0;
    private static final int ORDEN_NOMBRE = 1;
    private static final int ORDEN_DIFICULTAD = 2;

    private final List<Tarea> todasLasTareas = new ArrayList<>();
    private final List<Tarea> tareasVisibles = new ArrayList<>();
    private boolean mostrarHechas = false;
    private int orden = ORDEN_ALTA;

    /** Tarea pendiente de confirmar su borrado. */
    private Tarea tareaABorrar;

    private TareaRecyclerAdapter adaptador;
    private Button btnMostrar;
    private ProgressBar pbProgreso;
    private TextView tvProgreso;
    private ActivityResultLauncher<Intent> launcher;
    private TareaDao tareaDao;

    private int usuarioId;
    private String usuarioNombre;

    private DrawerLayout drawerLayout;

    /** Comprueba el usuario, prepara la lista, los menus y los dialogos, y carga las tareas. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sin usuario identificado no se puede entrar: se vuelve al login.
        usuarioId = getIntent().getIntExtra(EXTRA_USUARIO_ID, -1);
        usuarioNombre = getIntent().getStringExtra(EXTRA_USUARIO_NOMBRE);
        if (usuarioId == -1 || usuarioNombre == null) {
            AjustesActivity.irAlLogin(this);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_coleccion_tareas);
        aplicarInsets();

        // Recupera el filtro y el orden si la pantalla se ha girado.
        if (savedInstanceState != null) {
            mostrarHechas = savedInstanceState.getBoolean(ESTADO_MOSTRAR_HECHAS);
            orden = savedInstanceState.getInt(ESTADO_ORDEN);
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbarColeccion);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.coleccion_de_tareas);
        }

        // Menu lateral: el icono de la barra superior lo abre y lo cierra.
        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.abrir_menu, R.string.cerrar_menu);
        toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, R.color.on_primary));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
        navigationView.setCheckedItem(R.id.nav_tareas);

        // La cabecera del menu lateral saluda al usuario que ha entrado.
        TextView tvNavUsuario = navigationView.getHeaderView(0).findViewById(R.id.tvNavUsuario);
        tvNavUsuario.setText(getString(R.string.hola_usuario, usuarioNombre));

        // Atras: si el menu lateral esta abierto se cierra; si no, se sale de la app.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        tareaDao = AppDatabase.getInstance(this).tareaDao();
        // Primera vez: migra datos antiguos y crea la carga inicial si hace falta.
        AppDatabase.inicializar(this, () -> {
            if (!isFinishing() && !isDestroyed()) {
                cargarTareasDesdeBD();
            }
        });

        RecyclerView rvTareas = findViewById(R.id.rvTareas);
        Button btnAnadir = findViewById(R.id.btnAnadirTarea);
        btnMostrar = findViewById(R.id.btnMostrarTareas);
        pbProgreso = findViewById(R.id.pbProgresoTareas);
        tvProgreso = findViewById(R.id.tvProgresoTareas);

        TextView tvSubtitulo = findViewById(R.id.tvSubtituloColeccion);
        tvSubtitulo.setText(getString(R.string.subtitulo_coleccion, usuarioNombre));

        rvTareas.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new TareaRecyclerAdapter(
                tareasVisibles,
                this::abrirDetalleTarea,
                this::alCambiarEstado,
                this::mostrarMenuContextual
        );
        rvTareas.setAdapter(adaptador);

        // Vuelta del formulario de alta / edicion.
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Tarea tarea = (Tarea) result.getData()
                                .getSerializableExtra(AnadirTareasActivity.EXTRA_TAREA);
                        if (tarea != null) {
                            // Si la tarea guardada no se ve con el filtro actual,
                            // se cambia al filtro que le corresponde.
                            if (tarea.isHecha() != mostrarHechas) {
                                mostrarHechas = tarea.isHecha();
                                actualizarBotonMostrar();
                            }
                            cargarTareasDesdeBD();
                            Toast.makeText(this, R.string.tarea_guardada, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Respuesta del DialogFragment de confirmacion de borrado.
        getSupportFragmentManager().setFragmentResultListener(
                CLAVE_CONFIRMAR_BORRADO, this, (clave, resultado) -> {
                    if (resultado.getBoolean(ConfirmacionDialogFragment.RESULTADO_CONFIRMADO)
                            && tareaABorrar != null) {
                        borrarTarea(tareaABorrar);
                    }
                    tareaABorrar = null;
                });

        actualizarBotonMostrar();

        btnMostrar.setOnClickListener(v -> {
            mostrarHechas = !mostrarHechas;
            actualizarBotonMostrar();
            aplicarFiltro();

            int mensaje;
            if (tareasVisibles.isEmpty()) {
                mensaje = mostrarHechas ? R.string.no_hay_hechas : R.string.no_hay_pendientes;
            } else {
                mensaje = mostrarHechas ? R.string.mostrando_hechas : R.string.mostrando_pendientes;
            }
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        });

        btnAnadir.setOnClickListener(v -> abrirAlta());
    }

    /** Guarda el filtro y el orden para recuperarlos al girar la pantalla. */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ESTADO_MOSTRAR_HECHAS, mostrarHechas);
        outState.putInt(ESTADO_ORDEN, orden);
    }

    /** Recarga la lista cada vez que se vuelve a esta pantalla. */
    @Override
    protected void onResume() {
        super.onResume();
        // Al volver de otra pantalla (ficha, edicion, busqueda...) se refresca la lista.
        cargarTareasDesdeBD();
    }

    /** Carga el menu de opciones de la barra superior. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_coleccion_tareas, menu);
        return true;
    }

    /** Acciones del menu de opciones: anadir, buscar, lista de la compra, ordenar y acerca de. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_anadir_tarea) {
            abrirAlta();
            return true;
        }
        if (id == R.id.action_buscar_tareas) {
            abrirConUsuario(BuscarActivity.class);
            return true;
        }
        if (id == R.id.action_lista_compra) {
            abrirConUsuario(ListaCompraActivity.class);
            return true;
        }
        if (id == R.id.action_ordenar_nombre) {
            ordenar(ORDEN_NOMBRE, R.string.tareas_ordenadas);
            return true;
        }
        if (id == R.id.action_ordenar_dificultad) {
            ordenar(ORDEN_DIFICULTAD, R.string.tareas_ordenadas_dificultad);
            return true;
        }
        if (id == R.id.action_acerca_de) {
            startActivity(new Intent(this, AcercaDeActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Cambia el criterio de orden, refresca la lista y lo avisa con un Toast. */
    private void ordenar(int nuevoOrden, int mensaje) {
        orden = nuevoOrden;
        aplicarFiltro();
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    /** Abre la seccion elegida en el menu lateral (Navigation Drawer) y lo cierra. */
    private boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        drawerLayout.closeDrawer(GravityCompat.START);

        if (id == R.id.nav_lista_compra) {
            abrirConUsuario(ListaCompraActivity.class);
        } else if (id == R.id.nav_gastos_compartidos) {
            // Pantalla todavia sin construir: solo muestra "Proximamente".
            startActivity(new Intent(this, GastosCompartidosActivity.class));
        } else if (id == R.id.nav_companeros) {
            abrirConUsuario(CompanerosActivity.class);
        } else if (id == R.id.nav_ajustes) {
            abrirConUsuario(AjustesActivity.class);
        } else if (id == R.id.nav_cambiar_tema) {
            // Alterna claro/oscuro; la pantalla se recrea con la nueva paleta.
            Tema.alternar(this);
        } else if (id == R.id.nav_acerca_de) {
            startActivity(new Intent(this, AcercaDeActivity.class));
        } else if (id == R.id.nav_cerrar_sesion) {
            AjustesActivity.irAlLogin(this);
        }
        // "Tareas" es esta misma pantalla: solo se cierra el menu.
        return id == R.id.nav_tareas;
    }

    /** Abre otra pantalla pasando el id y el nombre del usuario. */
    private void abrirConUsuario(Class<?> pantalla) {
        Intent intent = new Intent(this, pantalla);
        intent.putExtra(EXTRA_USUARIO_ID, usuarioId);
        intent.putExtra(EXTRA_USUARIO_NOMBRE, usuarioNombre);
        startActivity(intent);
    }

    /** Abre el formulario vacio para crear una tarea nueva. */
    private void abrirAlta() {
        launcher.launch(new Intent(this, AnadirTareasActivity.class));
    }

    /** Lee todas las tareas de Room fuera del hilo principal. */
    private void cargarTareasDesdeBD() {
        AppDatabase.ejecutar(() -> {
            List<Tarea> tareas = tareaDao.obtenerTodas();
            runOnUiThread(() -> {
                todasLasTareas.clear();
                todasLasTareas.addAll(tareas);
                aplicarFiltro();
            });
        });
    }

    /** Cambia el texto del boton segun el filtro activo (hechas o pendientes). */
    private void actualizarBotonMostrar() {
        btnMostrar.setText(mostrarHechas ? R.string.ver_pendientes : R.string.ver_hechas);
    }

    /** Abre la ficha de la tarea pulsada (clic corto en la tarjeta). */
    private void abrirDetalleTarea(Tarea tarea) {
        Intent intent = new Intent(this, DetalleTareaActivity.class);
        intent.putExtra(DetalleTareaActivity.EXTRA_TAREA_ID, tarea.getId());
        startActivity(intent);
    }

    /** Menu contextual de cada tarjeta (clic largo): Editar / Eliminar. */
    private void mostrarMenuContextual(View vista, Tarea tarea) {
        PopupMenu menu = new PopupMenu(this, vista);
        menu.getMenuInflater().inflate(R.menu.menu_tarea_contextual, menu.getMenu());

        menu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_editar_tarea) {
                abrirEdicionTarea(tarea);
                return true;
            }
            if (id == R.id.action_eliminar_tarea) {
                tareaABorrar = tarea;
                ConfirmacionDialogFragment.nuevo(
                        CLAVE_CONFIRMAR_BORRADO,
                        R.string.confirmar_eliminar_tarea,
                        R.string.mensaje_confirmar_eliminar_tarea,
                        R.string.eliminar
                ).show(getSupportFragmentManager(), "dialogo_borrar_tarea");
                return true;
            }
            return false;
        });
        menu.show();
    }

    /** Abre el formulario con los datos de la tarea para editarla. */
    private void abrirEdicionTarea(Tarea tarea) {
        Intent intent = new Intent(this, AnadirTareasActivity.class);
        intent.putExtra(AnadirTareasActivity.EXTRA_TAREA_EDITAR, tarea);
        launcher.launch(intent);
    }

    /** Borra la tarea; sus registros se borran solos por el ON DELETE CASCADE. */
    private void borrarTarea(Tarea tarea) {
        AppDatabase.ejecutar(() -> {
            tareaDao.borrar(tarea);
            runOnUiThread(() -> {
                cargarTareasDesdeBD();
                Toast.makeText(this, R.string.tarea_eliminada, Toast.LENGTH_SHORT).show();
            });
        });
    }

    /** Se llama al marcar o desmarcar el CheckBox de una tarjeta. */
    private void alCambiarEstado(Tarea tarea) {
        AppDatabase.ejecutar(() -> {
            tareaDao.actualizar(tarea);
            runOnUiThread(() -> {
                cargarTareasDesdeBD();
                Toast.makeText(this,
                        tarea.isHecha() ? R.string.tarea_marcada_hecha : R.string.tarea_marcada_no_hecha,
                        Toast.LENGTH_SHORT).show();
            });
        });
    }

    /** Aplica el filtro hechas/pendientes y el orden elegido, y actualiza el progreso. */
    private void aplicarFiltro() {
        tareasVisibles.clear();
        int hechas = 0;

        for (Tarea t : todasLasTareas) {
            if (t.isHecha()) {
                hechas++;
            }
            if (t.isHecha() == mostrarHechas) {
                tareasVisibles.add(t);
            }
        }

        if (orden == ORDEN_NOMBRE) {
            tareasVisibles.sort((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()));
        } else if (orden == ORDEN_DIFICULTAD) {
            tareasVisibles.sort(Comparator.comparingInt(Tarea::getDificultad).reversed());
        }

        if (adaptador != null) {
            adaptador.notifyDataSetChanged();
        }

        int total = todasLasTareas.size();
        int porcentaje = total == 0 ? 0 : hechas * 100 / total;
        pbProgreso.setProgress(porcentaje);
        tvProgreso.setText(getString(R.string.progreso_tareas, hechas, total, porcentaje));
    }

    /** Anade al padding el espacio de las barras del sistema (modo edge-to-edge). */
    private void aplicarInsets() {
        View root = findViewById(R.id.main);
        int base = (int) (24 * getResources().getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(base + bars.left, base + bars.top,
                    base + bars.right, base + bars.bottom);
            return insets;
        });
    }
}
