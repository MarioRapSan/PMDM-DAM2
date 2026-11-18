package es.ies.cm.dam2.pmdm.roommate;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista simple de todas las tareas guardadas en Room.
 */
public class ListaTareas extends AppCompatActivity {

    private TareaDao tareaDao;
    private ListView lvListaTareas;
    private TextView tvTotal;
    private ArrayAdapter<String> adapter;
    private final List<String> lineas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_tareas_room);

        MaterialToolbar toolbar = findViewById(R.id.toolbarListaTareas);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.lista_de_tareas);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tareaDao = AppDatabase.getInstance(this).tareaDao();

        lvListaTareas = findViewById(R.id.lvListaTareas);
        tvTotal = findViewById(R.id.tvTotalListaTareas);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                lineas
        );
        lvListaTareas.setAdapter(adapter);

        cargarTareas();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cargarTareas() {
        AppDatabase.ejecutar(() -> {
            List<Tarea> tareas = tareaDao.obtenerTodas();

            runOnUiThread(() -> {
                lineas.clear();

                for (Tarea tarea : tareas) {
                    lineas.add(
                            tarea.getNombre()
                                    + " - "
                                    + tarea.getPrioridad()
                                    + " - "
                                    + tarea.getDia()
                    );
                }

                adapter.notifyDataSetChanged();
                tvTotal.setText("Tareas guardadas: " + tareas.size());
            });
        });
    }
}
