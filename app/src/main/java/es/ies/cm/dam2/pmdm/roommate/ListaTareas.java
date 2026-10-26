package es.ies.cm.dam2.pmdm.roommate;


import android.content.Intent;
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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListaTareas extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

        private ListView lista;
        private Spinner spinTareas, spinPrioridad;
        private RadioButton radioPendiente, radioCompletada;


        private final String[] Tareas = {
                "Limpiar baño",
                "Limpiar salón",
                "Limpiar cocina",
                "Sacar la basura",
                "Comprar productos de limpieza",
                "Comprar productos de cocina"
        };

        private final String[] Companeros = {
                "Jose", "Raul", "Winwaleo"
        };
    List<ListaTareaPrioridad> listaT = new ArrayList<>();
    private final String[] Prioridad = {
            "Puede esperar", "Hacer cuanto antes", "Urgente"
    };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_lista_tareas);

            lista = findViewById(R.id.simpleListView);
            spinTareas = findViewById(R.id.spinnerTareas);
            spinPrioridad = findViewById(R.id.spinnerPrioridad);
            radioPendiente = findViewById(R.id.radioPendiente);
            radioCompletada = findViewById(R.id.radioCompletada);

            // Adaptador de la lista de compañeros
            ArrayAdapter<String> adaptadorLista =
                    new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_list_item_single_choice,
                            Companeros
                    );

            lista.setAdapter(adaptadorLista);

            lista.setOnItemClickListener((parent, view, position, id) -> {
                String companero =
                        parent.getItemAtPosition(position).toString();

                Toast.makeText(
                        this,
                        "Compañero seleccionado: " + companero,
                        Toast.LENGTH_SHORT
                ).show();
            });

            // Adaptador del Spinner de tareas
            ArrayAdapter<String> adaptadorTareas =
                    new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            Tareas
                    );

            adaptadorTareas.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
            );

            spinTareas.setAdapter(adaptadorTareas);

            // Adaptador del Spinner de prioridad
            ArrayAdapter<String> adaptadorPrioridad =
                    new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            Prioridad
                    );

            adaptadorPrioridad.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
            );

            spinPrioridad.setAdapter(adaptadorPrioridad);

            // Listener para el Spinner de tareas
            spinTareas.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(
                                AdapterView<?> parent,
                                View view,
                                int position,
                                long id
                        ) {
                            String tarea = Tareas[position];
                            Toast.makeText(
                                    es.ies.cm.dam2.pmdm.roommate.ListaTareas.this,
                                    "Tarea: " + tarea,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    }
            );

            // Listener para el Spinner de prioridad
            spinPrioridad.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(
                                AdapterView<?> parent,
                                View view,
                                int position,
                                long id
                        ) {
                            String prioridad = Prioridad[position];
                            Toast.makeText(
                                    es.ies.cm.dam2.pmdm.roommate.ListaTareas.this,
                                    "Prioridad: " + prioridad,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    }
            );

            // Listeners de los RadioButton
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
            Button btnAnadir = findViewById(R.id.btnAnadir);
            btnAnadir.setOnClickListener(v -> {
                ListaTareaPrioridad.add(new ListaTareaPrioridad(spinTareas.getTag(),spinPrioridad.getTag());
            });
        }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
