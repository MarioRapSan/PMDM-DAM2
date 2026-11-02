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
import java.util.List;

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

    private String companeroSeleccionado = null;
    private String prioridadSeleccionada = null;
    private String tareaSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_tareas);
        aplicarInsets();

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
    }

    private void cargarCatalogoTareas() {
        catalogoTareas.add(new ListaTareaPrioridad("Limpiar baño", "Puede esperar"));
        catalogoTareas.add(new ListaTareaPrioridad("Limpiar salón", "Puede esperar"));
        catalogoTareas.add(new ListaTareaPrioridad("Regar las plantas", "Puede esperar"));
        catalogoTareas.add(new ListaTareaPrioridad("Ordenar el armario", "Puede esperar"));

        catalogoTareas.add(new ListaTareaPrioridad("Limpiar cocina", "Hacer cuanto antes"));
        catalogoTareas.add(new ListaTareaPrioridad("Comprar productos de limpieza", "Hacer cuanto antes"));
        catalogoTareas.add(new ListaTareaPrioridad("Lavar la ropa", "Hacer cuanto antes"));
        catalogoTareas.add(new ListaTareaPrioridad("Cambiar las sábanas", "Hacer cuanto antes"));

        catalogoTareas.add(new ListaTareaPrioridad("Sacar la basura", "Urgente"));
        catalogoTareas.add(new ListaTareaPrioridad("Comprar productos de cocina", "Urgente"));
        catalogoTareas.add(new ListaTareaPrioridad("Arreglar la persiana", "Urgente"));
        catalogoTareas.add(new ListaTareaPrioridad("Pagar la luz", "Urgente"));
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
        String[] companeros = getResources()
                .getStringArray(R.array.companeros_tarea);

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
                    companeroSeleccionado = companeros[position];
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
            String estado;

            if (radioCompletada.isChecked()) {
                estado = getString(R.string.completada);
            } else if (radioPendiente.isChecked()) {
                estado = getString(R.string.pendiente);
            } else {
                estado = getString(R.string.sin_estado);
            }

            listaT.add(new ListaTareaPrioridad(
                    tareaSeleccionada,
                    prioridadSeleccionada,
                    companeroSeleccionado,
                    estado
            ));

            adaptadorLista.notifyDataSetChanged();
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
