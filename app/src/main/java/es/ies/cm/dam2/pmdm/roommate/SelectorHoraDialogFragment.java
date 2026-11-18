package es.ies.cm.dam2.pmdm.roommate;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Locale;

/**
 * DialogFragment propio con un TimePicker en formato 24 horas.
 *
 * Recibe la hora actual del campo (HH:mm) y devuelve la hora elegida
 * con la Fragment Result API.
 */
public class SelectorHoraDialogFragment extends DialogFragment {

    public static final String REQUEST_KEY = "selector_hora";
    public static final String RESULTADO_HORA = "hora";
    public static final String PATRON_HORA = "^([01]?\\d|2[0-3]):[0-5]\\d$";

    private static final String ARG_HORA = "hora_inicial";

    /** Crea el dialogo pasandole la hora que ya tenia el campo. */
    public static SelectorHoraDialogFragment nuevo(String horaActual) {
        Bundle args = new Bundle();
        args.putString(ARG_HORA, horaActual);
        SelectorHoraDialogFragment dialogo = new SelectorHoraDialogFragment();
        dialogo.setArguments(args);
        return dialogo;
    }

    /** Crea el TimePickerDialog abierto en la hora del campo (o a las 18:30). */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Hora por defecto si el campo esta vacio.
        int hora = 18;
        int minuto = 30;

        String horaActual = requireArguments().getString(ARG_HORA, "");
        if (horaActual.matches(PATRON_HORA)) {
            String[] partes = horaActual.split(":");
            hora = Integer.parseInt(partes[0]);
            minuto = Integer.parseInt(partes[1]);
        }

        return new TimePickerDialog(
                requireContext(),
                (view, h, m) -> {
                    Bundle resultado = new Bundle();
                    resultado.putString(RESULTADO_HORA,
                            String.format(Locale.getDefault(), "%02d:%02d", h, m));
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY, resultado);
                },
                hora,
                minuto,
                true
        );
    }
}
