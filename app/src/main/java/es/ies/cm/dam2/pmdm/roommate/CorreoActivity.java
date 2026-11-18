
package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Envia una invitacion por correo usando un Intent implicito (ACTION_SENDTO).
 * Comprueba que los campos estan rellenos y que hay una app de correo instalada.
 */
public class CorreoActivity extends AppCompatActivity {

    /** Extra opcional con el correo del destinatario. */
    public static final String EXTRA_DESTINATARIO = "extra_destinatario";

    /** Valida los campos y abre la app de correo con el destinatario, el asunto y el mensaje. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correo);

        EditText etDestinatario = findViewById(R.id.etDestinatario);
        EditText etAsunto = findViewById(R.id.etAsunto);
        EditText etMensaje = findViewById(R.id.etMensaje);

        // Si se abre desde la ficha de un companero, su correo ya viene escrito.
        String destinatarioRecibido = getIntent().getStringExtra(EXTRA_DESTINATARIO);
        if (savedInstanceState == null && destinatarioRecibido != null) {
            etDestinatario.setText(destinatarioRecibido);
        }

        Button btnEnviarCorreo = findViewById(R.id.btnEnviarCorreo);

        btnEnviarCorreo.setOnClickListener(v -> {

            String destinatario = etDestinatario.getText()
                    .toString().trim();

            String asunto = etAsunto.getText()
                    .toString().trim();

            String mensaje = etMensaje.getText()
                    .toString().trim();

            if (destinatario.isEmpty()
                    || asunto.isEmpty()
                    || mensaje.isEmpty()) {

                Toast.makeText(
                        this,
                        R.string.error_rellena_campos,
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Intent intent = new Intent(
                    Intent.ACTION_SENDTO
            );

            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{destinatario});
            intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
            intent.putExtra(Intent.EXTRA_TEXT, mensaje);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(
                        this,
                        R.string.error_sin_app_correo,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
        Button btnVolver = findViewById(R.id.btnVolver);

        btnVolver.setOnClickListener(v -> finish());
    }
}
