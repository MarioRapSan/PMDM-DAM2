package es.ies.cm.dam2.pmdm.roommate;

/**
 * Usuario que puede entrar en la aplicacion.
 */
public class Usuario {

    private final int id;
    private final String nombre;
    private final String clave;

    /** Usuario leido de la tabla usuarios. */
    public Usuario(int id, String nombre, String clave) {
        this.id = id;
        this.nombre = nombre;
        this.clave = clave;
    }

    // Getters y setters: acceso a los atributos privados.
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getClave() {
        return clave;
    }

    /** Muestra el nombre del usuario. */
    @Override
    public String toString() {
        return nombre;
    }
}
