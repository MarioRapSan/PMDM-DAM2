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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class AnadirTareasActivity extends AppCompatActivity {

    public static final String EXTRA_TAREA = "extra_tarea";

    private EditText etNombre, etDia, etDinero, etHora;
    private Spinner spinnerPrioridad, spinnerCompanero;
    private CheckBox cbHecha;
    private RatingBar rbDificultad;

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
        cbHecha = findViewById(R.id.cbHecha);
        rbDificultad = findViewById(R.id.rbDificultadTarea);
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

        ArrayAdapter<CharSequence> adapterCompanero =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.companeros_tarea,
                        R.layout.item_spinner_texto
                );
        adapterCompanero.setDropDownViewResource(
                R.layout.item_spinner_texto
        );
        spinnerCompanero.setAdapter(adapterCompanero);

        rbDificultad.setRating(3);

        etHora.setOnClickListener(v -> mostrarSelectorHora());

        btnAceptar.setOnClickListener(v -> aceptar());
        btnCancelar.setOnClickListener(v -> finish());
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

        Tarea tarea = new Tarea(
                nombre,
                prioridad,
                dia,
                dinero,
                hora,
                companero,
                R.drawable.ic_persona,
                hecha
        );
        tarea.setDificultad(dificultad);

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
