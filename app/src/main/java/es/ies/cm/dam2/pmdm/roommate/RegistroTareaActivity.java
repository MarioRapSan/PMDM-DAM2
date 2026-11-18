package es.ies.cm.dam2.pmdm.roommate;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Registros de tareas: cada vez que alguien hace una tarea se guarda un registro
 * (fecha, puntos y quien la hizo). RegistroTarea es la entidad relacionada 1-N
 * de Tarea en Room, con ON DELETE CASCADE.
 *
 * - Spinners dependientes: el Spinner de companero filtra el Spinner de tareas.
 * - CRUD completo de registros: alta, consulta, edicion (clic) y borrado (clic largo).
 * - La fecha se elige con SelectorFechaDialogFragment.
 * - Se abre desde la ficha de una tarea, que llega ya seleccionada (EXTRA_TAREA_ID).
 */
public class RegistroTareaActivity extends AppCompatActivity {

    private static final String CLAVE_CONFIRMAR_BORRADO = "confirmar_borrado_registro";

    /** Extra opcional con la tarea que debe aparecer seleccionada al abrir. */
    public static final String EXTRA_TAREA_ID = "extra_tarea_id_registros";

    /** Tarea que hay que seleccionar cuando se carguen los Spinner (-1 si ninguna). */
    private int tareaPreseleccionadaId = -1;

    private TareaDao tareaDao;
    private RegistroTareaDao registroTareaDao;

    private Spinner spinnerCompanero;
    private Spinner spinnerTareas;
    private Spinner spinnerGanador;
    private TextView tvTotalRegistros;
    private TextView tvTotalGeneral;
    private TextView tvTituloForm;
    private EditText etFecha;
    private EditText etPuntuacion;
    private Button btnGuardar;
    private Button btnCancelarEdicion;

    /** Todas las tareas de la BD y las que pasan el filtro del companero. */
    private final List<Tarea> todasLasTareas = new ArrayList<>();
    private final List<Tarea> tareasFiltradas = new ArrayList<>();
    private final List<String> nombresTareas = new ArrayList<>();
    private final List<String> nombresCompaneros = new ArrayList<>();

    private final List<RegistroTarea> registros = new ArrayList<>();
    private final List<String> lineasRegistros = new ArrayList<>();

    private ArrayAdapter<String> adapterTareas;
    private ArrayAdapter<String> adapterRegistros;
    private ArrayAdapter<String> adapterCompaneroFiltro;
    private ArrayAdapter<String> adapterGanador;

    private int tareaSeleccionadaId = -1;

    /** Registro que se esta editando (null si se esta creando uno nuevo). */
    private RegistroTarea registroEnEdicion;
    /** Registro pendiente de confirmar su borrado. */
    private RegistroTarea registroABorrar;

