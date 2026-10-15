package es.ies.cm.dam2.pmdm.roommate;


import static es.ies.cm.dam2.pmdm.roommate.R.id.txtNombre;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CompaneroActivity extends AppCompatActivity {
    String nombre ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_companero);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button btnReseteo = findViewById(R.id.btnReseteo);
        TextView label = findViewById(R.id.txtNombre);
        btnReseteo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                label.setText("");
            }
        });
        Button btn = findViewById(R.id.btnConfRech);

        btn.setOnClickListener(v -> {
            Intent intent = new Intent(CompaneroActivity.this, ConfirmarAsistenciaActivity.class);
            nombre = label.getText().toString();
            startActivity(intent);
        });
    }
}