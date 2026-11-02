package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ColeccionTareasActivity extends AppCompatActivity {

    private final List<Tarea> todasLasTareas = new ArrayList<>();
    private final List<Tarea> tareasVisibles = new ArrayList<>();
    private boolean mostrarHechas = false;

    private TareaRecyclerAdapter adaptador;
    private RecyclerView rvTareas;
    private Button btnMostrar;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_coleccion_tareas);
        aplicarInsets();

        rvTareas = findViewById(R.id.rvTareas);
        Button btnAnadir = findViewById(R.id.btnAnadirTarea);
        btnMostrar = findViewById(R.id.btnMostrarTareas);
        Button btnVolver = findViewById(R.id.btnVolverColeccion);

        rvTareas.setLayoutManager(new LinearLayoutManager(this));

        adaptador = new TareaRecyclerAdapter(
                tareasVisibles,
                this::abrirDetalleTarea,
                this::alCambiarEstado,
                this::eliminarTarea
        );
        rvTareas.setAdapter(adaptador);

        cargarTareasIniciales();

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK
                            && result.getData() != null) {

                        Tarea nueva = (Tarea) result.getData()
                                .getSerializableExtra(
                                        AnadirTareasActivity.EXTRA_TAREA
                                );

                        if (nueva != null) {
                            todasLasTareas.add(nueva);

                            // Si el filtro actual oculta la nueva tarea,
                            // cambiamos al estado de la tarea añadida.
                            if (nueva.isHecha() != mostrarHechas) {
                                mostrarHechas = nueva.isHecha();
                                btnMostrar.setText(
                                        mostrarHechas
                                                ? "Ver no hechas"
                                                : "Ver hechas"
                                );
                            }

                            aplicarFiltro();

                            if (!tareasVisibles.isEmpty()) {
                                rvTareas.scrollToPosition(
                                        tareasVisibles.size() - 1
                                );
                            }

                            Toast.makeText(
                                    this,
                                    "Tarea añadida",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                }
        );

        btnMostrar.setText("Ver hechas");
        btnMostrar.setOnClickListener(v -> {
            mostrarHechas = !mostrarHechas;
            aplicarFiltro();

            btnMostrar.setText(
                    mostrarHechas ? "Ver no hechas" : "Ver hechas"
            );

            String mensaje;
            if (tareasVisibles.isEmpty()) {
                mensaje = mostrarHechas
                        ? "No hay tareas hechas"
                        : "No hay tareas no hechas";
            } else {
                mensaje = mostrarHechas
                        ? "Mostrando tareas hechas"
                        : "Mostrando tareas no hechas";
            }

            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        });

        btnAnadir.setOnClickListener(v ->
                launcher.launch(
                        new Intent(this, AnadirTareasActivity.class)
                ));

        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        aplicarFiltro();
    }

    private void abrirDetalleTarea(Tarea tarea) {
        Intent intent = new Intent(this, DetalleTareaActivity.class);
        intent.putExtra(DetalleTareaActivity.EXTRA_TAREA, tarea);
        startActivity(intent);
    }

    private void eliminarTarea(Tarea tarea) {
        todasLasTareas.remove(tarea);

        int posicion = tareasVisibles.indexOf(tarea);
        if (posicion >= 0) {
            tareasVisibles.remove(posicion);
            adaptador.notifyItemRemoved(posicion);
        }

        Toast.makeText(
                this,
                "Tarea borrada",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void alCambiarEstado(Tarea tarea) {
        aplicarFiltro();

        Toast.makeText(
                this,
                tarea.isHecha()
                        ? "Tarea marcada como hecha"
                        : "Tarea marcada como no hecha",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void aplicarFiltro() {
        tareasVisibles.clear();

        for (Tarea t : todasLasTareas) {
            if (t.isHecha() == mostrarHechas) {
                tareasVisibles.add(t);
            }
        }

        if (adaptador != null) {
            adaptador.notifyDataSetChanged();
        }
    }

    private void cargarTareasIniciales() {
        // 26 tareas: 20 no hechas (visibles al abrir) y 6 hechas.

        // Tareas hechas
        agregar("Limpiar baño", "Puede esperar",
                "Lunes", 0, "10:00", "Jose", true);
        agregar("Ordenar el armario", "Puede esperar",
                "Jueves", 0, "17:00", "Jose", true);
        agregar("Comprar productos de limpieza", "Hacer cuanto antes",
                "Sábado", 12.50, "12:00", "Winwaleo", true);
        agregar("Cambiar las sábanas", "Hacer cuanto antes",
                "Lunes", 0, "19:00", "Raul", true);
        agregar("Comprar productos de cocina", "Urgente",
                "Miércoles", 23.40, "13:00", "Jose", true);
        agregar("Pagar la luz", "Urgente",
                "Viernes", 48.75, "09:00", "Winwaleo", true);

        // Tareas no hechas
        agregar("Limpiar salón", "Puede esperar",
                "Lunes", 0, "08:00", "Raul", false);
        agregar("Regar las plantas", "Puede esperar",
                "Martes", 0, "09:00", "Winwaleo", false);
        agregar("Limpiar cocina", "Hacer cuanto antes",
                "Miércoles", 0, "10:00", "Raul", false);
        agregar("Lavar la ropa", "Hacer cuanto antes",
                "Jueves", 0, "11:00", "Jose", false);
        agregar("Sacar la basura", "Urgente",
                "Viernes", 0, "12:00", "Winwaleo", false);
        agregar("Arreglar la persiana", "Urgente",
                "Sábado", 0, "13:00", "Raul", false);
        agregar("Barrer el pasillo", "Puede esperar",
                "Domingo", 0, "14:00", "Jose", false);
        agregar("Fregar los platos", "Puede esperar",
                "Lunes", 0, "15:00", "Raul", false);
        agregar("Cambiar la bombilla", "Urgente",
                "Martes", 0, "16:00", "Winwaleo", false);
        agregar("Organizar la despensa", "Hacer cuanto antes",
                "Miércoles", 0, "17:00", "Jose", false);
        agregar("Tender la ropa", "Puede esperar",
                "Jueves", 0, "18:00", "Raul", false);
        agregar("Arreglar la silla", "Urgente",
                "Viernes", 0, "19:00", "Winwaleo", false);
        agregar("Limpiar las ventanas", "Puede esperar",
                "Sábado", 0, "08:30", "Jose", false);
        agregar("Hacer la cama", "Puede esperar",
                "Domingo", 0, "09:30", "Raul", false);
        agregar("Ordenar el escritorio", "Hacer cuanto antes",
                "Lunes", 0, "10:30", "Winwaleo", false);
        agregar("Limpiar el frigorífico", "Hacer cuanto antes",
                "Martes", 0, "11:30", "Jose", false);
        agregar("Reponer el papel higiénico", "Urgente",
                "Miércoles", 0, "12:30", "Raul", false);
        agregar("Barrer la terraza", "Puede esperar",
                "Jueves", 0, "13:30", "Winwaleo", false);
        agregar("Limpiar los zapatos", "Puede esperar",
                "Viernes", 0, "14:30", "Jose", false);
        agregar("Preparar la mochila", "Hacer cuanto antes",
                "Sábado", 0, "15:30", "Raul", false);
    }

    private void agregar(String nombre, String prioridad, String dia,
                         double dinero, String hora, String companero,
                         boolean hecha) {
        Tarea tarea = new Tarea(
                nombre, prioridad, dia, dinero, hora,
                companero, R.drawable.ic_persona, hecha
        );
        tarea.setDificultad(Tarea.dificultadPorPrioridad(prioridad));
        todasLasTareas.add(tarea);
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