package es.ies.cm.dam2.pmdm.roommate;

import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Permite editar la foto y los datos de contacto de un companero concreto.
 * Solo se abre desde la ficha del companero seleccionado.
 */
public class EditarCompaneroActivity extends AppCompatActivity {

    public static final String EXTRA_COMPANERO_ID = "extra_companero_id";

    private RoomMateDB db;
    private int companeroId;
    private Companero companero;
    private String fotoPerfilActual = "ic_persona";
    private String fotoPerfilOriginal = "ic_persona";

    private ImageView ivFotoEditar;
    private TextView tvNombreCompaneroEditar;
    private EditText etTelefonoEditar;
    private EditText etCorreoEditar;

    private ActivityResultLauncher<String> selectorFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_companero);

        db = new RoomMateDB(this);

        companeroId = getIntent().getIntExtra(EXTRA_COMPANERO_ID, -1);
        if (companeroId == -1) {
            Toast.makeText(
                    this,
                    R.string.error_companero_no_encontrado,
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        ivFotoEditar = findViewById(R.id.ivFotoEditar);
        tvNombreCompaneroEditar = findViewById(R.id.tvNombreCompaneroEditar);
        etTelefonoEditar = findViewById(R.id.etTelefonoEditar);
        etCorreoEditar = findViewById(R.id.etCorreoEditar);

        Button btnCambiarFoto = findViewById(R.id.btnCambiarFotoEditar);
        Button btnGuardar = findViewById(R.id.btnGuardarEditar);
        Button btnCancelar = findViewById(R.id.btnCancelarEditar);

        selectorFoto = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::seleccionarFoto
        );

        btnCambiarFoto.setOnClickListener(
                v -> selectorFoto.launch("image/*")
        );
        btnGuardar.setOnClickListener(v -> guardarCambios());
        btnCancelar.setOnClickListener(v -> finish());

        companero = db.obtenerCompanero(companeroId);
        if (companero == null) {
            Toast.makeText(
                    this,
                    R.string.error_companero_no_encontrado,
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        cargarDatos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

    private void cargarDatos() {
        tvNombreCompaneroEditar.setText(companero.getNombreCompleto());
        etTelefonoEditar.setText(companero.getTelefono());
        etCorreoEditar.setText(companero.getCorreo());

        String foto = companero.getFotoPerfil();
        if (foto == null || foto.trim().isEmpty()) {
            foto = "ic_persona";
        }

        fotoPerfilActual = foto;
        fotoPerfilOriginal = foto;
        FotoPerfilUtils.mostrarFotoPerfil(ivFotoEditar, fotoPerfilActual);
    }

    private void seleccionarFoto(Uri uri) {
        if (uri == null) {
            return;
        }

        String ruta = FotoPerfilUtils.guardarFotoPerfil(this, uri);
        if (ruta == null) {
            Toast.makeText(
                    this,
                    R.string.error_foto_perfil,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Si se elige otra foto antes de guardar, se elimina la anterior.
        if (fotoPerfilActual != null
                && !fotoPerfilActual.equals(fotoPerfilOriginal)) {
            FotoPerfilUtils.borrarFotoPerfil(fotoPerfilActual);
        }

        fotoPerfilActual = ruta;
        FotoPerfilUtils.mostrarFotoPerfil(ivFotoEditar, fotoPerfilActual);
    }

    private void guardarCambios() {
        String telefono = etTelefonoEditar.getText().toString().trim();
        String correo = etCorreoEditar.getText().toString().trim();

        if (!correo.isEmpty()
                && !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(
                    this,
                    R.string.error_correo,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Se conservan los datos personales y solo cambian foto y contacto.
        Companero actualizado = new Companero(
                companeroId,
                companero.getNombre(),
                companero.getApellidos(),
                companero.getEdad(),
                companero.getSexo(),
                companero.getFechaEntrada(),
                companero.getIntroduccion(),
                fotoPerfilActual,
                telefono,
                correo
        );

        int filas = db.actualizarCompanero(actualizado);
        if (filas > 0) {
            if (fotoPerfilOriginal != null
                    && !fotoPerfilOriginal.equals(fotoPerfilActual)) {
                FotoPerfilUtils.borrarFotoPerfil(fotoPerfilOriginal);
            }

            Toast.makeText(
                    this,
                    R.string.perfil_actualizado,
                    Toast.LENGTH_SHORT
            ).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(
                    this,
                    R.string.error_actualizar_perfil,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
