
package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class FichaCompaneroActivity extends AppCompatActivity {

    private Spinner spinnerCompaneros;
    private TextView tvInformacion;
    private ArrayAdapter<Companero> adapter;
    private List<Companero> companeros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ficha_companero);

        spinnerCompaneros = findViewById(R.id.spinnerCompaneros);
        tvInformacion = findViewById(R.id.tvInformacion);

        Button btnBorrar = findViewById(R.id.btnBorrarCompanero);
        Button btnMostrar = findViewById(R.id.btnMostrarInformacion);
        Button btnVolver = findViewById(R.id.btnVolverAlta);

        companeros = CompaneroRepository.getCompaneros();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                companeros
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerCompaneros.setAdapter(adapter);

        actualizarEstado();

        btnMostrar.setOnClickListener(v -> mostrarInformacion());

        btnBorrar.setOnClickListener(v -> borrarCompanero());

        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(
                    FichaCompaneroActivity.this,
                    MainActivity.class
            );

            // Evita crear otra MainActivity si ya existe
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void mostrarInformacion() {
        int posicion = spinnerCompaneros.getSelectedItemPosition();

        if (posicion < 0 || posicion >= companeros.size()) {
            Toast.makeText(
                    this,
                    "No hay compañeros registrados",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Companero c = companeros.get(posicion);

        String informacion =
                "Nombre: " + c.getNombre() + "\n\n"
                        + "Sexo: " + c.getSexo() + "\n\n"
                        + "Edad: " + c.getEdad() + " años\n\n"
                        + "LGTB+ friendly: "
                        + (c.isLgtbFriendly() ? "Sí" : "No") + "\n\n"
                        + "Introducción personal:\n"
                        + (c.getIntroduccion().isEmpty()
                        ? "Sin introducción personal"
                        : c.getIntroduccion());

        tvInformacion.setText(informacion);
    }

    private void borrarCompanero() {
        int posicion = spinnerCompaneros.getSelectedItemPosition();

        if (posicion < 0 || posicion >= companeros.size()) {
            Toast.makeText(
                    this,
                    "No hay compañeros para borrar",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String nombre = companeros.get(posicion).getNombre();

        CompaneroRepository.borrar(posicion);
        adapter.notifyDataSetChanged();

        tvInformacion.setText(
                "Selecciona un compañero y pulsa Mostrar información."
        );

        actualizarEstado();

        Toast.makeText(
                this,
                "Compañero eliminado: " + nombre,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void actualizarEstado() {
        boolean hayCompaneros = !companeros.isEmpty();

        spinnerCompaneros.setEnabled(hayCompaneros);

        if (!hayCompaneros) {
            tvInformacion.setText("No hay compañeros registrados.");
        }
    }
}
