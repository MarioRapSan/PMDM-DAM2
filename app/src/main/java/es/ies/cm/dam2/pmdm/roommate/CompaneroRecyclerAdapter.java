package es.ies.cm.dam2.pmdm.roommate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adaptador propio del RecyclerView de companeros. Cada elemento es una
 * CardView (item_companero_card) con la foto, el nombre completo y los datos
 * de contacto. El perfil del usuario que ha iniciado sesion se marca con "(tu)".
 * Avisa a la Activity del clic corto (abrir la ficha) y del clic largo
 * (menu contextual Editar / Eliminar, solo en el perfil propio).
 */
public class CompaneroRecyclerAdapter
        extends RecyclerView.Adapter<CompaneroRecyclerAdapter.CompaneroViewHolder> {

    /** Clic corto en una tarjeta. */
    public interface OnCompaneroClickListener {
        void onCompaneroClick(Companero companero);
    }

    /** Clic largo en una tarjeta (recibe la vista para anclar el menu). */
    public interface OnCompaneroLongClickListener {
        void onCompaneroLongClick(View vista, Companero companero);
    }

    private final List<Companero> companeros;
    private final OnCompaneroClickListener clickListener;
    private final OnCompaneroLongClickListener longClickListener;

    /** Usuario que ha iniciado sesion (para marcar su propio perfil). */
    private final int usuarioId;

    /** Recibe la lista de companeros, el usuario actual y los listeners de la Activity. */
    public CompaneroRecyclerAdapter(@NonNull List<Companero> companeros,
                                    int usuarioId,
                                    OnCompaneroClickListener clickListener,
                                    OnCompaneroLongClickListener longClickListener) {
        this.companeros = companeros;
        this.usuarioId = usuarioId;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    /** Infla la tarjeta (CardView) de un companero. */
    @NonNull
    @Override
    public CompaneroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_companero_card, parent, false);
        return new CompaneroViewHolder(vista);
    }

    /** Rellena la tarjeta con los datos del companero y le asigna los clics. */
    @Override
    public void onBindViewHolder(@NonNull CompaneroViewHolder holder, int position) {
        Companero companero = companeros.get(position);
        Context contexto = holder.itemView.getContext();

        FotoPerfilUtils.mostrarFotoPerfil(holder.foto, companero.getFotoPerfil());
        holder.nombre.setText(companero.esDe(usuarioId)
                ? contexto.getString(R.string.nombre_propio, companero.getNombreCompleto())
                : companero.getNombreCompleto());

        // Telefono y correo en una linea; si falta alguno se muestra "Sin ...".
        String telefono = vacio(companero.getTelefono())
                ? contexto.getString(R.string.sin_telefono) : companero.getTelefono();
        String correo = vacio(companero.getCorreo())
                ? contexto.getString(R.string.sin_correo) : companero.getCorreo();
        holder.contacto.setText(telefono + " · " + correo);

        holder.itemView.setOnClickListener(v -> clickListener.onCompaneroClick(companero));
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onCompaneroLongClick(v, companero);
            return true;
        });
    }

    /** Numero de companeros que se muestran. */
    @Override
    public int getItemCount() {
        return companeros.size();
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    /** Guarda las vistas de una tarjeta para no buscarlas cada vez. */
    static class CompaneroViewHolder extends RecyclerView.ViewHolder {

        final ImageView foto;
        final TextView nombre;
        final TextView contacto;

        CompaneroViewHolder(@NonNull View itemView) {
            super(itemView);
            foto = itemView.findViewById(R.id.ivFotoCompaneroItem);
            nombre = itemView.findViewById(R.id.tvNombreCompaneroItem);
            contacto = itemView.findViewById(R.id.tvContactoCompaneroItem);
        }
    }
}
