package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Pantalla de alta de usuario (registro).
 *
 * Crea un usuario nuevo en la tabla usuarios de SQLite. Si todo va bien vuelve
 * al login devolviendo el nombre para que el usuario solo tenga que escribir la clave.
 */
public class RegistroUsuarioActivity extends AppCompatActivity {

    /** Extra con el nombre del usuario creado, que se devuelve al login. */
    public static final String EXTRA_NOMBRE_CREADO = "extra_nombre_creado";

    /** Longitud minima de la clave. */
    private static final int MIN_CLAVE = 4;

    private EditText etUsuario;
    private EditText etClave;
    private EditText etClaveRepetida;
    private Button btnCrear;

    /** Enlaza los campos y los botones Registrarse y Cancelar. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_usuario);
        aplicarInsets();

        etUsuario = findViewById(R.id.etUsuarioRegistro);
        etClave = findViewById(R.id.etClaveRegistro);
        etClaveRepetida = findViewById(R.id.etClaveRepetidaRegistro);
        btnCrear = findViewById(R.id.btnCrearCuenta);
        Button btnCancelar = findViewById(R.id.btnCancelarRegistro);

        btnCrear.setOnClickListener(v -> registrar());
        // Cancelar vuelve al login sin crear nada.
        btnCancelar.setOnClickListener(v -> finish());
    }

    /** Valida el formulario y guarda el usuario en un hilo secundario. */
    private void registrar() {
        String nombre = etUsuario.getText().toString().trim();
        String clave = etClave.getText().toString().trim();
        String repetida = etClaveRepetida.getText().toString().trim();

        if (nombre.isEmpty()) {
            etUsuario.setError(getString(R.string.error_usuario_vacio));
            etUsuario.requestFocus();
            return;
        }
        if (clave.length() < MIN_CLAVE) {
            etClave.setError(getString(R.string.error_clave_corta, MIN_CLAVE));
            etClave.requestFocus();
            return;
        }
        if (!clave.equals(repetida)) {
            etClaveRepetida.setError(getString(R.string.error_claves_distintas));
            etClaveRepetida.requestFocus();
            return;
        }

        // Evita dobles pulsaciones mientras se guarda.
        btnCrear.setEnabled(false);

        AppDatabase.ejecutar(() -> {
            RoomMateDB db = new RoomMateDB(this);
            long id = db.registrarUsuario(nombre, clave);
            db.close();

            runOnUiThread(() -> {
                btnCrear.setEnabled(true);

                if (id == -1) {
                    // La columna nombre es UNIQUE: ese usuario ya existe.
                    etUsuario.setError(getString(R.string.error_usuario_existe));
                    etUsuario.requestFocus();
                    return;
                }

                Toast.makeText(this, R.string.cuenta_creada, Toast.LENGTH_SHORT).show();
                Intent datos = new Intent();
                datos.putExtra(EXTRA_NOMBRE_CREADO, nombre);
                setResult(RESULT_OK, datos);
                finish();
            });
        });
    }

    /** Anade al padding el espacio de las barras del sistema (modo edge-to-edge). */
    private void aplicarInsets() {
        View root = findViewById(R.id.main);
        int base = (int) (24 * getResources().getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(base + bars.left, base + bars.top,
                    base + bars.right, base + bars.bottom);
            return insets;
        });
    }
}
