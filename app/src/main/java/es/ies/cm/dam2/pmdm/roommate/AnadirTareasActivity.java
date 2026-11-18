package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Formulario para crear una tarea nueva o editar una existente (alta / edicion).
 *
 * Usa componentes basicos (EditText, CheckBox) y avanzados (Spinner, Switch, RatingBar).
 * La fecha y la hora se eligen con DialogFragment propios (SelectorFechaDialogFragment
 * y SelectorHoraDialogFragment) y se guardan en la tarea. El guardado en Room se hace
 * en un hilo secundario y, al terminar, se devuelve la tarea a la pantalla anterior.
 */
public class AnadirTareasActivity extends AppCompatActivity {

    public static final String EXTRA_TAREA = "extra_tarea";
    public static final String EXTRA_TAREA_EDITAR = "extra_tarea_editar";

    private EditText etNombre, etDia, etDinero, etHora, etDescripcion;
    private Spinner spinnerPrioridad, spinnerCompanero;
    private CheckBox cbHecha;
    private Switch swCompartida;
    private RatingBar rbDificultad;
    private TextView tvTitulo;

    private int tareaId = 0;

    /** Companero que hay que seleccionar cuando se cargue el Spinner. */
    private String companeroPendiente;
    private static final String ESTADO_COMPANERO = "companero";

    /**
     * Configura los Spinner y los selectores de fecha y hora. Si llega una tarea, pasa a modo
     * edicion.
     */
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
        swCompartida = findViewById(R.id.swCompartida);
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

        // Valor por defecto solo al crear la pantalla (no al girar).
        if (savedInstanceState == null) {
            rbDificultad.setRating(3);
        }

        // Fecha y hora con DialogFragment propios. El resultado llega por la
        // Fragment Result API, que sigue funcionando aunque se gire la pantalla.
        etDia.setOnClickListener(v ->
                SelectorFechaDialogFragment.nuevo(etDia.getText().toString().trim())
                        .show(getSupportFragmentManager(), "selector_fecha"));
        etHora.setOnClickListener(v ->
                SelectorHoraDialogFragment.nuevo(etHora.getText().toString().trim())
                        .show(getSupportFragmentManager(), "selector_hora"));

        getSupportFragmentManager().setFragmentResultListener(
                SelectorFechaDialogFragment.REQUEST_KEY, this, (clave, resultado) -> {
                    etDia.setText(resultado.getString(SelectorFechaDialogFragment.RESULTADO_FECHA));
                    etDia.setError(null);
                });
        getSupportFragmentManager().setFragmentResultListener(
                SelectorHoraDialogFragment.REQUEST_KEY, this, (clave, resultado) -> {
                    etHora.setText(resultado.getString(SelectorHoraDialogFragment.RESULTADO_HORA));
                    etHora.setError(null);
                });

        btnAceptar.setOnClickListener(v -> aceptar());
        btnCancelar.setOnClickListener(v -> finish());

        // Si llega una tarea, el formulario se rellena para editarla.
        Tarea tareaEditar = (Tarea) getIntent()
                .getSerializableExtra(EXTRA_TAREA_EDITAR);

        if (savedInstanceState != null) {
            companeroPendiente = savedInstanceState.getString(ESTADO_COMPANERO);
        } else if (tareaEditar != null) {
            companeroPendiente = tareaEditar.getCompanero();
        }

        if (tareaEditar != null) {
            tareaId = tareaEditar.getId();
            tvTitulo.setText(R.string.editar_tarea_titulo);
            btnAceptar.setText(R.string.guardar);
            // Solo se rellena la primera vez: si se gira la pantalla, Android
            // restaura lo que el usuario ya habia cambiado.
            if (savedInstanceState == null) {
                cargarTareaEnFormulario(tareaEditar);
            }
        }
    }

    /**
     * Carga los companeros de SQLite en un hilo secundario y rellena el Spinner.
     * Cuando termina, selecciona el companero pendiente (al editar o al girar).
     */
    private void configurarSpinnerCompanero() {
        AppDatabase.ejecutar(() -> {
            RoomMateDB db = new RoomMateDB(this);
            List<String> nombres = db.obtenerNombresCompaneros();
            db.close();

            runOnUiThread(() -> {
                List<String> opciones = new ArrayList<>();
                opciones.add(getString(R.string.selecciona_companero));
                opciones.addAll(nombres);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, R.layout.item_spinner_texto, opciones);
                adapter.setDropDownViewResource(R.layout.item_spinner_texto);
                spinnerCompanero.setAdapter(adapter);

                if (companeroPendiente != null) {
                    seleccionarEnSpinner(spinnerCompanero, companeroPendiente);
                }
            });
        });
    }

    /** Guarda el companero elegido para recuperarlo al girar la pantalla. */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // El Spinner de companeros se rellena despues de girar, asi que se
        // guarda a mano el companero elegido para volver a seleccionarlo.
        if (spinnerCompanero.getSelectedItemPosition() > 0) {
            outState.putString(ESTADO_COMPANERO,
                    spinnerCompanero.getSelectedItem().toString());
        }
    }

    /** Rellena el formulario con los datos de la tarea que se va a editar. */
    private void cargarTareaEnFormulario(Tarea tarea) {
        etNombre.setText(tarea.getNombre());
        etDia.setText(tarea.getDia());
        etHora.setText(tarea.getHora());
        etDescripcion.setText(tarea.getDescripcion());
        cbHecha.setChecked(tarea.isHecha());
        swCompartida.setChecked(tarea.isCompartida());
        rbDificultad.setRating(tarea.getDificultad());

        if (tarea.getDinero() > 0) {
            etDinero.setText(String.valueOf(tarea.getDinero()));
        }

        spinnerPrioridad.setSelection(Prioridades.posicionDeValor(this, tarea.getPrioridad()));
    }

    /**
     * Selecciona en el Spinner el elemento con ese texto. Si es un companero que ya no existe,
     * lo anade.
     */
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

    /**
     * Valida el formulario y guarda la tarea en Room: insert si es nueva, update si se esta
     * editando.
     */
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
        // Se guarda el valor fijo, no el texto traducido del Spinner.
        String prioridad = Prioridades.valorDePosicion(this, posPrioridad);

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
            Toast.makeText(
                    this,
                    R.string.error_fecha,
                    Toast.LENGTH_SHORT
            ).show();
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
                Toast.makeText(
                        this,
                        R.string.error_fecha,
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(
                        this,
                        R.string.error_hora_obligatoria,
                        Toast.LENGTH_SHORT
                ).show();
            }
            return;
        }

        if (hayHora && !hora.matches(SelectorHoraDialogFragment.PATRON_HORA)) {
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
        tarea.setCompartida(swCompartida.isChecked());

        AppDatabase.ejecutar(() -> {
            TareaDao dao = AppDatabase.getInstance(this).tareaDao();

            if (tareaId == 0) {
                long id = dao.insertar(tarea);

                if (id > 0) {
                    tarea.setId((int) id);
                }
            } else {
                dao.actualizar(tarea);
            }

            runOnUiThread(() -> {
                Intent data = new Intent();
                data.putExtra(EXTRA_TAREA, tarea);
                setResult(RESULT_OK, data);
                finish();
            });
        });
    }

    /** Anade al padding el espacio de las barras del sistema (modo edge-to-edge). */
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
