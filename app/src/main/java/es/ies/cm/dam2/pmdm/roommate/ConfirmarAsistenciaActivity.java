
package es.ies.cm.dam2.pmdm.roommate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ConfirmarAsistenciaActivity extends AppCompatActivity {

    public static final String EXTRA_ASISTENCIA = "extra_asistencia";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmar_asistencia);

        Button btnAceptar = findViewById(R.id.btnAceptar);
        Button btnCancelar = findViewById(R.id.btnCancelar);

        btnAceptar.setOnClickListener(v -> devolverResultado(true));

        btnCancelar.setOnClickListener(v -> devolverResultado(false));
    }

    private void devolverResultado(boolean aceptada) {
        Intent data = new Intent();
        data.putExtra(EXTRA_ASISTENCIA, aceptada);

        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
