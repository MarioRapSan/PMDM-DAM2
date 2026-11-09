
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

import es.ies.cm.dam2.pmdm.roommate.db.RoomMateDB;
import es.ies.cm.dam2.pmdm.roommate.db.Tarea;
import es.ies.cm.dam2.pmdm.roommate.db.TareasRepository;

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

        // Configurar Spinner de prioridad
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

        // Configurar Spinner de compañeros
        configurarSpinnerCompanero();

        rbDificultad.setRating(3);

        etHora.setOnClickListener(v -> mostrarSelectorHora());

        btnAceptar.setOnClickListener(v -> aceptar());
        btnCancelar.setOnClickListener(v -> finish());

        // Comprobar si se recibe una tarea para editar
        Tarea tareaEditar = (Tarea) getIntent()
                .getSerializableExtra(EXTRA_TAREA_EDITAR);

        if (tareaEditar != null) {
            tareaId = tareaEditar.getId();

            tvTitulo.setText(R.string.editar_tarea_titulo);
            btnAceptar.setText(R.string.guardar);

            cargarTareaEnFormulario(tareaEditar);
        }
    }

    // Configurar Spinner de compañeros
    private void configurarSpinnerCompanero() {

        List<String> opciones = new ArrayList<>();

        opciones.add(getString(R.string.selecciona_companero));

        RoomMateDB db = RoomMateDB.getInstance(this);

        List<String> nombres = db.obtenerNombresCompaneros();

        if (nombres.isEmpty()) {

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

        adapter.setDropDownViewResource(
                R.layout.item_spinner_texto
        );

        spinnerCompanero.setAdapter(adapter);
    }

    // Cargar los datos de una tarea en el formulario
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

        seleccionarEnSpinner(
                spinnerPrioridad,
                tarea.getPrioridad()
        );

        seleccionarEnSpinner(
                spinnerCompanero,
                tarea.getCompanero()
        );
    }

    // Seleccionar un elemento de un Spinner
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

        // Si el compañero ya no está en la lista, añadirlo
        if (spinner == spinnerCompanero) {

            ArrayAdapter<String> adapter =
                    (ArrayAdapter<String>) spinner.getAdapter();

            adapter.add(valor);

            spinner.setSelection(adapter.getCount() - 1);
        }
    }

    // Mostrar selector de hora
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

    // Validar y guardar tarea
    private void aceptar() {

        etNombre.setError(null);
        etDia.setError(null);
        etDinero.setError(null);
        etHora.setError(null);

        // Nombre
        String nombre = etNombre.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError(getString(R.string.error_nombre));
            etNombre.requestFocus();
            return;
        }

        // Prioridad
        int posPrioridad =
                spinnerPrioridad.getSelectedItemPosition();

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

        // Compañero
        int posCompanero =
                spinnerCompanero.getSelectedItemPosition();

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

        // Estado de la tarea
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

        // Validar formato de hora
        if (hayHora &&
                !hora.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {

            etHora.setError(getString(R.string.error_hora));

            Toast.makeText(
                    this,
                    R.string.error_hora,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Dinero
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

        // Dificultad
        int dificultad = Math.max(
                1,
                (int) Math.round(rbDificultad.getRating())
        );

        // Descripción
        String descripcion =
                etDescripcion.getText().toString().trim();

        // Crear objeto Tarea
        Tarea tarea = new Tarea(
                tareaId,
                nombre,
                prioridad,
                dia,
                dinero,
                hora,
                companero,
                hecha,
                dificultad,
                descripcion
        );

        // Guardar mediante el repositorio
        TareasRepository repository =
                new TareasRepository(this);

        boolean guardada;

        if (tareaId == 0) {

            // Insertar tarea nueva
            guardada = repository.insertarTarea(tarea);

        } else {

            // Actualizar tarea existente
            guardada = repository.actualizarTarea(tarea);
        }

        if (!guardada) {

            Toast.makeText(
                    this,
                    R.string.error_guardar_tarea,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Devolver tarea a la actividad anterior
        Intent data = new Intent();

        data.putExtra(EXTRA_TAREA, tarea);

        setResult(RESULT_OK, data);

        finish();
    }

    // Aplicar márgenes de pantalla
    private void aplicarInsets() {

        View root = findViewById(R.id.main);

        int base = (int) (24 * getResources()
                .getDisplayMetrics().density);

        ViewCompat.setOnApplyWindowInsetsListener(
                root,
                (v, insets) -> {

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
                }
        );
    }
}
