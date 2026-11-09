package es.ies.cm.dam2.pmdm.roommate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import es.ies.cm.dam2.pmdm.roommate.db.Tarea;

public class DetalleTareaActivity extends AppCompatActivity {

    public static final String EXTRA_TAREA = "extra_tarea_detalle";

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_tarea);
        aplicarInsets();

        Tarea tarea = (Tarea) getIntent()
                .getSerializableExtra(EXTRA_TAREA);

        if (tarea == null) {
            Toast.makeText(
                    this,
                    "No se ha podido cargar la tarea",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        ImageView foto = findViewById(R.id.ivFotoDetalle);
        TextView nombre = findViewById(R.id.tvNombreDetalle);
        TextView companero = findViewById(R.id.tvCompaneroDetalle);
        TextView prioridad = findViewById(R.id.tvPrioridadDetalle);
        TextView fecha = findViewById(R.id.tvFechaDetalle);
        TextView descripcion = findViewById(R.id.tvDescripcionDetalle);
        RatingBar dificultad = findViewById(R.id.rbDificultadDetalle);
        Button btnVolver = findViewById(R.id.btnVolverDetalle);

        nombre.setText(tarea.getNombre());

        String nombreCompanero = tarea.getCompanero();
        if (nombreCompanero == null || nombreCompanero.trim().isEmpty()) {
            nombreCompanero = "Sin asignar";
        }
        companero.setText("Añadida por: " + nombreCompanero);

        prioridad.setText("Prioridad: " + tarea.getPrioridad());
        String dia = tarea.getDia();
        String hora = tarea.getHora();
        String textoFecha = (dia == null || dia.trim().isEmpty())
                ? "Sin fecha"
                : dia;

        if (hora != null && !hora.trim().isEmpty()) {
            textoFecha += " · " + hora;
        }
        fecha.setText("Fecha: " + textoFecha);

        String textoDescripcion = tarea.getDescripcion();
        if (textoDescripcion == null || textoDescripcion.trim().isEmpty()) {
            textoDescripcion = getString(R.string.sin_descripcion);
        }
        descripcion.setText(
                getString(R.string.descripcion_detalle) + ": " + textoDescripcion
        );

        dificultad.setRating(tarea.getDificultad());

        btnVolver.setOnClickListener(v -> finish());
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
