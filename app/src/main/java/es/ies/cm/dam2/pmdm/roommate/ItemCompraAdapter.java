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
 * Adaptador que muestra el nombre y la categoria de cada producto.
 */
public class ItemCompraAdapter extends ArrayAdapter<ItemCompra> {

    private final Context context;
    private final List<ItemCompra> items;

    public ItemCompraAdapter(Context context, List<ItemCompra> items) {
        super(context, R.layout.item_lista_compra, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View vista = convertView;

        if (vista == null) {
            vista = LayoutInflater.from(context)
                    .inflate(R.layout.item_lista_compra, parent, false);
        }

        ItemCompra item = items.get(position);

        TextView nombre = vista.findViewById(R.id.tvNombreCompra);
        TextView categoria = vista.findViewById(R.id.tvCategoriaCompra);

        nombre.setText(item.getNombre());
        categoria.setText(item.getCategoria());

        return vista;
    }
}
