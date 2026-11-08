package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Pantalla de alta de companeros.
 * Tambien sirve como punto de entrada a las demas pantallas.
 */
public class MainActivity extends AppCompatActivity {

    private EditText etNombre;
    private EditText etApellidos;
    private EditText etEdad;
    private EditText etFechaEntrada;
    private EditText etIntroduccion;
    private RadioGroup rgSexo;

    private int usuarioId = 1;
    private String usuarioNombre = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Usuario que ha iniciado sesion. Si no llega ninguno, se usa admin.
        usuarioId = getIntent().getIntExtra(
                ColeccionTareasActivity.EXTRA_USUARIO_ID,
                1
        );
        usuarioNombre = getIntent().getStringExtra(
                ColeccionTareasActivity.EXTRA_USUARIO_NOMBRE
        );
        if (usuarioNombre == null || usuarioNombre.isEmpty()) {
            usuarioNombre = "admin";
        }

        etNombre = findViewById(R.id.etNombre);
        etApellidos = findViewById(R.id.etApellidos);
        etEdad = findViewById(R.id.etEdad);
        etFechaEntrada = findViewById(R.id.etFechaEntrada);
        etIntroduccion = findViewById(R.id.etIntroduccion);
        rgSexo = findViewById(R.id.rgSexo);

        Button btnCheck = findViewById(R.id.btnCheck);
        Button btnSiguiente = findViewById(R.id.btnSiguiente);
        Button btnInvitacion = findViewById(R.id.btnInvitacion);
        Button btnCorreo = findViewById(R.id.btnCorreo);
        Button btnLista = findViewById(R.id.btnLista);
        Button btnColeccion = findViewById(R.id.btnColeccionTareas);
        Button btnBuscar = findViewById(R.id.btnBuscar);
        Button btnCompra = findViewById(R.id.btnCompra);

        // Guarda el companero en la base de datos.
        btnCheck.setOnClickListener(v -> enviarFormulario());

        // Abre la ficha de los companeros guardados.
        btnSiguiente.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FichaCompaneroActivity.class);
            startActivity(intent);
        });

        btnInvitacion.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InvitacionActivity.class);
            startActivity(intent);
        });

        btnCorreo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CorreoActivity.class);
            startActivity(intent);
        });

        btnLista.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListaTareas.class);
            startActivity(intent);
        });

        btnColeccion.setOnClickListener(v -> abrirColeccion());
        btnBuscar.setOnClickListener(v -> abrirBuscar());
        btnCompra.setOnClickListener(v -> abrirCompra());
    }

    private void abrirColeccion() {
        Intent intent = new Intent(MainActivity.this, ColeccionTareasActivity.class);
        intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, usuarioId);
        intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_NOMBRE, usuarioNombre);
        startActivity(intent);
    }

    private void abrirBuscar() {
        Intent intent = new Intent(MainActivity.this, BuscarActivity.class);
        intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, usuarioId);
        intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_NOMBRE, usuarioNombre);
        startActivity(intent);
    }

    private void abrirCompra() {
        Intent intent = new Intent(MainActivity.this, ListaCompraActivity.class);
        intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, usuarioId);
        intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_NOMBRE, usuarioNombre);
        startActivity(intent);
    }

    private void enviarFormulario() {
        String nombre = etNombre.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String edadTexto = etEdad.getText().toString().trim();
        String fechaEntrada = etFechaEntrada.getText().toString().trim();
        String introduccion = etIntroduccion.getText().toString().trim();

        int sexoId = rgSexo.getCheckedRadioButtonId();

        // El nombre, los apellidos, la edad y el sexo son obligatorios.
        if (nombre.isEmpty()
                || apellidos.isEmpty()
                || edadTexto.isEmpty()
                || sexoId == -1) {
            mostrarError();
            return;
        }

        int edad;
        try {
            edad = Integer.parseInt(edadTexto);
        } catch (NumberFormatException e) {
            mostrarError();
            return;
        }

        if (edad < 18 || edad > 100) {
            mostrarError();
            return;
        }

        RadioButton radioSexo = findViewById(sexoId);
        String sexo = radioSexo.getText().toString();

        Companero nuevo = new Companero(
                nombre,
                apellidos,
                edad,
                sexo,
                fechaEntrada,
                introduccion,
                "ic_persona"
        );

        RoomMateDB db = new RoomMateDB(this);
        long id = db.insertarCompanero(nuevo);
        db.close();

        if (id == -1) {
            Toast.makeText(
                    this,
                    R.string.error_guardar_companero,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        etNombre.setText("");
        etApellidos.setText("");
        etEdad.setText("");
        etFechaEntrada.setText("");
        etIntroduccion.setText("");
        rgSexo.clearCheck();

        Toast.makeText(
                this,
                R.string.companero_registrado,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void mostrarError() {
        Toast.makeText(
                this,
                R.string.datos_incorrectos,
                Toast.LENGTH_SHORT
        ).show();
    }
}
