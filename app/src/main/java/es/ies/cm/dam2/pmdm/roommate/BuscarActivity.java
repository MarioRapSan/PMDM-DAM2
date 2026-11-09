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
    private RoomMateDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buscar);
        aplicarInsets();

        db = new RoomMateDB(this);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

    private void buscar() {
        String texto = etNombre.getText().toString().trim();
        String modo = spinnerModo.getSelectedItem().toString();
        String prioridad = spinnerPrioridad.getSelectedItem().toString();

        resultados.clear();
        resultados.addAll(db.buscarTareas(texto, modo, prioridad));
        adapter.notifyDataSetChanged();
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
