package es.ies.cm.dam2.pmdm.roommate;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Formulario para crear una tarea nueva o editar una existente.
 */
public class AnadirTareasActivity extends AppCompatActivity {

    public static final String EXTRA_TAREA = "extra_tarea";
    public static final String EXTRA_TAREA_EDITAR = "extra_tarea_editar";

    private EditText etNombre, etDia, etDinero, etHora, etDescripcion;
    private Spinner spinnerPrioridad, spinnerCompanero;
    private CheckBox cbHecha;
    private RatingBar rbDificultad;
    private TextView tvTitulo;

    private int tareaId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anadir_tareas);
        aplicarInsets();

        etNombre = findViewById(R.id.etNombreTarea);
        spinnerPrioridad = findViewById(R.id.spinnerPrioridadTarea);
        spinnerCompanero = findViewById(R.id.spinnerCompaneroTarea);
        etDia = findViewById(R.id.etDiaTarea);
        etDinero = findViewById(R.id.etDineroTarea);
        etHora = findViewById(R.id.etHoraTarea);
        etDescripcion = findViewById(R.id.etDescripcionTarea);
        cbHecha = findViewById(R.id.cbHecha);
        rbDificultad = findViewById(R.id.rbDificultadTarea);
        tvTitulo = findViewById(R.id.tvTituloFormulario);
        Button btnAceptar = findViewById(R.id.btnAceptarTarea);
        Button btnCancelar = findViewById(R.id.btnCancelarTarea);

        ArrayAdapter<CharSequence> adapterPrioridad =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.prioridades_tarea,
                        R.layout.item_spinner_texto
                );
        adapterPrioridad.setDropDownViewResource(
                R.layout.item_spinner_texto
        );
        spinnerPrioridad.setAdapter(adapterPrioridad);

        configurarSpinnerCompanero();

        rbDificultad.setRating(3);

        etHora.setOnClickListener(v -> mostrarSelectorHora());

        btnAceptar.setOnClickListener(v -> aceptar());
        btnCancelar.setOnClickListener(v -> finish());

        // Si llega una tarea, el formulario se rellena para editarla.
        Tarea tareaEditar = (Tarea) getIntent()
                .getSerializableExtra(EXTRA_TAREA_EDITAR);

        if (tareaEditar != null) {
            tareaId = tareaEditar.getId();
            tvTitulo.setText(R.string.editar_tarea_titulo);
            btnAceptar.setText(R.string.guardar);
            cargarTareaEnFormulario(tareaEditar);
        }
    }

    private void configurarSpinnerCompanero() {
        List<String> opciones = new ArrayList<>();
        opciones.add(getString(R.string.selecciona_companero));

        RoomMateDB db = new RoomMateDB(this);
        List<String> nombres = db.obtenerNombresCompaneros();
        db.close();

        if (nombres.isEmpty()) {
            // Si todavia no hay companeros guardados, se usan los de ejemplo.
            String[] ejemplos = getResources()
                    .getStringArray(R.array.companeros_tarea);
            for (int i = 1; i < ejemplos.length; i++) {
                opciones.add(ejemplos[i]);
            }
        } else {
            opciones.addAll(nombres);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_texto,
                opciones
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_texto);
        spinnerCompanero.setAdapter(adapter);
    }

    private void cargarTareaEnFormulario(Tarea tarea) {
        etNombre.setText(tarea.getNombre());
        etDia.setText(tarea.getDia());
        etHora.setText(tarea.getHora());
        etDescripcion.setText(tarea.getDescripcion());
        cbHecha.setChecked(tarea.isHecha());
        rbDificultad.setRating(tarea.getDificultad());

        if (tarea.getDinero() > 0) {
            etDinero.setText(String.valueOf(tarea.getDinero()));
        }

        seleccionarEnSpinner(spinnerPrioridad, tarea.getPrioridad());
        seleccionarEnSpinner(spinnerCompanero, tarea.getCompanero());
    }

    @SuppressWarnings("unchecked")
    private void seleccionarEnSpinner(Spinner spinner, String valor) {
        if (valor == null) {
            return;
        }

        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item != null && item.toString().equals(valor)) {
                spinner.setSelection(i);
                return;
            }
        }

        // Si el companero ya no esta en la lista, se anade para no perderlo.
        if (spinner == spinnerCompanero) {
            ArrayAdapter<String> adapter =
                    (ArrayAdapter<String>) spinner.getAdapter();
            adapter.add(valor);
            spinner.setSelection(adapter.getCount() - 1);
        }
    }

    private void mostrarSelectorHora() {
        int hora = 18;
        int minuto = 30;

        String horaActual = etHora.getText().toString().trim();
        if (horaActual.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
            String[] partes = horaActual.split(":");
            hora = Integer.parseInt(partes[0]);
            minuto = Integer.parseInt(partes[1]);
        }

        TimePickerDialog dialogo = new TimePickerDialog(
                this,
                (view, horaElegida, minutoElegido) ->
                        etHora.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "%02d:%02d",
                                        horaElegida,
                                        minutoElegido
                                )
                        ),
                hora,
                minuto,
                true
        );

        dialogo.show();
    }

    private void aceptar() {
        etNombre.setError(null);
        etDia.setError(null);
        etDinero.setError(null);
        etHora.setError(null);

        String nombre = etNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            etNombre.setError(getString(R.string.error_nombre));
            etNombre.requestFocus();
            return;
        }

        int posPrioridad = spinnerPrioridad.getSelectedItemPosition();
        if (posPrioridad <= 0) {
            Toast.makeText(
                    this,
                    R.string.error_prioridad,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        String prioridad = spinnerPrioridad
                .getItemAtPosition(posPrioridad)
                .toString();

        int posCompanero = spinnerCompanero.getSelectedItemPosition();
        if (posCompanero <= 0) {
            Toast.makeText(
                    this,
                    R.string.error_companero,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        String companero = spinnerCompanero
                .getItemAtPosition(posCompanero)
                .toString();

        boolean hecha = cbHecha.isChecked();
        String dia = etDia.getText().toString().trim();
        String hora = etHora.getText().toString().trim();

        boolean hayFecha = !dia.isEmpty();
        boolean hayHora = !hora.isEmpty();

        if (hecha && !hayFecha) {
            etDia.setError(getString(R.string.error_fecha));
            etDia.requestFocus();
            return;
        }

        if (hecha && !hayHora) {
            Toast.makeText(
                    this,
                    R.string.error_hora_obligatoria,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!hecha && hayFecha != hayHora) {
            if (!hayFecha) {
                etDia.setError(getString(R.string.error_fecha));
                etDia.requestFocus();
            } else {
                Toast.makeText(
                        this,
                        R.string.error_hora_obligatoria,
                        Toast.LENGTH_SHORT
                ).show();
            }
            return;
        }

        if (hayHora && !hora.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
            etHora.setError(getString(R.string.error_hora));
            Toast.makeText(
                    this,
                    R.string.error_hora,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        double dinero = 0;
        String dineroTexto = etDinero.getText().toString()
                .trim()
                .replace(',', '.');

        if (!dineroTexto.isEmpty()) {
            try {
                dinero = Double.parseDouble(dineroTexto);
            } catch (NumberFormatException e) {
                etDinero.setError(getString(R.string.error_dinero));
                etDinero.requestFocus();
                return;
            }

            if (dinero < 0) {
                etDinero.setError(
                        getString(R.string.error_dinero_negativo)
                );
                etDinero.requestFocus();
                return;
            }
        }

        int dificultad = Math.max(
                1,
                (int) Math.round(rbDificultad.getRating())
        );

        String descripcion = etDescripcion.getText().toString().trim();

        Tarea tarea = new Tarea(
                tareaId,
                nombre,
                prioridad,
                dia,
                dinero,
                hora,
                companero,
                R.drawable.ic_persona,
                hecha,
                dificultad,
                descripcion
        );

        RoomMateDB db = new RoomMateDB(this);

        if (tareaId == 0) {
            long id = db.insertarTarea(tarea);
            if (id <= 0) {
                db.close();
                Toast.makeText(
                        this,
                        R.string.error_guardar_tarea,
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
            tarea.setId((int) id);
        } else {
            db.actualizarTarea(tarea);
        }

        db.close();

        Intent data = new Intent();
        data.putExtra(EXTRA_TAREA, tarea);
        setResult(RESULT_OK, data);
        finish();
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
