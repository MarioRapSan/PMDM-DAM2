package es.ies.cm.dam2.pmdm.roommate;

/**
 * Producto que se puede anadir a la lista de la compra.
 */
public class ItemCompra {

    private final int id;
    private final String nombre;
    private final String categoria;

    public ItemCompra(int id, String nombre, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
