package es.ies.cm.dam2.pmdm.roommate;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * DialogFragment con un pequeno formulario (layout propio) para anadir un
 * producto nuevo al catalogo de la lista de la compra.
 *
 * Devuelve el nombre y la categoria con la Fragment Result API. La validacion
 * se hace aqui: si el nombre esta vacio, el dialogo no se cierra.
 */
public class NuevoProductoDialogFragment extends DialogFragment {

    public static final String REQUEST_KEY = "nuevo_producto";
    public static final String RESULTADO_NOMBRE = "nombre";
    public static final String RESULTADO_CATEGORIA = "categoria";

    /** Crea el dialogo con el formulario y valida el nombre antes de cerrarlo. */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View vista = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_nuevo_producto, null);
        EditText etNombre = vista.findViewById(R.id.etNombreProducto);
        EditText etCategoria = vista.findViewById(R.id.etCategoriaProducto);

        AlertDialog dialogo = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.nuevo_producto)
                .setView(vista)
                // El listener real se pone en onShow para poder validar sin cerrar.
                .setPositiveButton(R.string.crear, null)
                .setNegativeButton(R.string.cancelar, null)
                .create();

        dialogo.setOnShowListener(d ->
                dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String nombre = etNombre.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        etNombre.setError(getString(R.string.error_producto_vacio));
                        return;
                    }

                    Bundle resultado = new Bundle();
                    resultado.putString(RESULTADO_NOMBRE, nombre);
                    resultado.putString(RESULTADO_CATEGORIA,
                            etCategoria.getText().toString().trim());
                    getParentFragmentManager().setFragmentResult(REQUEST_KEY, resultado);
                    dialogo.dismiss();
                }));

        return dialogo;
    }
}
