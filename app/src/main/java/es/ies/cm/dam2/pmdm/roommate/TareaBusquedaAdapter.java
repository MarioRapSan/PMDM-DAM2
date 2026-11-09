package es.ies.cm.dam2.pmdm.roommate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Adaptador que muestra cada resultado de la busqueda.
 */
public class TareaBusquedaAdapter extends ArrayAdapter<Tarea> {

    private final Context context;
    private final List<Tarea> tareas;

    public TareaBusquedaAdapter(Context context, List<Tarea> tareas) {
        super(context, R.layout.item_busqueda_tarea, tareas);
        this.context = context;
        this.tareas = tareas;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View vista = convertView;

        if (vista == null) {
            vista = LayoutInflater.from(context)
                    .inflate(R.layout.item_busqueda_tarea, parent, false);
        }

        Tarea tarea = tareas.get(position);

        TextView nombre = vista.findViewById(R.id.tvNombreBusqueda);
        TextView detalle = vista.findViewById(R.id.tvDetalleBusqueda);

        nombre.setText(tarea.getNombre());

        String estado = tarea.isHecha() ? "Hecha" : "Pendiente";
        detalle.setText(
                tarea.getPrioridad() + " · "
                        + tarea.getCompanero() + " · " + estado
        );

        return vista;
    }
}
