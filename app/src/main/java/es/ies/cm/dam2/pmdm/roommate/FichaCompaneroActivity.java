package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Muestra la ficha de los companeros guardados en la base de datos.
 */
public class FichaCompaneroActivity extends AppCompatActivity {

    private Spinner spinnerCompaneros;
    private TextView tvInformacion;
    private ArrayAdapter<Companero> adapter;
    private final List<Companero> companeros = new ArrayList<>();
    private RoomMateDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ficha_companero);

        db = new RoomMateDB(this);

        spinnerCompaneros = findViewById(R.id.spinnerCompaneros);
        tvInformacion = findViewById(R.id.tvInformacion);

        Button btnBorrar = findViewById(R.id.btnBorrarCompanero);
        Button btnMostrar = findViewById(R.id.btnMostrarInformacion);
        Button btnVolver = findViewById(R.id.btnVolverAlta);

        adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_texto,
                companeros
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_texto);
        spinnerCompaneros.setAdapter(adapter);

        btnMostrar.setOnClickListener(v -> mostrarInformacion());
        btnBorrar.setOnClickListener(v -> borrarCompanero());
        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(
                    FichaCompaneroActivity.this,
                    MainActivity.class
            );
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarCompaneros();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

    private void cargarCompaneros() {
        companeros.clear();
        companeros.addAll(db.obtenerCompaneros());
        adapter.notifyDataSetChanged();

        if (companeros.isEmpty()) {
            tvInformacion.setText("No hay compañeros registrados.");
        }

        actualizarEstado();
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

        String fecha = c.getFechaEntrada();
        if (fecha == null || fecha.trim().isEmpty()) {
            fecha = "Sin fecha";
        }

        String introduccion = c.getIntroduccion();
        if (introduccion == null || introduccion.trim().isEmpty()) {
            introduccion = "Sin introducción personal";
        }

        String informacion =
                "Nombre: " + c.getNombre() + " " + c.getApellidos() + "\n\n"
                        + "Sexo: " + c.getSexo() + "\n\n"
                        + "Edad: " + c.getEdad() + " años\n\n"
                        + "Fecha de entrada: " + fecha + "\n\n"
                        + "Foto de perfil: " + c.getFotoPerfil() + "\n\n"
                        + "Introducción personal:\n" + introduccion;

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

        Companero companero = companeros.get(posicion);
        db.borrarCompanero(companero.getId());

        tvInformacion.setText(
                "Selecciona un compañero y pulsa Mostrar información."
        );

        cargarCompaneros();

        Toast.makeText(
                this,
                "Compañero eliminado: " + companero.getNombreCompleto(),
                Toast.LENGTH_SHORT
        ).show();
    }

    private void actualizarEstado() {
        boolean hayCompaneros = !companeros.isEmpty();
        spinnerCompaneros.setEnabled(hayCompaneros);
    }
}
