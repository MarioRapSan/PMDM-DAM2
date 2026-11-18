package es.ies.cm.dam2.pmdm.roommate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adaptador propio del RecyclerView de tareas. Cada elemento es una CardView
 * (item_tarea_card) con el nombre, quien la anadio, la etiqueta "Compartida"
 * y un CheckBox para marcarla como hecha.
 *
 * Avisa a la Activity mediante tres listeners: clic corto (abrir ficha),
 * cambio del CheckBox (guardar estado) y clic largo (menu contextual).
 */
public class TareaRecyclerAdapter
        extends RecyclerView.Adapter<TareaRecyclerAdapter.TareaViewHolder> {

    public interface OnTareaClickListener {
        void onTareaClick(Tarea tarea);
    }

    public interface OnEstadoCambiadoListener {
        void onEstadoCambiado(Tarea tarea);
    }

    public interface OnTareaLongClickListener {
        void onTareaLongClick(View vista, Tarea tarea);
    }

    private final List<Tarea> tareas;
    private final OnTareaClickListener clickListener;
    private final OnEstadoCambiadoListener estadoListener;
    private final OnTareaLongClickListener longClickListener;

    /** Recibe la lista de tareas y los tres listeners de la Activity. */
    public TareaRecyclerAdapter(
            @NonNull List<Tarea> tareas,
            OnTareaClickListener clickListener,
            OnEstadoCambiadoListener estadoListener,
            OnTareaLongClickListener longClickListener
    ) {
        this.tareas = tareas;
        this.clickListener = clickListener;
        this.estadoListener = estadoListener;
        this.longClickListener = longClickListener;
    }

    /** Infla la tarjeta (CardView) de una tarea. */
    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarea_card, parent, false);
        return new TareaViewHolder(view);
    }

    /** Rellena la tarjeta con los datos de la tarea y le asigna los clics. */
    @Override
    public void onBindViewHolder(
            @NonNull TareaViewHolder holder,
            int position
    ) {
        Tarea tarea = tareas.get(position);

        int foto = tarea.getFotoResId() == 0
                ? R.drawable.ic_persona
                : tarea.getFotoResId();
        holder.foto.setImageResource(foto);

        holder.nombre.setText(tarea.getNombre());

        android.content.Context contexto = holder.itemView.getContext();
        String companero = tarea.getCompanero();
        if (companero == null || companero.trim().isEmpty()) {
            companero = contexto.getString(R.string.sin_asignar);
        }
        holder.companero.setText(contexto.getString(R.string.anadida_por, companero));
        holder.compartida.setVisibility(tarea.isCompartida() ? View.VISIBLE : View.GONE);

        // Se quita el listener antes de marcar el CheckBox para que el reciclado
        // de vistas no dispare cambios falsos en otras tareas.
        holder.check.setOnCheckedChangeListener(null);
        holder.check.setChecked(tarea.isHecha());
        holder.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (tarea.isHecha() != isChecked) {
                tarea.setHecha(isChecked);
                if (estadoListener != null) {
                    estadoListener.onEstadoCambiado(tarea);
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTareaClick(tarea);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onTareaLongClick(v, tarea);
                return true;
            }
            return false;
        });
    }

    /** Numero de tareas que se muestran. */
    @Override
    public int getItemCount() {
        return tareas.size();
    }

    static class TareaViewHolder extends RecyclerView.ViewHolder {

        ImageView foto;
        TextView nombre;
        TextView companero;
        TextView compartida;
        CheckBox check;

        TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            foto = itemView.findViewById(R.id.ivFotoTarea);
            nombre = itemView.findViewById(R.id.tvNombreTarea);
            companero = itemView.findViewById(R.id.tvCompaneroTarea);
            compartida = itemView.findViewById(R.id.tvCompartidaTarea);
            check = itemView.findViewById(R.id.cbHechaItem);
        }
    }
}