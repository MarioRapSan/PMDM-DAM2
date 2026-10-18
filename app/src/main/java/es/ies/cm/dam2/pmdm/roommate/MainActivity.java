package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etNombre;
    private EditText etEdad;
    private EditText etIntroduccion;
    private RadioGroup rgSexo;
    private RadioGroup rgLgtbFriendly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Referencias a los campos
        etNombre = findViewById(R.id.etNombre);
        etEdad = findViewById(R.id.etEdad);
        etIntroduccion = findViewById(R.id.etIntroduccion);
        rgSexo = findViewById(R.id.rgSexo);
        rgLgtbFriendly = findViewById(R.id.rgLgtbFriendly);

        Button btnCheck = findViewById(R.id.btnCheck);
        Button btnSiguiente = findViewById(R.id.btnSiguiente);

        // Validar y enviar los datos
        btnCheck.setOnClickListener(v -> enviarFormulario());

        // Mantener la navegación existente
        btnSiguiente.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    FichaCompaneroActivity.class
            );
            startActivity(intent);
        });

        Button btnInvitacion = findViewById(R.id.btnInvitacion);
        Button btnCorreo = findViewById(R.id.btnCorreo);

        btnInvitacion.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    InvitacionActivity.class
            );
            startActivity(intent);
        });

        btnCorreo.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    CorreoActivity.class
            );
            startActivity(intent);
        });

    }


    private void enviarFormulario() {

        String nombre = etNombre.getText().toString().trim();
        String edadTexto = etEdad.getText().toString().trim();
        String introduccion =
                etIntroduccion.getText().toString().trim();

        int sexoId = rgSexo.getCheckedRadioButtonId();
        int lgtbId = rgLgtbFriendly.getCheckedRadioButtonId();

        // Todos los campos obligatorios deben estar respondidos
        if (nombre.isEmpty()
                || edadTexto.isEmpty()
                || sexoId == -1
                || lgtbId == -1) {
            mostrarError();
            return;
        }

        // Comprobar que la edad es válida
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

        // Obtener las respuestas seleccionadas
        RadioButton radioSexo = findViewById(sexoId);
        String sexo = radioSexo.getText().toString();

        boolean lgtbFriendly = (lgtbId == R.id.rbLgtbSi);


        Companero nuevo = new Companero(
                nombre,
                sexo,
                edad,
                lgtbFriendly,
                introduccion
        );

        CompaneroRepository.agregar(nuevo);


        etNombre.setText("");
        etEdad.setText("");
        etIntroduccion.setText("");
        rgSexo.clearCheck();
        rgLgtbFriendly.clearCheck();

        Toast.makeText(
                MainActivity.this,
                "Compañero registrado. Puedes introducir otro.",
                Toast.LENGTH_SHORT
        ).show();

    }
    private void mostrarError() {
        Toast.makeText(
                MainActivity.this,
                "Datos incorrectos",
                Toast.LENGTH_SHORT
        ).show();
    }
}

