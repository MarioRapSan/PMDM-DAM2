package es.ies.cm.dam2.pmdm.roommate;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * Dialogo de confirmacion reutilizable (Si / No).
 *
 * Se usa, por ejemplo, para confirmar el borrado de una tarea o la baja de un usuario.
 * Al ser un DialogFragment sobrevive al giro de pantalla, cosa que un AlertDialog
 * creado directamente en la Activity no hace.
 *
 * La respuesta se devuelve con la Fragment Result API: la pantalla que lo abre
 * escucha la clave {@link #getArguments() requestKey} y recibe RESULTADO_CONFIRMADO = true
 * cuando el usuario pulsa el boton positivo.
 */
public class ConfirmacionDialogFragment extends DialogFragment {

    public static final String RESULTADO_CONFIRMADO = "confirmado";

    private static final String ARG_CLAVE = "clave";
    private static final String ARG_TITULO = "titulo";
    private static final String ARG_MENSAJE = "mensaje";
    private static final String ARG_BOTON_SI = "boton_si";

    /**
     * Crea el dialogo.
     *
     * @param requestKey clave con la que se devolvera la respuesta
     * @param titulo     recurso de texto del titulo
     * @param mensaje    recurso de texto del mensaje
     * @param botonSi    recurso de texto del boton positivo (Eliminar, Darme de baja...)
     */
    public static ConfirmacionDialogFragment nuevo(
            String requestKey, int titulo, int mensaje, int botonSi) {
        Bundle args = new Bundle();
        args.putString(ARG_CLAVE, requestKey);
        args.putInt(ARG_TITULO, titulo);
        args.putInt(ARG_MENSAJE, mensaje);
        args.putInt(ARG_BOTON_SI, botonSi);

        ConfirmacionDialogFragment dialogo = new ConfirmacionDialogFragment();
        dialogo.setArguments(args);
        return dialogo;
    }

    /** Crea el AlertDialog con el titulo, el mensaje y los botones recibidos como argumentos. */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        String clave = args.getString(ARG_CLAVE);

        return new AlertDialog.Builder(requireContext())
                .setTitle(args.getInt(ARG_TITULO))
                .setMessage(args.getInt(ARG_MENSAJE))
                .setPositiveButton(args.getInt(ARG_BOTON_SI), (d, w) -> {
                    // Devuelve la confirmacion a la Activity que abrio el dialogo.
                    Bundle resultado = new Bundle();
                    resultado.putBoolean(RESULTADO_CONFIRMADO, true);
                    getParentFragmentManager().setFragmentResult(clave, resultado);
                })
                .setNegativeButton(R.string.cancelar, null)
                .create();
    }
}
