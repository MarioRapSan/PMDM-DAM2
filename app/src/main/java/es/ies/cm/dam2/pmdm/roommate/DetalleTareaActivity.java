package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

/**
 * Ficha (detalle) de una tarea.
 *
 * Recibe solo el id de la tarea y la lee de Room cada vez que se muestra, asi
 * nunca ensena datos obsoletos (por ejemplo, despues de editarla).
 * Tambien muestra sus registros (entidad relacionada 1-N), permite editarla y
 * abre la pantalla de registros ya filtrada por esta tarea.
 */
public class DetalleTareaActivity extends AppCompatActivity {

    /** Extra con el id de la tarea a mostrar. */
    public static final String EXTRA_TAREA_ID = "extra_tarea_id";

    private int tareaId;
    private Tarea tareaActual;

    private ImageView foto;
    private TextView tvNombre, tvCompanero, tvPrioridad, tvFecha;
    private TextView tvCompartida, tvDescripcion, tvRegistros;
    private RatingBar rbDificultad;

    /** Abre el formulario de edicion; al guardar se vuelve al listado. */
    private ActivityResultLauncher<Intent> lanzadorEdicion;

    /** Recoge el id de la tarea, enlaza las vistas y prepara los botones Editar y Volver. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_tarea);
        aplicarInsets();

        tareaId = getIntent().getIntExtra(EXTRA_TAREA_ID, -1);
        if (tareaId == -1) {
            Toast.makeText(this, R.string.error_cargar_tarea, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        foto = findViewById(R.id.ivFotoDetalle);
        tvNombre = findViewById(R.id.tvNombreDetalle);
        tvCompanero = findViewById(R.id.tvCompaneroDetalle);
        tvPrioridad = findViewById(R.id.tvPrioridadDetalle);
        tvFecha = findViewById(R.id.tvFechaDetalle);
        tvCompartida = findViewById(R.id.tvCompartidaDetalle);
        tvDescripcion = findViewById(R.id.tvDescripcionDetalle);
        tvRegistros = findViewById(R.id.tvRegistrosDetalle);
        rbDificultad = findViewById(R.id.rbDificultadDetalle);

        lanzadorEdicion = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                resultado -> {
                    // Tras guardar la edicion se vuelve al listado, que se refresca solo.
                    if (resultado.getResultCode() == RESULT_OK) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });

        Button btnEditar = findViewById(R.id.btnEditarDetalle);
        Button btnVolver = findViewById(R.id.btnVolverDetalle);

        btnEditar.setOnClickListener(v -> {
            if (tareaActual != null) {
                Intent intent = new Intent(this, AnadirTareasActivity.class);
                intent.putExtra(AnadirTareasActivity.EXTRA_TAREA_EDITAR, tareaActual);
                lanzadorEdicion.launch(intent);
            }
        });
        // Registros de esta tarea (relacion 1-N): se abren ya filtrados por ella.
        Button btnRegistros = findViewById(R.id.btnRegistrosDetalle);
        btnRegistros.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegistroTareaActivity.class);
            intent.putExtra(RegistroTareaActivity.EXTRA_TAREA_ID, tareaId);
            startActivity(intent);
        });

        btnVolver.setOnClickListener(v -> finish());
    }

    /** Se recarga al volver (por ejemplo, despues de editar) para no mostrar datos viejos. */
    @Override
    protected void onResume() {
        super.onResume();
        cargarTarea();
    }

    /** Lee la tarea y sus registros de Room en un hilo secundario. */
    private void cargarTarea() {
        AppDatabase.ejecutar(() -> {
            AppDatabase bd = AppDatabase.getInstance(this);
            Tarea tarea = bd.tareaDao().obtenerPorId(tareaId);
            List<RegistroTarea> registros = bd.registroTareaDao().obtenerPorTarea(tareaId);

            runOnUiThread(() -> {
                if (tarea == null) {
                    // La tarea se ha borrado mientras tanto.
                    Toast.makeText(this, R.string.error_cargar_tarea, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                tareaActual = tarea;
                mostrar(tarea, registros);
            });
        });
    }

    /** Pinta los datos de la tarea y la lista de sus registros. */
    private void mostrar(Tarea tarea, List<RegistroTarea> registros) {
        foto.setImageResource(R.drawable.ic_persona);
        tvNombre.setText(tarea.getNombre());

        String companero = tarea.getCompanero();
        if (companero == null || companero.trim().isEmpty()) {
            companero = getString(R.string.sin_asignar);
        }
        tvCompanero.setText(getString(R.string.anadida_por, companero));
        tvPrioridad.setText(getString(R.string.prioridad_valor,
                Prioridades.etiqueta(this, tarea.getPrioridad())));

        String textoFecha = (tarea.getDia() == null || tarea.getDia().trim().isEmpty())
                ? getString(R.string.sin_fecha)
                : tarea.getDia();
        if (tarea.getHora() != null && !tarea.getHora().trim().isEmpty()) {
            textoFecha += " · " + tarea.getHora();
        }
        tvFecha.setText(getString(R.string.fecha_valor, textoFecha));

        tvCompartida.setText(tarea.isCompartida()
                ? R.string.tarea_compartida_si
                : R.string.tarea_compartida_no);

        String descripcion = tarea.getDescripcion();
        if (descripcion == null || descripcion.trim().isEmpty()) {
            descripcion = getString(R.string.sin_descripcion);
        }
        tvDescripcion.setText(getString(R.string.descripcion_valor, descripcion));

        rbDificultad.setRating(tarea.getDificultad());

        // Lista de registros de esta tarea (relacion 1-N).
        StringBuilder texto = new StringBuilder(
                getString(R.string.registros_titulo, registros.size()));
        if (registros.isEmpty()) {
            texto.append("\n").append(getString(R.string.sin_registros));
        }
        for (RegistroTarea r : registros) {
            texto.append("\n").append(getString(R.string.linea_registro,
                    r.getFecha(), r.getPuntuacion(), r.getGanador()));
        }
        tvRegistros.setText(texto.toString());
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