    /** Prepara los Spinner dependientes, la lista de registros, el formulario y los dialogos. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_tarea);

        MaterialToolbar toolbar = findViewById(R.id.toolbarRegistros);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.registros_de_tareas);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Si se abre desde la ficha de una tarea, esa tarea sale ya seleccionada.
        if (savedInstanceState == null) {
            tareaPreseleccionadaId = getIntent().getIntExtra(EXTRA_TAREA_ID, -1);
        }

        tareaDao = AppDatabase.getInstance(this).tareaDao();
        registroTareaDao = AppDatabase.getInstance(this).registroTareaDao();

        spinnerCompanero = findViewById(R.id.spinnerCompaneroFiltro);
        spinnerTareas = findViewById(R.id.spinnerTareasRegistro);
        spinnerGanador = findViewById(R.id.spinnerGanador);
        tvTotalRegistros = findViewById(R.id.tvTotalRegistros);
        tvTotalGeneral = findViewById(R.id.tvTotalGeneral);
        tvTituloForm = findViewById(R.id.tvTituloFormRegistro);
        etFecha = findViewById(R.id.etFechaRegistro);
        etPuntuacion = findViewById(R.id.etPuntuacionRegistro);
        btnGuardar = findViewById(R.id.btnGuardarRegistro);
        btnCancelarEdicion = findViewById(R.id.btnCancelarEdicionRegistro);
        ListView lvRegistros = findViewById(R.id.lvRegistros);
        lvRegistros.setEmptyView(findViewById(R.id.tvSinRegistros));

        // Adaptadores de los Spinner y de la lista.
        adapterCompaneroFiltro = crearAdapterSpinner(new ArrayList<>());
        spinnerCompanero.setAdapter(adapterCompaneroFiltro);

        adapterTareas = crearAdapterSpinner(nombresTareas);
        spinnerTareas.setAdapter(adapterTareas);

        adapterGanador = crearAdapterSpinner(nombresCompaneros);
        spinnerGanador.setAdapter(adapterGanador);

        adapterRegistros = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, lineasRegistros);
        lvRegistros.setAdapter(adapterRegistros);

        // Spinner 1 -> Spinner 2: al cambiar de companero se filtran las tareas.
        spinnerCompanero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filtrarTareasPorCompanero();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Spinner 2 -> lista: al cambiar de tarea se cargan sus registros.
        spinnerTareas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < tareasFiltradas.size()) {
                    tareaSeleccionadaId = tareasFiltradas.get(position).getId();
                    // La tarea preseleccionada ya se ha mostrado: a partir de aqui manda el usuario.
                    if (tareaSeleccionadaId == tareaPreseleccionadaId) {
                        tareaPreseleccionadaId = -1;
                    }
                    cancelarEdicion();
                    cargarRegistros();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tareaSeleccionadaId = -1;
            }
        });

        // Clic: editar el registro. Clic largo: borrarlo (con confirmacion).
        lvRegistros.setOnItemClickListener((parent, view, position, id) ->
                empezarEdicion(registros.get(position)));
        lvRegistros.setOnItemLongClickListener((parent, view, position, id) -> {
            registroABorrar = registros.get(position);
            ConfirmacionDialogFragment.nuevo(
                    CLAVE_CONFIRMAR_BORRADO,
                    R.string.eliminar_registro,
                    R.string.mensaje_confirmar_eliminar_registro,
                    R.string.eliminar
            ).show(getSupportFragmentManager(), "dialogo_borrar_registro");
            return true;
        });

        getSupportFragmentManager().setFragmentResultListener(
                CLAVE_CONFIRMAR_BORRADO, this, (clave, resultado) -> {
                    if (resultado.getBoolean(ConfirmacionDialogFragment.RESULTADO_CONFIRMADO)
                            && registroABorrar != null) {
                        borrarRegistro(registroABorrar);
                    }
                    registroABorrar = null;
                });

        // Fecha con el DialogFragment propio.
        etFecha.setOnClickListener(v ->
                SelectorFechaDialogFragment.nuevo(etFecha.getText().toString().trim())
                        .show(getSupportFragmentManager(), "selector_fecha"));
        getSupportFragmentManager().setFragmentResultListener(
                SelectorFechaDialogFragment.REQUEST_KEY, this, (clave, resultado) -> {
                    etFecha.setText(resultado.getString(SelectorFechaDialogFragment.RESULTADO_FECHA));
                    etFecha.setError(null);
                });

        btnGuardar.setOnClickListener(v -> guardarRegistro());
        btnCancelarEdicion.setOnClickListener(v -> cancelarEdicion());

        cargarDatos();
    }

    /** Crea un adaptador de Spinner con el estilo de texto de la app. */
    private ArrayAdapter<String> crearAdapterSpinner(List<String> datos) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_texto, datos);
        adapter.setDropDownViewResource(R.layout.item_spinner_texto);
        return adapter;
    }

    /** La flecha de la barra superior cierra la pantalla y vuelve a la anterior. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Carga companeros (SQLite) y tareas (Room) en un hilo secundario. */
    private void cargarDatos() {
        AppDatabase.ejecutar(() -> {
            RoomMateDB db = new RoomMateDB(this);
            List<String> companeros = db.obtenerNombresCompaneros();
            db.close();
            List<Tarea> tareas = tareaDao.obtenerTodas();

            runOnUiThread(() -> {
                nombresCompaneros.clear();
                nombresCompaneros.addAll(companeros);
                if (nombresCompaneros.isEmpty()) {
                    nombresCompaneros.add(getString(R.string.sin_asignar));
                }
                adapterGanador.notifyDataSetChanged();

                // Primer Spinner: "Todos" + companeros.
                adapterCompaneroFiltro.clear();
                adapterCompaneroFiltro.add(getString(R.string.todos));
                adapterCompaneroFiltro.addAll(companeros);
                adapterCompaneroFiltro.notifyDataSetChanged();

                todasLasTareas.clear();
                todasLasTareas.addAll(tareas);
                filtrarTareasPorCompanero();
            });
        });
    }

    /** Rellena el Spinner de tareas segun el companero elegido en el primer Spinner. */
    private void filtrarTareasPorCompanero() {
        int posicion = spinnerCompanero.getSelectedItemPosition();
        String companero = posicion > 0 ? adapterCompaneroFiltro.getItem(posicion) : null;

        tareasFiltradas.clear();
        nombresTareas.clear();
        for (Tarea t : todasLasTareas) {
            if (companero == null || companero.equals(t.getCompanero())) {
                tareasFiltradas.add(t);
                nombresTareas.add(t.getNombre());
            }
        }
        adapterTareas.notifyDataSetChanged();

        if (tareasFiltradas.isEmpty()) {
            tareaSeleccionadaId = -1;
            registros.clear();
            lineasRegistros.clear();
            adapterRegistros.notifyDataSetChanged();
            tvTotalRegistros.setText(R.string.total_registros_cero);
        } else {
            // Se selecciona la tarea preseleccionada (si esta en la lista) o la primera.
            int posicionTarea = 0;
            for (int i = 0; i < tareasFiltradas.size(); i++) {
                if (tareasFiltradas.get(i).getId() == tareaPreseleccionadaId) {
                    posicionTarea = i;
                    break;
                }
            }
            spinnerTareas.setSelection(posicionTarea);
            tareaSeleccionadaId = tareasFiltradas.get(posicionTarea).getId();
            cargarRegistros();
        }
    }

    /** Lee los registros de la tarea seleccionada y los totales. */
    private void cargarRegistros() {
        int tareaId = tareaSeleccionadaId;
        if (tareaId == -1) {
            return;
        }

        AppDatabase.ejecutar(() -> {
            List<RegistroTarea> lista = registroTareaDao.obtenerPorTarea(tareaId);
            int total = registroTareaDao.contarPorTarea(tareaId);
            int totalGeneral = registroTareaDao.contarTotal();

            runOnUiThread(() -> {
                registros.clear();
                registros.addAll(lista);
                lineasRegistros.clear();
                for (RegistroTarea r : lista) {
                    lineasRegistros.add(getString(R.string.linea_registro,
                            r.getFecha(), r.getPuntuacion(), r.getGanador()));
                }
                adapterRegistros.notifyDataSetChanged();
                tvTotalRegistros.setText(getString(R.string.total_registros, total));
                tvTotalGeneral.setText(getString(R.string.total_general, totalGeneral));
            });
        });
    }

    /** Pasa el formulario a modo edicion con los datos del registro. */
    private void empezarEdicion(RegistroTarea registro) {
        registroEnEdicion = registro;
        etFecha.setText(registro.getFecha());
        etPuntuacion.setText(String.valueOf(registro.getPuntuacion()));
        int pos = nombresCompaneros.indexOf(registro.getGanador());
        if (pos >= 0) {
            spinnerGanador.setSelection(pos);
        }
        tvTituloForm.setText(R.string.editar_registro);
        btnGuardar.setText(R.string.guardar_cambios);
        btnCancelarEdicion.setVisibility(View.VISIBLE);
    }

    /** Vuelve el formulario a modo "nuevo registro". */
    private void cancelarEdicion() {
        registroEnEdicion = null;
        etFecha.setText("");
        etPuntuacion.setText("");
        tvTituloForm.setText(R.string.nuevo_registro);
        btnGuardar.setText(R.string.guardar_registro);
        btnCancelarEdicion.setVisibility(View.GONE);
    }

    /** Valida y guarda (insert o update) el registro en Room. */
    private void guardarRegistro() {
        String fecha = etFecha.getText().toString().trim();
        String textoPuntuacion = etPuntuacion.getText().toString().trim();

        if (fecha.isEmpty()) {
            etFecha.setError(getString(R.string.error_fecha));
            return;
        }

        int puntuacion;
        try {
            puntuacion = Integer.parseInt(textoPuntuacion);
        } catch (NumberFormatException e) {
            etPuntuacion.setError(getString(R.string.error_puntuacion));
            etPuntuacion.requestFocus();
            return;
        }

        if (tareaSeleccionadaId == -1) {
            Toast.makeText(this, R.string.selecciona_tarea, Toast.LENGTH_SHORT).show();
            return;
        }

        String ganador = spinnerGanador.getSelectedItem() == null
                ? getString(R.string.sin_asignar)
                : spinnerGanador.getSelectedItem().toString();

        RegistroTarea editado = registroEnEdicion;

        AppDatabase.ejecutar(() -> {
            if (editado == null) {
                registroTareaDao.insertar(
                        new RegistroTarea(tareaSeleccionadaId, fecha, puntuacion, ganador));
            } else {
                editado.setFecha(fecha);
                editado.setPuntuacion(puntuacion);
                editado.setGanador(ganador);
                registroTareaDao.actualizar(editado);
            }

            runOnUiThread(() -> {
                cancelarEdicion();
                Toast.makeText(this, R.string.registro_guardado, Toast.LENGTH_SHORT).show();
                cargarRegistros();
            });
        });
    }

    /** Borra el registro (ya confirmado) y recarga la lista. */
    private void borrarRegistro(RegistroTarea registro) {
        AppDatabase.ejecutar(() -> {
            registroTareaDao.borrar(registro);
            runOnUiThread(() -> {
                if (registro == registroEnEdicion) {
                    cancelarEdicion();
                }
                Toast.makeText(this, R.string.registro_eliminado, Toast.LENGTH_SHORT).show();
                cargarRegistros();
            });
        });
    }
}
