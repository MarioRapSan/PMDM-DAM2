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
 * Pantalla de inicio de sesion.
 * Comprueba el usuario y la clave en la base de datos.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario;
    private EditText etClave;
    private RoomMateDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        aplicarInsets();

        db = new RoomMateDB(this);

        etUsuario = findViewById(R.id.etUsuarioLogin);
        etClave = findViewById(R.id.etClaveLogin);

        Button btnEntrar = findViewById(R.id.btnEntrar);
        Button btnAlta = findViewById(R.id.btnIrAlta);

        btnEntrar.setOnClickListener(v -> entrar());
        btnAlta.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, 1);
            intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_NOMBRE, "admin");
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

    private void entrar() {
        String nombre = etUsuario.getText().toString().trim();
        String clave = etClave.getText().toString().trim();

        if (nombre.isEmpty() || clave.isEmpty()) {
            Toast.makeText(
                    this,
                    R.string.error_campos_login,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Usuario usuario = db.validarUsuario(nombre, clave);

        if (usuario == null) {
            Toast.makeText(
                    this,
                    R.string.error_usuario_clave,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Intent intent = new Intent(this, ColeccionTareasActivity.class);
        intent.putExtra(
                ColeccionTareasActivity.EXTRA_USUARIO_ID,
                usuario.getId()
        );
        intent.putExtra(
                ColeccionTareasActivity.EXTRA_USUARIO_NOMBRE,
                usuario.getNombre()
        );
        startActivity(intent);
        finish();
    }

    private void aplicarInsets() {
        View root = findViewById(R.id.main);
        int base = (int) (24 * getResources()
                .getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );
            v.setPadding(
                    base + bars.left,
                    base + bars.top,
                    base + bars.right,
                    base + bars.bottom
            );
            return insets;
        });
    }
}
