package es.ies.cm.dam2.pmdm.roommate;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista de la compra de un usuario.
 * Los productos se eligen de la base de datos y se guardan por usuario.
 */
public class ListaCompraActivity extends AppCompatActivity {

    private Spinner spinnerItems;
    private ListView lista;
    private TextView tvListaVacia;

    private final List<ItemCompra> items = new ArrayList<>();
    private final List<ItemCompra> listaCompra = new ArrayList<>();

    private ArrayAdapter<ItemCompra> adapterSpinner;
    private ItemCompraAdapter adapterLista;
    private RoomMateDB db;

    private int usuarioId = 1;
    private String usuarioNombre = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_compra);
        aplicarInsets();

        db = new RoomMateDB(this);

        usuarioId = getIntent().getIntExtra(
                ColeccionTareasActivity.EXTRA_USUARIO_ID,
                1
        );
        usuarioNombre = getIntent().getStringExtra(
                ColeccionTareasActivity.EXTRA_USUARIO_NOMBRE
        );
        if (usuarioNombre == null || usuarioNombre.isEmpty()) {
            usuarioNombre = "admin";
        }

        TextView tvTitulo = findViewById(R.id.tvTituloCompra);
        tvTitulo.setText(
                getString(R.string.lista_compra_de) + " " + usuarioNombre
        );

        spinnerItems = findViewById(R.id.spinnerItemsCompra);
        lista = findViewById(R.id.lvListaCompra);
        tvListaVacia = findViewById(R.id.tvListaCompraVacia);

        Button btnAnadir = findViewById(R.id.btnAnadirCompra);
        Button btnVolver = findViewById(R.id.btnVolverCompra);

        adapterSpinner = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_texto,
                items
        );
        adapterSpinner.setDropDownViewResource(R.layout.item_spinner_texto);
        spinnerItems.setAdapter(adapterSpinner);

        adapterLista = new ItemCompraAdapter(this, listaCompra);
        lista.setAdapter(adapterLista);
        lista.setEmptyView(tvListaVacia);

        btnAnadir.setOnClickListener(v -> anadirItem());
        btnVolver.setOnClickListener(v -> finish());

        lista.setOnItemLongClickListener((parent, view, position, id) -> {
            ItemCompra item = listaCompra.get(position);
            db.eliminarListaCompra(usuarioId, item.getId());
            cargarLista();

            Toast.makeText(
                    this,
                    R.string.articulo_eliminado,
                    Toast.LENGTH_SHORT
            ).show();

            return true;
        });

        cargarItems();
        cargarLista();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

    private void cargarItems() {
        items.clear();
        items.addAll(db.obtenerItemsCompra());
        adapterSpinner.notifyDataSetChanged();

        if (!items.isEmpty()) {
            spinnerItems.setSelection(0);
        }
    }

    private void cargarLista() {
        listaCompra.clear();
        listaCompra.addAll(db.obtenerListaCompra(usuarioId));
        adapterLista.notifyDataSetChanged();
    }

    private void anadirItem() {
        ItemCompra item = (ItemCompra) spinnerItems.getSelectedItem();

        if (item == null) {
            return;
        }

        boolean anadido = db.anadirListaCompra(usuarioId, item.getId());

        if (!anadido) {
            Toast.makeText(
                    this,
                    R.string.articulo_duplicado,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        cargarLista();

        Toast.makeText(
                this,
                R.string.articulo_anadido,
                Toast.LENGTH_SHORT
        ).show();
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
