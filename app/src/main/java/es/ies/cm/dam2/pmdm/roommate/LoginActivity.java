package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Pantalla de inicio de sesion (entrada de la app).
 *
 * Comprueba el usuario y la clave contra la tabla usuarios de SQLite.
 * Si son correctos, abre la coleccion de tareas pasando el id y el nombre
 * del usuario por el Intent, para que cada usuario vea su propia lista de la compra.
 * Desde aqui tambien se puede crear una cuenta nueva (RegistroUsuarioActivity).
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario;
    private EditText etClave;
    private Button btnEntrar;

    /** Recibe el nombre del usuario recien creado para rellenar el campo. */
    private ActivityResultLauncher<Intent> lanzadorRegistro;

    /** Enlaza los campos y los botones Entrar y Registrarse. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        aplicarInsets();

        etUsuario = findViewById(R.id.etUsuarioLogin);
        etClave = findViewById(R.id.etClaveLogin);
        btnEntrar = findViewById(R.id.btnEntrar);
        Button btnRegistrarse = findViewById(R.id.btnRegistrarse);

        lanzadorRegistro = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                resultado -> {
                    if (resultado.getResultCode() == RESULT_OK && resultado.getData() != null) {
                        String nombre = resultado.getData()
                                .getStringExtra(RegistroUsuarioActivity.EXTRA_NOMBRE_CREADO);
                        etUsuario.setText(nombre);
                        etClave.setText("");
                        etClave.requestFocus();
                    }
                }
        );

        btnEntrar.setOnClickListener(v -> entrar());
        btnRegistrarse.setOnClickListener(v ->
                lanzadorRegistro.launch(new Intent(this, RegistroUsuarioActivity.class)));
    }

    /** Valida los campos y consulta la base de datos fuera del hilo principal. */
    private void entrar() {
        String nombre = etUsuario.getText().toString().trim();
        String clave = etClave.getText().toString().trim();

        if (nombre.isEmpty() || clave.isEmpty()) {
            Toast.makeText(this, R.string.error_campos_login, Toast.LENGTH_SHORT).show();
            return;
        }

        btnEntrar.setEnabled(false);

        AppDatabase.ejecutar(() -> {
            RoomMateDB db = new RoomMateDB(this);
            Usuario usuario = db.validarUsuario(nombre, clave);
            db.close();

            runOnUiThread(() -> {
                btnEntrar.setEnabled(true);

                if (usuario == null) {
                    // Credenciales incorrectas: se avisa y se limpia la clave.
                    etClave.setText("");
                    Toast.makeText(this, R.string.error_usuario_clave, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Credenciales correctas: el id del usuario viaja a la siguiente pantalla.
                Intent intent = new Intent(this, ColeccionTareasActivity.class);
                intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, usuario.getId());
                intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_NOMBRE, usuario.getNombre());
                startActivity(intent);
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
