package es.ies.cm.dam2.pmdm.roommate;

import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * Creacion del perfil de companero del usuario que ha iniciado sesion
 * (INSERT en la tabla companeros de SQLite, ligado a su cuenta con usuario_id).
 *
 * Se abre desde el listado de companeros, solo si el usuario aun no tiene perfil. Valida los datos obligatorios,
 * permite elegir una foto de la galeria y la fecha de entrada con el
 * SelectorFechaDialogFragment. Al guardar vuelve al listado, que se recarga solo.
 */
public class AltaCompaneroActivity extends AppCompatActivity {

    private static final String ESTADO_FOTO = "foto_seleccionada";

    private EditText etNombre;
    private EditText etApellidos;
    private EditText etEdad;
    private EditText etFechaEntrada;
    private EditText etTelefono;
    private EditText etCorreo;
    private EditText etIntroduccion;
    private RadioGroup rgSexo;
    private ImageView ivFotoCompanero;
    private String fotoPerfilSeleccionada = "ic_persona";
    private ActivityResultLauncher<String> selectorFoto;

    /** Cuenta a la que se liga el perfil nuevo. */
    private int usuarioId;

    /** Prepara la barra superior, el formulario, el selector de foto y el de fecha. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alta_companero);

        usuarioId = getIntent().getIntExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, -1);
        if (usuarioId == -1) {
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbarAlta);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.crear_mi_perfil);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ivFotoCompanero = findViewById(R.id.ivFotoCompanero);
        etNombre = findViewById(R.id.etNombre);
        etApellidos = findViewById(R.id.etApellidos);
        etEdad = findViewById(R.id.etEdad);
        etFechaEntrada = findViewById(R.id.etFechaEntrada);
        etTelefono = findViewById(R.id.etTelefono);
        etCorreo = findViewById(R.id.etCorreo);
        etIntroduccion = findViewById(R.id.etIntroduccion);
        rgSexo = findViewById(R.id.rgSexo);

        // Recupera la foto elegida si se ha girado la pantalla.
        if (savedInstanceState != null) {
            fotoPerfilSeleccionada = savedInstanceState.getString(ESTADO_FOTO, "ic_persona");
            FotoPerfilUtils.mostrarFotoPerfil(ivFotoCompanero, fotoPerfilSeleccionada);
        }

        // Foto de perfil: se elige en la galeria y se copia a la carpeta de la app.
        selectorFoto = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::seleccionarFoto
        );
        Button btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        btnSeleccionarFoto.setOnClickListener(v -> selectorFoto.launch("image/*"));

        // Fecha de entrada con el DialogFragment del calendario.
        etFechaEntrada.setOnClickListener(v ->
                SelectorFechaDialogFragment.nuevo(etFechaEntrada.getText().toString().trim())
                        .show(getSupportFragmentManager(), "selector_fecha"));
        getSupportFragmentManager().setFragmentResultListener(
                SelectorFechaDialogFragment.REQUEST_KEY, this, (clave, resultado) ->
                        etFechaEntrada.setText(
                                resultado.getString(SelectorFechaDialogFragment.RESULTADO_FECHA)));

        Button btnGuardar = findViewById(R.id.btnCheck);
        Button btnCancelar = findViewById(R.id.btnCancelarAlta);
        btnGuardar.setOnClickListener(v -> guardarCompanero());
        btnCancelar.setOnClickListener(v -> finish());
    }

    /** Guarda la foto elegida para no perderla al girar la pantalla. */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Los EditText se restauran solos; la foto elegida hay que guardarla a mano.
        outState.putString(ESTADO_FOTO, fotoPerfilSeleccionada);
    }

    /** La flecha de la barra superior cierra la pantalla y vuelve al listado. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Copia la imagen elegida en la galeria a la carpeta de la app y la muestra. */
    private void seleccionarFoto(Uri uri) {
        if (uri == null) {
            return;
        }

        String ruta = FotoPerfilUtils.guardarFotoPerfil(this, uri);
        if (ruta == null) {
            Toast.makeText(this, R.string.error_foto_perfil, Toast.LENGTH_SHORT).show();
            return;
        }

        fotoPerfilSeleccionada = ruta;
        FotoPerfilUtils.mostrarFotoPerfil(ivFotoCompanero, ruta);
    }

    /** Valida el formulario y guarda el companero (INSERT en SQLite) en un hilo secundario. */
    private void guardarCompanero() {
        String nombre = etNombre.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String edadTexto = etEdad.getText().toString().trim();
        String fechaEntrada = etFechaEntrada.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String introduccion = etIntroduccion.getText().toString().trim();
        int sexoId = rgSexo.getCheckedRadioButtonId();

        // El nombre, los apellidos, la edad y el sexo son obligatorios.
        if (nombre.isEmpty() || apellidos.isEmpty() || edadTexto.isEmpty() || sexoId == -1) {
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

        if (!correo.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, R.string.error_correo, Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton radioSexo = findViewById(sexoId);
        // El perfil queda ligado a la cuenta del usuario: solo el podra editarlo.
        Companero nuevo = new Companero(
                nombre,
                apellidos,
                edad,
                radioSexo.getText().toString(),
                fechaEntrada,
                introduccion,
                fotoPerfilSeleccionada,
                telefono,
                correo
        );
        nuevo.setUsuarioId(usuarioId);

        AppDatabase.ejecutar(() -> {
            RoomMateDB db = new RoomMateDB(this);
            long id = db.insertarCompanero(nuevo);
            db.close();

            runOnUiThread(() -> {
                if (id == -1) {
                    Toast.makeText(this, R.string.error_guardar_companero, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, R.string.companero_registrado, Toast.LENGTH_SHORT).show();
                // Vuelve al listado de companeros, que se recarga en onResume.
                finish();
            });
        });
    }

    /** Avisa con un Toast de que hay datos incorrectos en el formulario. */
    private void mostrarError() {
        Toast.makeText(this, R.string.datos_incorrectos, Toast.LENGTH_SHORT).show();
    }
}
