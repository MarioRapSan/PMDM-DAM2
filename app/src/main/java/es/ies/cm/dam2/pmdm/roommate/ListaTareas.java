// app/src/main/java/es/ies/cm/dam2/pmdm/roommate/ListaTareas.java
package es.ies.cm.dam2.pmdm.roommate;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.ies.cm.dam2.pmdm.roommate.db.RoomMateDB;
import es.ies.cm.dam2.pmdm.roommate.db.Tarea;
import es.ies.cm.dam2.pmdm.roommate.db.TareasRepository;

/**
 * Pantalla de lista de tareas con filtros por prioridad, tarea y companero.
 * Los datos se leen y se guardan en SQLite.
 */
public class ListaTareas extends AppCompatActivity {

    private ListView lista;
    private Spinner spinPrioridad;
    private Spinner spinTareas;
    private Spinner spinCompaneros;
    private RadioButton radioPendiente;
    private RadioButton radioCompletada;
    private Button btnAnadir;
    private Button btnVolver;

    private final List<ListaTareaPrioridad> catalogoTareas = new ArrayList<>();
    private final List<ListaTareaPrioridad> listaT = new ArrayList<>();

    private ArrayAdapter<ListaTareaPrioridad> adaptadorLista;
    private RoomMateDB db;

    private String companeroSeleccionado = null;
    private String prioridadSeleccionada = null;
    private String tareaSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_tareas);
        aplicarInsets();

        TareasRepository repository = new TareasRepository(this);
        repository.obtenerTareas(tareas);

        lista = findViewById(R.id.simpleListView);
        spinPrioridad = findViewById(R.id.spinnerPrioridad);
        spinTareas = findViewById(R.id.spinnerTareas);
        spinCompaneros = findViewById(R.id.spinnerCompaneros);
        radioPendiente = findViewById(R.id.radioPendiente);
        radioCompletada = findViewById(R.id.radioCompletada);
        btnAnadir = findViewById(R.id.btnAnadir);
        btnVolver = findViewById(R.id.btnVolver);

        radioPendiente.setChecked(true);

        cargarCatalogoTareas();

        configurarListaTareas();
        configurarSpinnerPrioridad();
        configurarSpinnerTareas();
        configurarSpinnerCompaneros();
        configurarRadioButtons();
        configurarBotonAnadir();
        configurarBotonVolver();

        actualizarSpinnerTareas();
        actualizarEstadoBoton();
        cargarListaDesdeBD();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db != null) {
            cargarCatalogoTareas();
            actualizarSpinnerTareas();
            cargarListaDesdeBD();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

    private void cargarCatalogoTareas() {
        catalogoTareas.clear();

        List<Tarea> tareas = db.obtenerTareas();
        Set<String> vistos = new HashSet<>();

        for (Tarea tarea : tareas) {
            String clave = tarea.getNombre() + "|" + tarea.getPrioridad();
            if (vistos.add(clave)) {
                catalogoTareas.add(new ListaTareaPrioridad(
                        tarea.getNombre(),
                        tarea.getPrioridad()
                ));
            }
        }
    }

    private void cargarListaDesdeBD() {
        listaT.clear();

        for (Tarea tarea : db.obtenerTareas()) {
            listaT.add(new ListaTareaPrioridad(
                    tarea.getNombre(),
                    tarea.getPrioridad(),
                    tarea.getCompanero(),
                    tarea.isHecha()
                            ? getString(R.string.completada)
                            : getString(R.string.pendiente)
            ));
        }

        if (adaptadorLista != null) {
            adaptadorLista.notifyDataSetChanged();
        }
    }

    private void configurarListaTareas() {
        adaptadorLista = new ArrayAdapter<>(
                this,
                R.layout.item_lista_tarea,
                listaT
        );

        lista.setAdapter(adaptadorLista);
        lista.setEmptyView(findViewById(R.id.tvListaVacia));
    }

    private void configurarSpinnerPrioridad() {
        String[] prioridades = getResources()
                .getStringArray(R.array.prioridades_tarea);

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_texto,
                prioridades
        );

        adaptador.setDropDownViewResource(
                R.layout.item_spinner_texto
        );

        spinPrioridad.setAdapter(adaptador);

        spinPrioridad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id
            ) {
                if (position == 0) {
                    prioridadSeleccionada = null;
                } else {
                    prioridadSeleccionada = prioridades[position];
                }

                actualizarSpinnerTareas();
                actualizarEstadoBoton();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                prioridadSeleccionada = null;
                actualizarSpinnerTareas();
                actualizarEstadoBoton();
            }
        });
    }

    private void configurarSpinnerTareas() {
        spinTareas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id
            ) {
                if (position == 0) {
                    tareaSeleccionada = null;
                } else {
                    tareaSeleccionada = parent
                            .getItemAtPosition(position)
                            .toString();
                }

                actualizarEstadoBoton();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tareaSeleccionada = null;
                actualizarEstadoBoton();
            }
        });
    }

    private void actualizarSpinnerTareas() {
        List<String> tareasFiltradas = new ArrayList<>();
        tareasFiltradas.add(getString(R.string.selecciona_tarea));

        if (prioridadSeleccionada != null) {
            for (ListaTareaPrioridad tarea : catalogoTareas) {
                if (tarea.getPrioridad().equals(prioridadSeleccionada)) {
                    tareasFiltradas.add(tarea.getNombre());
                }
            }
        }

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_texto,
                tareasFiltradas
        );

        adaptador.setDropDownViewResource(
                R.layout.item_spinner_texto
        );

        spinTareas.setAdapter(adaptador);
        spinTareas.setEnabled(prioridadSeleccionada != null);
        tareaSeleccionada = null;
    }

    private void configurarSpinnerCompaneros() {
        List<String> companeros = new ArrayList<>();
        companeros.add(getString(R.string.selecciona_companero));
        companeros.addAll(db.obtenerNombresCompaneros());

        if (companeros.size() == 1) {
            String[] ejemplos = getResources()
                    .getStringArray(R.array.companeros_tarea);
            for (int i = 1; i < ejemplos.length; i++) {
                companeros.add(ejemplos[i]);
            }
        }

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_texto,
                companeros
        );

        adaptador.setDropDownViewResource(
                R.layout.item_spinner_texto
        );

        spinCompaneros.setAdapter(adaptador);

        spinCompaneros.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id
            ) {
                if (position == 0) {
                    companeroSeleccionado = null;
                } else {
                    companeroSeleccionado = companeros.get(position);
                }

                actualizarEstadoBoton();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                companeroSeleccionado = null;
                actualizarEstadoBoton();
            }
        });
    }

    private void configurarRadioButtons() {
        radioPendiente.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        R.string.estado_pendiente,
                        Toast.LENGTH_SHORT
                ).show()
        );

        radioCompletada.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        R.string.estado_completada,
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    private void configurarBotonAnadir() {
        btnAnadir.setOnClickListener(v -> {
            boolean hecha = radioCompletada.isChecked();

            // Evita guardar dos veces la misma tarea con el mismo companero.
            for (Tarea tarea : db.obtenerTareas()) {
                if (tarea.getNombre().equals(tareaSeleccionada)
                        && tarea.getPrioridad().equals(prioridadSeleccionada)
                        && tarea.getCompanero().equals(companeroSeleccionado)) {
                    Toast.makeText(
                            this,
                            R.string.tarea_ya_en_lista,
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
            }

            Tarea nueva = new Tarea(
                    tareaSeleccionada,
                    prioridadSeleccionada,
                    "",
                    0,
                    "",
                    companeroSeleccionado,
                    hecha
            );
            nueva.setDificultad(
                    Tarea.dificultadPorPrioridad(prioridadSeleccionada)
            );

            long id = db.insertarTarea(nueva);

            if (id == -1) {
                Toast.makeText(
                        this,
                        R.string.error_guardar_tarea,
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            cargarListaDesdeBD();
            lista.smoothScrollToPosition(listaT.size() - 1);

            Toast.makeText(
                    this,
                    R.string.tarea_anadida,
                    Toast.LENGTH_SHORT
            ).show();

            tareaSeleccionada = null;
            actualizarSpinnerTareas();
            actualizarEstadoBoton();
        });
    }

    private void configurarBotonVolver() {
        btnVolver.setOnClickListener(v -> finish());
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

    private void actualizarEstadoBoton() {
        btnAnadir.setEnabled(
                prioridadSeleccionada != null
                        && companeroSeleccionado != null
                        && tareaSeleccionada != null
        );
    }
}
