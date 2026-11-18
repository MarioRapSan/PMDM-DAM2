package es.ies.cm.dam2.pmdm.roommate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * Ajustes de la cuenta del usuario que ha iniciado sesion.
 *
 * Completa el CRUD de la tabla usuarios:
 *  - UPDATE: cambiar la clave (comprobando antes la clave actual).
 *  - DELETE: darse de baja, con confirmacion en un DialogFragment.
 * Tambien permite cerrar la sesion, volver al login y cambiar entre
 * el tema claro y el oscuro (Switch "Modo oscuro").
 */
public class AjustesActivity extends AppCompatActivity {

    /** Clave con la que el dialogo de confirmacion devuelve la respuesta. */
    private static final String CLAVE_CONFIRMAR_BAJA = "confirmar_baja";

    private int usuarioId;
    private String usuarioNombre;

    private EditText etClaveActual;
    private EditText etClaveNueva;

    /** Prepara la pantalla: usuario actual, tema, cambio de clave, cierre de sesion y baja. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        MaterialToolbar toolbar = findViewById(R.id.toolbarAjustes);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.ajustes);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Usuario que ha iniciado sesion (llega desde el menu lateral de la coleccion).
        usuarioId = getIntent().getIntExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, -1);
        usuarioNombre = getIntent().getStringExtra(ColeccionTareasActivity.EXTRA_USUARIO_NOMBRE);

        // Sin usuario no tiene sentido esta pantalla: se vuelve al login.
        if (usuarioId == -1 || usuarioNombre == null) {
            irAlLogin(this);
            return;
        }

        TextView tvUsuario = findViewById(R.id.tvUsuarioAjustes);
        tvUsuario.setText(getString(R.string.sesion_iniciada_como, usuarioNombre));

        etClaveActual = findViewById(R.id.etClaveActual);
        etClaveNueva = findViewById(R.id.etClaveNueva);

        Button btnCambiar = findViewById(R.id.btnCambiarClave);
        Button btnCerrar = findViewById(R.id.btnCerrarSesion);
        Button btnBaja = findViewById(R.id.btnDarseDeBaja);

        // Switch del tema: primero se marca el estado guardado y despues se pone
        // el listener, para que al recrearse la pantalla no se dispare solo.
        Switch swModoOscuro = findViewById(R.id.swModoOscuro);
        swModoOscuro.setChecked(Tema.esOscuro(this));
        swModoOscuro.setOnCheckedChangeListener((boton, activado) ->
                Tema.cambiar(this, activado));

        btnCambiar.setOnClickListener(v -> cambiarClave());
        btnCerrar.setOnClickListener(v -> irAlLogin(this));
        btnBaja.setOnClickListener(v ->
                ConfirmacionDialogFragment.nuevo(
                        CLAVE_CONFIRMAR_BAJA,
                        R.string.darse_de_baja,
                        R.string.mensaje_confirmar_baja,
                        R.string.darse_de_baja
                ).show(getSupportFragmentManager(), "dialogo_baja"));

        // Respuesta del dialogo de confirmacion (se registra aqui para que
        // siga funcionando aunque se gire la pantalla con el dialogo abierto).
        getSupportFragmentManager().setFragmentResultListener(
                CLAVE_CONFIRMAR_BAJA, this, (clave, resultado) -> {
                    if (resultado.getBoolean(ConfirmacionDialogFragment.RESULTADO_CONFIRMADO)) {
                        darseDeBaja();
                    }
                });

        cargarResumen();
    }

    /** Muestra cuantos articulos tiene el usuario en su lista personal. */
    private void cargarResumen() {
        TextView tvResumen = findViewById(R.id.tvResumenAjustes);
        AppDatabase.ejecutar(() -> {
            RoomMateDB db = new RoomMateDB(this);
            int total = db.contarListaCompra(usuarioId);
            db.close();
            runOnUiThread(() ->
                    tvResumen.setText(getString(R.string.resumen_lista_compra, total)));
        });
    }

    /** Comprueba la clave actual y guarda la nueva. */
    private void cambiarClave() {
        String actual = etClaveActual.getText().toString().trim();
        String nueva = etClaveNueva.getText().toString().trim();

        if (actual.isEmpty()) {
            etClaveActual.setError(getString(R.string.error_clave_vacia));
            return;
        }
        if (nueva.length() < 4) {
            etClaveNueva.setError(getString(R.string.error_clave_corta, 4));
            return;
        }

        AppDatabase.ejecutar(() -> {
            RoomMateDB db = new RoomMateDB(this);
            boolean claveCorrecta = db.validarUsuario(usuarioNombre, actual) != null;
            int cambiados = claveCorrecta ? db.cambiarClave(usuarioId, nueva) : 0;
            db.close();

            runOnUiThread(() -> {
                if (!claveCorrecta) {
                    etClaveActual.setError(getString(R.string.error_clave_actual));
                    return;
                }
                if (cambiados == 1) {
                    etClaveActual.setText("");
                    etClaveNueva.setText("");
                    Toast.makeText(this, R.string.clave_cambiada, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /** Borra el usuario (y en cascada su lista de la compra) y vuelve al login. */
    private void darseDeBaja() {
        AppDatabase.ejecutar(() -> {
            RoomMateDB db = new RoomMateDB(this);
            int borrados = db.borrarUsuario(usuarioId);
            db.close();

            runOnUiThread(() -> {
                if (borrados == 1) {
                    Toast.makeText(this, R.string.cuenta_borrada, Toast.LENGTH_LONG).show();
                    irAlLogin(this);
                }
            });
        });
    }

    /**
     * Vuelve al login borrando todas las pantallas anteriores, para que con
     * el boton Atras no se pueda volver a entrar sin identificarse.
     */
    public static void irAlLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    /** La flecha de la barra superior cierra la pantalla y vuelve a la anterior. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
