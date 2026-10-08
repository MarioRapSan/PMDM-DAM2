package es.ies.cm.dam2.pmdm.roommate;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ControlesBasicos2 extends AppCompatActivity {

    double alquiler = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_controles_basicos2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });


        RadioButton radioEntero = findViewById(R.id.radioEntero);
        RadioButton radioCompartido = findViewById(R.id.radioCompartido);
        RadioButton radioOpcCompra = findViewById(R.id.radioOpcCompra);

        Button btnLimpiar = findViewById(R.id.btnLimpiar);

        View main = findViewById(R.id.main);


        // Fondo inicial blanco
        main.setBackgroundColor(
                getColor(R.color.surface)
        );


        // Entero
        radioEntero.setOnClickListener(v -> {
            main.setBackgroundColor(
                    getColor(R.color.success)
            );
        });


        // Compartido
        radioCompartido.setOnClickListener(v -> {
            main.setBackgroundColor(
                    getColor(R.color.warning)
            );
        });


        // Opción de compra
        radioOpcCompra.setOnClickListener(v -> {
            main.setBackgroundColor(
                    getColor(R.color.error)
            );
        });


        // Botón Limpiar
        btnLimpiar.setOnClickListener(v -> {

            radioEntero.setChecked(false);
            radioCompartido.setChecked(false);
            radioOpcCompra.setChecked(false);

            main.setBackgroundColor(
                    getColor(R.color.surface)
            );
        });


        TextView textView = findViewById(R.id.textView);

        View.OnClickListener listener = v -> {
            Button boton = (Button) v;
            textView.setText(boton.getText());
        };

        findViewById(R.id.btn1).setOnClickListener(listener);
        findViewById(R.id.btn2).setOnClickListener(listener);
        findViewById(R.id.btn3).setOnClickListener(listener);
        findViewById(R.id.btn4).setOnClickListener(listener);
        findViewById(R.id.btn5).setOnClickListener(listener);
        findViewById(R.id.btn6).setOnClickListener(listener);
        findViewById(R.id.btn7).setOnClickListener(listener);
        findViewById(R.id.btn8).setOnClickListener(listener);
        findViewById(R.id.btn9).setOnClickListener(listener);

        EditText txtAlquiler = findViewById(R.id.txtAlquiler);
        TextView textViewAlquiler = findViewById(R.id.textViewAlquiler);

        txtAlquiler.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                if (s.toString().isEmpty()) {

                    textViewAlquiler.setTextColor(
                            getColor(R.color.text_primary)
                    );

                    textViewAlquiler.setText(
                            R.string.introduce_un_alquiler2
                    );

                    return;
                }

                alquiler = Double.parseDouble(s.toString());

                if (alquiler < 100 || alquiler > 2500) {

                    textViewAlquiler.setTextColor(
                            getColor(R.color.error)
                    );

                    textViewAlquiler.setText(
                            R.string.alquiler_fuera_de_rango
                    );

                } else {

                    textViewAlquiler.setTextColor(
                            getColor(R.color.success)
                    );

                    textViewAlquiler.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}