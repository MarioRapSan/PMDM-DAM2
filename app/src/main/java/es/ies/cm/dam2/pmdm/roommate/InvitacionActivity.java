
package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class InvitacionActivity extends AppCompatActivity {

    private EditText etNombreInvitado;
    private TextView tvResultado;

    private String nombre;

    private final ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    resultado -> {
                        if (resultado.getResultCode()
                                == RESULT_OK
                                && resultado.getData() != null) {

                            boolean aceptada = resultado.getData()
                                    .getBooleanExtra(
                                            ConfirmarAsistenciaActivity.EXTRA_ASISTENCIA,
                                            false
                                    );

                            String estado = aceptada
                                    ? "ACEPTADO"
                                    : "CANCELADO";

                            String mensaje = "De acuerdo "
                                    + nombre
                                    + ", usted ha "
                                    + estado
                                    + " la asistencia.";

                            tvResultado.setText(mensaje);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitacion);

        etNombreInvitado = findViewById(R.id.etNombreInvitado);
        tvResultado = findViewById(R.id.tvResultado);

        Button btnConfirmar = findViewById(R.id.btnConfirmar);

        btnConfirmar.setOnClickListener(v -> {
            nombre = etNombreInvitado.getText()
                    .toString().trim();

            if (nombre.isEmpty()) {
                Toast.makeText(
                        this,
                        "Introduce el nombre del compañero",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Intent intent = new Intent(
                    InvitacionActivity.this,
                    ConfirmarAsistenciaActivity.class
            );

            launcher.launch(intent);
        });
        Button btnVolver = findViewById(R.id.btnVolver);

        btnVolver.setOnClickListener(v -> finish());
    }
}
