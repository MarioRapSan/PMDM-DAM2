package es.ies.cm.dam2.pmdm.roommate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ControlesBasicos extends AppCompatActivity {
    int companeros = 0;
    String nombre = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_controles_basicos);
        Button btnSumar = findViewById(R.id.btnSumar);
        Button btnRestar = findViewById(R.id.btnRestar);
        Button btnAtras = findViewById(R.id.btnAtras);
        Button btnAdelante = findViewById(R.id.btnAdelante);
        Button btnActualizar = findViewById(R.id.btnActualizar);
        CheckBox checkBx = findViewById(R.id.checkBx);
        Button btnActivar = findViewById(R.id.btnActivar);
        btnSumar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                companeros++;
                TextView txtView = findViewById(R.id.txtView);
                txtView.setText(companeros);
            }
        });
        TextView txtView = findViewById(R.id.txtView);

        btnRestar.setOnClickListener(v -> {
            if (companeros > 0) {
                companeros--;
                txtView.setText(
                        getString(R.string.bot_n_restar_pulsado_n_mero_de_compa_eros) + companeros
                );
            }
        });

        btnSumar.setOnClickListener(v -> {
            companeros++;
            txtView.setText("Botón sumar pulsado, número de compañeros: " + companeros);
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
        btnAtras = findViewById(R.id.btnAtras);

        btnAtras.setOnClickListener(v -> {
            Intent intent = new Intent(ControlesBasicos.this, MainActivity.class);
            startActivity(intent);
        });
        btnActualizar = findViewById(R.id.btnActualizar);
        TextView editTxt = findViewById(R.id.txtAlquiler);
        TextView txtViewNombreUsuario = findViewById(R.id.txtViewNombreUsuario);
        btnActualizar.setOnClickListener(v -> {
            nombre = String.valueOf(editTxt.getText());
            txtViewNombreUsuario.setText(nombre);
        });
        checkBx = findViewById(R.id.checkBx);
        btnActivar.setEnabled(false);
        checkBx.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnActivar.setEnabled(!isChecked);
                });

        TextView txtViewTonteria = findViewById(R.id.txtViewTonteria);
        btnActivar.setOnClickListener(v -> {
            txtViewTonteria.setText(R.string.bot_n_pulsado);
        });
        Button boton = findViewById(R.id.btnAdelante);

        boton.setOnClickListener(v -> {
            Intent intent = new Intent(ControlesBasicos.this, ControlesBasicos2.class);
            startActivity(intent);
        });
    }
}