package es.ies.cm.dam2.pmdm.roommate;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * DialogFragment propio con un DatePicker.
 *
 * Recibe la fecha actual del campo (dd/MM/yyyy) para abrir el calendario en ese dia
 * y devuelve la fecha elegida, ya formateada, con la Fragment Result API.
 */
public class SelectorFechaDialogFragment extends DialogFragment {

    public static final String REQUEST_KEY = "selector_fecha";
    public static final String RESULTADO_FECHA = "fecha";
    public static final String FORMATO = "dd/MM/yyyy";

    private static final String ARG_FECHA = "fecha_inicial";

    /** Crea el dialogo pasandole la fecha que ya tenia el campo. */
    public static SelectorFechaDialogFragment nuevo(String fechaActual) {
        Bundle args = new Bundle();
        args.putString(ARG_FECHA, fechaActual);
        SelectorFechaDialogFragment dialogo = new SelectorFechaDialogFragment();
        dialogo.setArguments(args);
        return dialogo;
    }

    /** Crea el DatePickerDialog abierto en la fecha del campo (o en la de hoy). */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar calendario = Calendar.getInstance();

        // Si el campo ya tenia una fecha valida, el calendario se abre en ella.
        String fechaActual = requireArguments().getString(ARG_FECHA, "");
        SimpleDateFormat formato = new SimpleDateFormat(FORMATO, Locale.getDefault());
        formato.setLenient(false);
        try {
            Date fecha = formato.parse(fechaActual);
            if (fecha != null) {
                calendario.setTime(fecha);
            }
        } catch (ParseException ignored) {
            // Formato no valido o campo vacio: se usa la fecha de hoy.
        }

        return new DatePickerDialog(
                requireContext(),
                (view, anio, mes, dia) -> {
                    Bundle resultado = new Bundle();
                    // El mes del DatePicker empieza en 0, por eso se suma 1.
                    resultado.putString(RESULTADO_FECHA, String.format(
                            Locale.getDefault(), "%02d/%02d/%04d", dia, mes + 1, anio));
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY, resultado);
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );
    }
}
