package es.ies.cm.dam2.pmdm.roommate;

/**
 * Usuario que puede entrar en la aplicacion.
 */
public class Usuario {

    private final int id;
    private final String nombre;
    private final String clave;

    public Usuario(int id, String nombre, String clave) {
        this.id = id;
        this.nombre = nombre;
        this.clave = clave;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getClave() {
        return clave;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
