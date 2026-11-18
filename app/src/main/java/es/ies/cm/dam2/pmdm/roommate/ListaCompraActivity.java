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
 * Lista personal (lista de la compra) del usuario que ha iniciado sesion.
 *
 * Los productos se eligen de la tabla items_compra (SQLite) y la relacion
 * usuario-producto se guarda en lista_compra, por lo que cada usuario tiene su
 * propia lista. Clic largo sobre un producto: se quita de la lista.
 * Tambien se gestiona el catalogo: anadir productos nuevos (con un DialogFragment
 * con formulario) y borrar el producto elegido (con confirmacion).
 * Todas las consultas se hacen fuera del hilo principal.
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

    private static final String CLAVE_CONFIRMAR_BORRAR_PRODUCTO = "confirmar_borrar_producto";

    /** Producto pendiente de confirmar su borrado del catalogo. */
    private ItemCompra productoABorrar;

    private int usuarioId;
    private String usuarioNombre;

    /**
     * Comprueba el usuario, prepara el Spinner, la lista, los botones del catalogo y los
     * dialogos.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_compra);
        aplicarInsets();

        db = new RoomMateDB(this);

        // El id del usuario llega por el Intent desde la pantalla anterior.
        usuarioId = getIntent().getIntExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, -1);
        usuarioNombre = getIntent().getStringExtra(ColeccionTareasActivity.EXTRA_USUARIO_NOMBRE);
        if (usuarioId == -1 || usuarioNombre == null) {
            AjustesActivity.irAlLogin(this);
            finish();
            return;
        }

        TextView tvTitulo = findViewById(R.id.tvTituloCompra);
        tvTitulo.setText(getString(R.string.lista_compra_de_usuario, usuarioNombre));

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

        // Catalogo: nuevo producto (dialogo con formulario) y borrar producto.
        Button btnNuevoProducto = findViewById(R.id.btnNuevoProducto);
        Button btnBorrarProducto = findViewById(R.id.btnBorrarProducto);

        btnNuevoProducto.setOnClickListener(v ->
                new NuevoProductoDialogFragment()
                        .show(getSupportFragmentManager(), "dialogo_nuevo_producto"));

        btnBorrarProducto.setOnClickListener(v -> {
            productoABorrar = (ItemCompra) spinnerItems.getSelectedItem();
            if (productoABorrar == null) {
                return;
            }
            ConfirmacionDialogFragment.nuevo(
                    CLAVE_CONFIRMAR_BORRAR_PRODUCTO,
                    R.string.borrar_del_catalogo,
                    R.string.mensaje_confirmar_borrar_producto,
                    R.string.eliminar
            ).show(getSupportFragmentManager(), "dialogo_borrar_producto");
        });

        getSupportFragmentManager().setFragmentResultListener(
                NuevoProductoDialogFragment.REQUEST_KEY, this, (clave, resultado) ->
                        crearProducto(
                                resultado.getString(NuevoProductoDialogFragment.RESULTADO_NOMBRE),
                                resultado.getString(NuevoProductoDialogFragment.RESULTADO_CATEGORIA)));

        getSupportFragmentManager().setFragmentResultListener(
                CLAVE_CONFIRMAR_BORRAR_PRODUCTO, this, (clave, resultado) -> {
                    if (resultado.getBoolean(ConfirmacionDialogFragment.RESULTADO_CONFIRMADO)
                            && productoABorrar != null) {
                        borrarProducto(productoABorrar);
                    }
                    productoABorrar = null;
                });
        btnVolver.setOnClickListener(v -> finish());

        lista.setOnItemLongClickListener((parent, view, position, id) -> {
            ItemCompra item = listaCompra.get(position);
            AppDatabase.ejecutar(() -> {
                db.eliminarListaCompra(usuarioId, item.getId());
                runOnUiThread(() -> {
                    cargarLista();
                    Toast.makeText(this, R.string.articulo_eliminado, Toast.LENGTH_SHORT).show();
                });
            });
            return true;
        });

        cargarItems();
        cargarLista();
    }

    /** Cierra la conexion con la base de datos al salir de la pantalla. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

    /** Carga el catalogo de productos para el Spinner. */
    private void cargarItems() {
        AppDatabase.ejecutar(() -> {
            List<ItemCompra> catalogo = db.obtenerItemsCompra();
            runOnUiThread(() -> {
                items.clear();
                items.addAll(catalogo);
                adapterSpinner.notifyDataSetChanged();
            });
        });
    }

    /** Carga la lista personal del usuario (JOIN entre items_compra y lista_compra). */
    private void cargarLista() {
        AppDatabase.ejecutar(() -> {
            List<ItemCompra> deUsuario = db.obtenerListaCompra(usuarioId);
            runOnUiThread(() -> {
                listaCompra.clear();
                listaCompra.addAll(deUsuario);
                adapterLista.notifyDataSetChanged();
            });
        });
    }

    /** INSERT en el catalogo y selecciona el producto nuevo en el Spinner. */
    private void crearProducto(String nombre, String categoria) {
        AppDatabase.ejecutar(() -> {
            long id = db.insertarItemCompra(nombre, categoria);
            List<ItemCompra> catalogo = db.obtenerItemsCompra();
            runOnUiThread(() -> {
                if (id == -1) {
                    Toast.makeText(this, R.string.error_producto_existe, Toast.LENGTH_SHORT).show();
                    return;
                }
                items.clear();
                items.addAll(catalogo);
                adapterSpinner.notifyDataSetChanged();
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).getId() == id) {
                        spinnerItems.setSelection(i);
                        break;
                    }
                }
                Toast.makeText(this, getString(R.string.producto_creado, nombre),
                        Toast.LENGTH_SHORT).show();
            });
        });
    }

    /** DELETE del catalogo; se recargan el Spinner y la lista (por el CASCADE). */
    private void borrarProducto(ItemCompra producto) {
        AppDatabase.ejecutar(() -> {
            int borrados = db.borrarItemCompra(producto.getId());
            runOnUiThread(() -> {
                if (borrados > 0) {
                    cargarItems();
                    cargarLista();
                    Toast.makeText(this, getString(R.string.producto_borrado, producto.getNombre()),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /** Anade el producto elegido a la lista; si ya estaba, avisa. */
    private void anadirItem() {
        ItemCompra item = (ItemCompra) spinnerItems.getSelectedItem();
        if (item == null) {
            return;
        }

        AppDatabase.ejecutar(() -> {
            boolean anadido = db.anadirListaCompra(usuarioId, item.getId());
            runOnUiThread(() -> {
                if (!anadido) {
                    Toast.makeText(this, R.string.articulo_duplicado, Toast.LENGTH_SHORT).show();
                    return;
                }
                cargarLista();
                Toast.makeText(this, R.string.articulo_anadido, Toast.LENGTH_SHORT).show();
            });
        });
    }

    /** Anade al padding el espacio de las barras del sistema (modo edge-to-edge). */
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
