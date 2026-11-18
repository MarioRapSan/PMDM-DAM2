package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * Ficha de un companero de piso.
 *
 * Recibe el id del companero y lo lee de SQLite cada vez que se muestra
 * (onResume), asi despues de editarlo nunca ensena datos viejos.
 * Desde aqui se le puede enviar un correo (con su direccion ya escrita).
 * Editar y Borrar solo aparecen si es el perfil del usuario que ha iniciado
 * sesion: cada uno solo puede modificar su propio perfil.
 */
public class FichaCompaneroActivity extends AppCompatActivity {

    /** Extra con el id del companero que se va a mostrar. */
    public static final String EXTRA_COMPANERO_ID = "extra_companero_id_ficha";

    private static final String CLAVE_CONFIRMAR_BORRADO = "confirmar_borrado_ficha";

    private int companeroId;

    /** Usuario que ha iniciado sesion. */
    private int usuarioId;
    private Companero companero;

    private ImageView ivFoto;
    private TextView tvNombre;
    private TextView tvInformacion;
    private Button btnEditar;
    private Button btnBorrar;

    /** Recoge el id del companero y prepara la barra superior y los botones. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ficha_companero);

        companeroId = getIntent().getIntExtra(EXTRA_COMPANERO_ID, -1);
        usuarioId = getIntent().getIntExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, -1);
        if (companeroId == -1) {
            Toast.makeText(this, R.string.error_companero_no_encontrado, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbarFichaCompanero);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.ficha_companero);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ivFoto = findViewById(R.id.ivFotoFicha);
        tvNombre = findViewById(R.id.tvNombreFicha);
        tvInformacion = findViewById(R.id.tvInformacion);

        btnEditar = findViewById(R.id.btnEditarPerfil);
        Button btnCorreo = findViewById(R.id.btnCorreoCompanero);
        btnBorrar = findViewById(R.id.btnBorrarCompanero);
        Button btnVolver = findViewById(R.id.btnVolverFicha);

        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditarCompaneroActivity.class);
            intent.putExtra(EditarCompaneroActivity.EXTRA_COMPANERO_ID, companeroId);
            intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, usuarioId);
            startActivity(intent);
        });

        // Correo con la direccion del companero ya escrita (si la tiene).
        btnCorreo.setOnClickListener(v -> {
            if (companero != null) {
                Intent intent = new Intent(this, CorreoActivity.class);
                intent.putExtra(CorreoActivity.EXTRA_DESTINATARIO, companero.getCorreo());
                startActivity(intent);
            }
        });

        // Borrar pide confirmacion con el DialogFragment propio.
        btnBorrar.setOnClickListener(v -> ConfirmacionDialogFragment.nuevo(
                CLAVE_CONFIRMAR_BORRADO,
                R.string.eliminar_companero,
                R.string.mensaje_confirmar_eliminar_companero,
                R.string.eliminar
        ).show(getSupportFragmentManager(), "dialogo_borrar_companero"));

        getSupportFragmentManager().setFragmentResultListener(
                CLAVE_CONFIRMAR_BORRADO, this, (clave, resultado) -> {
                    if (resultado.getBoolean(ConfirmacionDialogFragment.RESULTADO_CONFIRMADO)) {
                        borrarCompanero();
                    }
                });

        btnVolver.setOnClickListener(v -> finish());
    }

    /** Se recarga al volver (por ejemplo, despues de editar) para no mostrar datos viejos. */
    @Override
    protected void onResume() {
        super.onResume();
        cargarCompanero();
    }

    /** La flecha de la barra superior vuelve al listado de companeros. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Lee el companero de SQLite en un hilo secundario y lo muestra. */
    private void cargarCompanero() {
        AppDatabase.ejecutar(() -> {
            RoomMateDB db = new RoomMateDB(this);
            Companero leido = db.obtenerCompanero(companeroId);
            db.close();

            runOnUiThread(() -> {
                if (leido == null) {
                    // Se ha borrado mientras tanto: se vuelve al listado.
                    finish();
                    return;
                }
                companero = leido;
                mostrarInformacion();
            });
        });
    }

    /** Pinta la foto, el nombre y el resto de datos del companero. */
    private void mostrarInformacion() {
        // Solo el dueno del perfil puede editarlo o borrarlo.
        int visibilidad = companero.esDe(usuarioId) ? View.VISIBLE : View.GONE;
        btnEditar.setVisibility(visibilidad);
        btnBorrar.setVisibility(visibilidad);

        FotoPerfilUtils.mostrarFotoPerfil(ivFoto, companero.getFotoPerfil());
        tvNombre.setText(companero.getNombreCompleto());

        String fecha = valorOTexto(companero.getFechaEntrada(), R.string.sin_fecha);
        String telefono = valorOTexto(companero.getTelefono(), R.string.sin_telefono);
        String correo = valorOTexto(companero.getCorreo(), R.string.sin_correo);
        String introduccion = valorOTexto(companero.getIntroduccion(), R.string.sin_introduccion);

        // Plantilla con todos los datos en strings.xml (traducible).
        tvInformacion.setText(getString(R.string.ficha_companero_info,
                companero.getNombreCompleto(),
                companero.getSexo(),
                companero.getEdad(),
                fecha,
                telefono,
                correo,
                introduccion));
    }

    /** Devuelve el valor, o el texto indicado si esta vacio. */
    private String valorOTexto(String valor, int textoSiVacio) {
        return (valor == null || valor.trim().isEmpty()) ? getString(textoSiVacio) : valor;
    }

    /** Borra el companero (ya confirmado) y su foto, y vuelve al listado. */
    private void borrarCompanero() {
        AppDatabase.ejecutar(() -> {
            RoomMateDB db = new RoomMateDB(this);
            int borrados = db.borrarCompanero(companeroId);
            db.close();

            runOnUiThread(() -> {
                if (borrados > 0) {
                    if (companero != null) {
                        FotoPerfilUtils.borrarFotoPerfil(companero.getFotoPerfil());
                        Toast.makeText(this,
                                getString(R.string.companero_eliminado, companero.getNombreCompleto()),
                                Toast.LENGTH_SHORT).show();
                    }
                    finish();
                } else {
                    Toast.makeText(this, R.string.error_borrar_companero, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
