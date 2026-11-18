package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Busca tareas por nombre y prioridad.
 */
public class BuscarActivity extends AppCompatActivity {

    private EditText etNombre;
    private Spinner spinnerModo;
    private Spinner spinnerPrioridad;
    private ListView lista;
    private TextView tvSinResultados;

    private final List<Tarea> resultados = new ArrayList<>();
    private TareaBusquedaAdapter adapter;
    private TareaDao tareaDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buscar);
        aplicarInsets();

        tareaDao = AppDatabase.getInstance(this).tareaDao();

        etNombre = findViewById(R.id.etBuscarNombre);
        spinnerModo = findViewById(R.id.spinnerModoBusqueda);
        spinnerPrioridad = findViewById(R.id.spinnerPrioridadBusqueda);
        lista = findViewById(R.id.lvResultadosBusqueda);
        tvSinResultados = findViewById(R.id.tvSinResultados);

        ArrayAdapter<CharSequence> adapterModo =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.modos_busqueda,
                        R.layout.item_spinner_texto
                );
        adapterModo.setDropDownViewResource(R.layout.item_spinner_texto);
        spinnerModo.setAdapter(adapterModo);

        ArrayAdapter<CharSequence> adapterPrioridad =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.prioridades_busqueda,
                        R.layout.item_spinner_texto
                );
        adapterPrioridad.setDropDownViewResource(R.layout.item_spinner_texto);
        spinnerPrioridad.setAdapter(adapterPrioridad);

        adapter = new TareaBusquedaAdapter(this, resultados);
        lista.setAdapter(adapter);
        lista.setEmptyView(tvSinResultados);

        Button btnBuscar = findViewById(R.id.btnBuscarTareas);
        Button btnVolver = findViewById(R.id.btnVolverBusqueda);

        btnBuscar.setOnClickListener(v -> buscar());
        btnVolver.setOnClickListener(v -> finish());

        lista.setOnItemClickListener((parent, view, position, id) -> {
            Tarea tarea = resultados.get(position);
            Intent intent = new Intent(this, DetalleTareaActivity.class);
            intent.putExtra(DetalleTareaActivity.EXTRA_TAREA, tarea);
            startActivity(intent);
        });
    }

    private void buscar() {
        String texto = etNombre.getText().toString().trim();
        String modo = spinnerModo.getSelectedItem().toString();
        String prioridad = spinnerPrioridad.getSelectedItem().toString();

        AppDatabase.ejecutar(() -> {
            List<Tarea> encontradas = tareaDao.obtenerTodas();
            List<Tarea> filtradas = new ArrayList<>();
            String textoMinusculas = texto.toLowerCase();

            for (Tarea tarea : encontradas) {
                String nombre = tarea.getNombre().toLowerCase();
                boolean coincideNombre = modo.equals("Empieza por")
                        ? nombre.startsWith(textoMinusculas)
                        : nombre.contains(textoMinusculas);
                boolean coincidePrioridad = prioridad.equals("Todas")
                        || prioridad.equals(tarea.getPrioridad());

                if (coincideNombre && coincidePrioridad) {
                    filtradas.add(tarea);
                }
            }

            List<Tarea> resultadoFinal = filtradas;

            runOnUiThread(() -> {
                resultados.clear();
                resultados.addAll(resultadoFinal);
                adapter.notifyDataSetChanged();
            });
        });
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
