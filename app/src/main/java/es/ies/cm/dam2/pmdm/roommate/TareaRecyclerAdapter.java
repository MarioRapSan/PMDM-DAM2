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

        String companero = tarea.getCompanero();
        if (companero == null || companero.trim().isEmpty()) {
            companero = "Sin asignar";
        }
        holder.companero.setText("Añadida por: " + companero);

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

    @Override
    public int getItemCount() {
        return tareas.size();
    }

    static class TareaViewHolder extends RecyclerView.ViewHolder {

        ImageView foto;
        TextView nombre;
        TextView companero;
        CheckBox check;

        TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            foto = itemView.findViewById(R.id.ivFotoTarea);
            nombre = itemView.findViewById(R.id.tvNombreTarea);
            companero = itemView.findViewById(R.id.tvCompaneroTarea);
            check = itemView.findViewById(R.id.cbHechaItem);
        }
    }
}