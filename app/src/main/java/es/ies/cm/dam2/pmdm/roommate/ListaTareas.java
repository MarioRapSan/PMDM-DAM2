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

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ListaTareas extends AppCompatActivity {

    private static final String TEXTO_SIN_PRIORIDAD = "Selecciona una prioridad";
    private static final String TEXTO_SIN_TAREA = "Selecciona una tarea";
    private static final String TEXTO_SIN_COMPANERO = "Selecciona un compañero";

    private ListView lista;
    private Spinner spinPrioridad;
    private Spinner spinTareas;
    private Spinner spinCompaneros;
    private RadioButton radioPendiente;
    private RadioButton radioCompletada;
    private Button btnAnadir;
    private Button btnVolver;

    private final String[] Companeros = {
            TEXTO_SIN_COMPANERO,
            "Jose",
            "Raul",
            "Winwaleo"
    };

    private final String[] Prioridades = {
            TEXTO_SIN_PRIORIDAD,
            "Puede esperar",
            "Hacer cuanto antes",
            "Urgente"
    };

    private final List<ListaTareaPrioridad> catalogoTareas = new ArrayList<>();
    private final List<ListaTareaPrioridad> listaT = new ArrayList<>();

    private ArrayAdapter<ListaTareaPrioridad> adaptadorLista;

    private String companeroSeleccionado = null;
    private String prioridadSeleccionada = null;
    private String tareaSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_tareas);

        lista = findViewById(R.id.simpleListView);
        spinPrioridad = findViewById(R.id.spinnerPrioridad);
        spinTareas = findViewById(R.id.spinnerTareas);
        spinCompaneros = findViewById(R.id.spinnerCompaneros);
        radioPendiente = findViewById(R.id.radioPendiente);
        radioCompletada = findViewById(R.id.radioCompletada);
        btnAnadir = findViewById(R.id.btnAnadir);
        btnVolver = findViewById(R.id.btnVolver);

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
        // Añade aquí más tareas si quieres que el spinner tenga más opciones.
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
                android.R.layout.simple_list_item_1,
                listaT
        );

        lista.setAdapter(adaptadorLista);
    }

    private void configurarSpinnerPrioridad() {
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Prioridades
        );

        adaptador.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
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
                    prioridadSeleccionada = Prioridades[position];
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
                    tareaSeleccionada = parent.getItemAtPosition(position).toString();
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
        tareasFiltradas.add(TEXTO_SIN_TAREA);

        if (prioridadSeleccionada != null) {
            for (ListaTareaPrioridad tarea : catalogoTareas) {
                if (tarea.getPrioridad().equals(prioridadSeleccionada)) {
                    tareasFiltradas.add(tarea.getNombre());
                }
            }
        }

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tareasFiltradas
        );

        adaptador.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinTareas.setAdapter(adaptador);
        spinTareas.setEnabled(prioridadSeleccionada != null);
        tareaSeleccionada = null;
    }

    private void configurarSpinnerCompaneros() {
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Companeros
        );

        adaptador.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
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
                    companeroSeleccionado = Companeros[position];
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
                        "Estado: Pendiente",
                        Toast.LENGTH_SHORT
                ).show()
        );

        radioCompletada.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Estado: Completada",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    private void configurarBotonAnadir() {
        btnAnadir.setOnClickListener(v -> {
            String estado = "Sin estado";

            if (radioPendiente.isChecked()) {
                estado = "Pendiente";
            } else if (radioCompletada.isChecked()) {
                estado = "Completada";
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
                    "Tarea añadida",
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

    private void actualizarEstadoBoton() {
        btnAnadir.setEnabled(
                prioridadSeleccionada != null
                        && companeroSeleccionado != null
                        && tareaSeleccionada != null
        );
    }
}