package es.ies.cm.dam2.pmdm.roommate;


import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class ListaTareas extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private String[] Tareas = {
            "Limpiar baño", "Limpiar salón",
            "Limpiar cocina", "Sacar la basura",
            "Comprar productos de limpieza", "Comprar productos de cocina"
    };
    private String[] Companeros = {
            "Jose", "Raul",
            "Winwaleo"
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_tareas);

        // Take the instance of Spinner and apply OnItemSelectedListener on it
        Spinner spinTareas = findViewById(R.id.spinnerTareas);
        spinTareas.setOnItemSelectedListener(this);
        Spinner spinCompa = findViewById(R.id.spinnerCompaneros);
        spinCompa.setOnItemSelectedListener(this);


        // Create the instance of ArrayAdapter having the list of courses
        ArrayAdapter<String> ad = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Tareas

        );
        ArrayAdapter<String> ad2 = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Companeros

        );

        // Set simple layout resource file for each item of spinner
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the ArrayAdapter (ad) data on the Spinner which binds data to spinner
        spinTareas.setAdapter(ad);
        spinCompa.setAdapter(ad2);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Make toast of the name of the course which is selected in the spinner
        Toast.makeText(getApplicationContext(), Tareas[position], Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), Companeros[position], Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // No action needed when no selection is made
    }
}