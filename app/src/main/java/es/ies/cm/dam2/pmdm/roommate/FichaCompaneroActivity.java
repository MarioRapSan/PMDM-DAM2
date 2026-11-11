package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
    private ImageView ivFotoFicha;
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
        ivFotoFicha = findViewById(R.id.ivFotoFicha);

        Button btnBorrar = findViewById(R.id.btnBorrarCompanero);
        Button btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        Button btnVolver = findViewById(R.id.btnVolverAlta);

        btnEditarPerfil.setOnClickListener(v -> abrirEditarPerfil());

        adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_texto,
                companeros
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_texto);
        spinnerCompaneros.setAdapter(adapter);

        spinnerCompaneros.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        mostrarInformacion();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        FotoPerfilUtils.mostrarFotoPerfil(ivFotoFicha, null);
                    }
                }
        );

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
        int idSeleccionado = -1;
        int posicionActual = spinnerCompaneros.getSelectedItemPosition();

        if (posicionActual >= 0 && posicionActual < companeros.size()) {
            idSeleccionado = companeros.get(posicionActual).getId();
        }

        companeros.clear();
        companeros.addAll(db.obtenerCompaneros());
        adapter.notifyDataSetChanged();
        actualizarEstado();

        if (companeros.isEmpty()) {
            tvInformacion.setText("No hay compañeros registrados.");
            FotoPerfilUtils.mostrarFotoPerfil(ivFotoFicha, null);
            return;
        }

        boolean seleccionado = false;
        if (idSeleccionado != -1) {
            seleccionado = seleccionarCompaneroPorId(idSeleccionado);
        }

        if (!seleccionado) {
            spinnerCompaneros.setSelection(0);
        }

        mostrarFotoSeleccionada();
        mostrarInformacion();
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
        FotoPerfilUtils.mostrarFotoPerfil(ivFotoFicha, c.getFotoPerfil());

        String fecha = c.getFechaEntrada();
        if (fecha == null || fecha.trim().isEmpty()) {
            fecha = "Sin fecha";
        }

        String telefono = c.getTelefono();
        if (telefono == null || telefono.trim().isEmpty()) {
            telefono = getString(R.string.sin_telefono);
        }

        String correo = c.getCorreo();
        if (correo == null || correo.trim().isEmpty()) {
            correo = getString(R.string.sin_correo);
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
                        + getString(R.string.telefono) + ": " + telefono + "\n\n"
                        + getString(R.string.correo_electronico) + ": " + correo + "\n\n"
                        + "Introducción personal:\n" + introduccion;

        tvInformacion.setText(informacion);
    }

    private void mostrarFotoSeleccionada() {
        int posicion = spinnerCompaneros.getSelectedItemPosition();

        if (posicion >= 0 && posicion < companeros.size()) {
            FotoPerfilUtils.mostrarFotoPerfil(
                    ivFotoFicha,
                    companeros.get(posicion).getFotoPerfil()
            );
        } else {
            FotoPerfilUtils.mostrarFotoPerfil(ivFotoFicha, null);
        }
    }

    private void abrirEditarPerfil() {
        int posicion = spinnerCompaneros.getSelectedItemPosition();
        if (posicion < 0 || posicion >= companeros.size()) {
            Toast.makeText(
                    this,
                    "No hay compañeros registrados",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Companero companero = companeros.get(posicion);
        Intent intent = new Intent(this, EditarCompaneroActivity.class);
        intent.putExtra(
                EditarCompaneroActivity.EXTRA_COMPANERO_ID,
                companero.getId()
        );
        startActivity(intent);
    }

    private boolean seleccionarCompaneroPorId(int id) {
        for (int i = 0; i < companeros.size(); i++) {
            if (companeros.get(i).getId() == id) {
                spinnerCompaneros.setSelection(i);
                return true;
            }
        }

        return false;
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
        int borrados = db.borrarCompanero(companero.getId());

        if (borrados > 0) {
            FotoPerfilUtils.borrarFotoPerfil(companero.getFotoPerfil());
            tvInformacion.setText(
                    "Selecciona un compañero y pulsa Mostrar información."
            );
            cargarCompaneros();

            Toast.makeText(
                    this,
                    "Compañero eliminado: " + companero.getNombreCompleto(),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    this,
                    R.string.error_borrar_companero,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void actualizarEstado() {
        boolean hayCompaneros = !companeros.isEmpty();
        spinnerCompaneros.setEnabled(hayCompaneros);
    }
}
