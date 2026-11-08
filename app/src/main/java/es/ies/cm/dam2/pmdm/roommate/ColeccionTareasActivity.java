package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Muestra las tareas guardadas en SQLite.
 * Permite ver el detalle, cambiar el estado y usar un menu contextual
 * para editar o borrar una tarea.
 */
public class ColeccionTareasActivity extends AppCompatActivity {

    public static final String EXTRA_USUARIO_ID = "extra_usuario_id";
    public static final String EXTRA_USUARIO_NOMBRE = "extra_usuario_nombre";

    private final List<Tarea> todasLasTareas = new ArrayList<>();
    private final List<Tarea> tareasVisibles = new ArrayList<>();
    private boolean mostrarHechas = false;

    private TareaRecyclerAdapter adaptador;
    private RecyclerView rvTareas;
    private Button btnMostrar;
    private ActivityResultLauncher<Intent> launcher;
    private RoomMateDB db;

    private int usuarioId = 1;
    private String usuarioNombre = "admin";

    private ActionMode actionMode;
    private Tarea tareaSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_coleccion_tareas);
        aplicarInsets();

        db = new RoomMateDB(this);

        usuarioId = getIntent().getIntExtra(EXTRA_USUARIO_ID, 1);
        usuarioNombre = getIntent().getStringExtra(EXTRA_USUARIO_NOMBRE);
        if (usuarioNombre == null || usuarioNombre.isEmpty()) {
            usuarioNombre = "admin";
        }

        rvTareas = findViewById(R.id.rvTareas);
        Button btnAnadir = findViewById(R.id.btnAnadirTarea);
        btnMostrar = findViewById(R.id.btnMostrarTareas);
        Button btnVolver = findViewById(R.id.btnVolverColeccion);
        Button btnBuscar = findViewById(R.id.btnBuscarTareas);
        Button btnCompra = findViewById(R.id.btnListaCompra);

        TextView tvSubtitulo = findViewById(R.id.tvSubtituloColeccion);
        tvSubtitulo.setText(
                "Usuario: " + usuarioNombre
                        + ". Pulsa una tarea para ver su detalle. "
                        + "Mantén pulsada para editar o borrar."
        );

        rvTareas.setLayoutManager(new LinearLayoutManager(this));

        adaptador = new TareaRecyclerAdapter(
                tareasVisibles,
                this::abrirDetalleTarea,
                this::alCambiarEstado,
                this::iniciarModoContextual
        );
        rvTareas.setAdapter(adaptador);

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK
                            && result.getData() != null) {

                        Tarea tarea = (Tarea) result.getData()
                                .getSerializableExtra(
                                        AnadirTareasActivity.EXTRA_TAREA
                                );

                        if (tarea != null) {
                            // Si la tarea guardada no se ve con el filtro actual,
                            // se cambia al estado que corresponde.
                            if (tarea.isHecha() != mostrarHechas) {
                                mostrarHechas = tarea.isHecha();
                                actualizarBotonMostrar();
                            }

                            cargarTareasDesdeBD();

                            Toast.makeText(
                                    this,
                                    R.string.tarea_guardada,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                }
        );

        actualizarBotonMostrar();

        btnMostrar.setOnClickListener(v -> {
            mostrarHechas = !mostrarHechas;
            actualizarBotonMostrar();
            aplicarFiltro();

            String mensaje = mostrarHechas
                    ? "Mostrando tareas hechas"
                    : "Mostrando tareas no hechas";

            if (tareasVisibles.isEmpty()) {
                mensaje = mostrarHechas
                        ? "No hay tareas hechas"
                        : "No hay tareas no hechas";
            }

            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        });

        btnAnadir.setOnClickListener(v ->
                launcher.launch(
                        new Intent(this, AnadirTareasActivity.class)
                ));

        btnBuscar.setOnClickListener(v -> {
            Intent intent = new Intent(this, BuscarActivity.class);
            intent.putExtra(EXTRA_USUARIO_ID, usuarioId);
            intent.putExtra(EXTRA_USUARIO_NOMBRE, usuarioNombre);
            startActivity(intent);
        });

        btnCompra.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListaCompraActivity.class);
            intent.putExtra(EXTRA_USUARIO_ID, usuarioId);
            intent.putExtra(EXTRA_USUARIO_NOMBRE, usuarioNombre);
            startActivity(intent);
        });

        btnVolver.setOnClickListener(v -> finish());

        // Carga las tareas que ya estan guardadas en la base de datos.
        cargarTareasDesdeBD();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Al volver de otra pantalla se refresca la lista.
        cargarTareasDesdeBD();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

    private void cargarTareasDesdeBD() {
        todasLasTareas.clear();
        todasLasTareas.addAll(db.obtenerTareas());
        aplicarFiltro();
    }

    private void actualizarBotonMostrar() {
        btnMostrar.setText(
                mostrarHechas ? "Ver no hechas" : "Ver hechas"
        );
    }

    private void abrirDetalleTarea(Tarea tarea) {
        Intent intent = new Intent(this, DetalleTareaActivity.class);
        intent.putExtra(DetalleTareaActivity.EXTRA_TAREA, tarea);
        startActivity(intent);
    }

    private void iniciarModoContextual(Tarea tarea) {
        if (actionMode != null) {
            return;
        }

        tareaSeleccionada = tarea;

        actionMode = startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(
                        R.menu.menu_tarea_contextual,
                        menu
                );
                mode.setTitle(tarea.getNombre());
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.action_editar_tarea) {
                    abrirEdicionTarea(tareaSeleccionada);
                    mode.finish();
                    return true;
                }

                if (id == R.id.action_borrar_tarea) {
                    borrarTarea(tareaSeleccionada);
                    mode.finish();
                    return true;
                }

                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;
                tareaSeleccionada = null;
            }
        });
    }

    private void abrirEdicionTarea(Tarea tarea) {
        Intent intent = new Intent(this, AnadirTareasActivity.class);
        intent.putExtra(AnadirTareasActivity.EXTRA_TAREA_EDITAR, tarea);
        launcher.launch(intent);
    }

    private void borrarTarea(Tarea tarea) {
        db.borrarTarea(tarea.getId());
        cargarTareasDesdeBD();

        Toast.makeText(
                this,
                R.string.tarea_borrada,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void alCambiarEstado(Tarea tarea) {
        db.actualizarHecha(tarea.getId(), tarea.isHecha());
        cargarTareasDesdeBD();

        Toast.makeText(
                this,
                tarea.isHecha()
                        ? R.string.tarea_marcada_hecha
                        : R.string.tarea_marcada_no_hecha,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void aplicarFiltro() {
        tareasVisibles.clear();

        for (Tarea t : todasLasTareas) {
            if (t.isHecha() == mostrarHechas) {
                tareasVisibles.add(t);
            }
        }

        if (adaptador != null) {
            adaptador.notifyDataSetChanged();
        }
    }

    private void aplicarInsets() {
        View root = findViewById(R.id.main);
        int base = (int) (24 * getResources()
                .getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );
            v.setPadding(
                    base + bars.left,
                    base + bars.top,
                    base + bars.right,
                    base + bars.bottom
            );
            return insets;
        });
    }
}
