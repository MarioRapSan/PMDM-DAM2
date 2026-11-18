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

    /** Recoge el id del companero, prepara el selector de foto y carga sus datos. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_companero);

        db = new RoomMateDB(this);

        companeroId = getIntent().getIntExtra(EXTRA_COMPANERO_ID, -1);
        int usuarioId = getIntent().getIntExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, -1);
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

        // Lectura del companero fuera del hilo principal. Si la pantalla se ha
        // girado, no se sobrescriben los campos que el usuario ya habia cambiado.
        boolean primeraVez = savedInstanceState == null;
        AppDatabase.ejecutar(() -> {
            Companero leido = db.obtenerCompanero(companeroId);
            runOnUiThread(() -> {
                // Solo el dueno del perfil puede editarlo.
                if (leido != null && !leido.esDe(usuarioId)) {
                    Toast.makeText(this, R.string.solo_tu_perfil, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                if (leido == null) {
                    Toast.makeText(this, R.string.error_companero_no_encontrado,
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                companero = leido;
                if (primeraVez) {
                    cargarDatos();
                } else {
                    String original = companero.getFotoPerfil();
                    fotoPerfilOriginal = (original == null || original.trim().isEmpty())
                            ? "ic_persona" : original;
                    tvNombreCompaneroEditar.setText(companero.getNombreCompleto());
                    FotoPerfilUtils.mostrarFotoPerfil(ivFotoEditar, fotoPerfilActual);
                }
            });
        });
    }

    /** Guarda la foto elegida para no perderla al girar la pantalla. */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("foto_actual", fotoPerfilActual);
    }

    /** Recupera la foto elegida despues de girar la pantalla. */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fotoPerfilActual = savedInstanceState.getString("foto_actual", fotoPerfilActual);
    }

    /** Cierra la conexion con la base de datos al salir de la pantalla. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

    /** Rellena la pantalla con el nombre, el telefono, el correo y la foto del companero. */
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

    /** Copia la imagen elegida en la galeria a la carpeta de la app y la muestra. */
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

    /** Valida el correo y guarda la foto y los datos de contacto (UPDATE en SQLite). */
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

        AppDatabase.ejecutar(() -> {
            int filas = db.actualizarCompanero(actualizado);
            runOnUiThread(() -> mostrarResultadoGuardado(filas));
        });
    }

    /** Muestra el resultado del UPDATE y vuelve a la ficha si ha ido bien. */
    private void mostrarResultadoGuardado(int filas) {
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
