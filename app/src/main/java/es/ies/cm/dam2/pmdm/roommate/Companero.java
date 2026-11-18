package es.ies.cm.dam2.pmdm.roommate;

/**
 * Datos de un companero de piso (tabla companeros de SQLite).
 *
 * Cada perfil puede estar ligado a una cuenta de usuario (usuarioId). Solo ese
 * usuario puede editar o borrar su perfil; para los demas es de solo lectura.
 */
public class Companero {

    private final int id;
    private final String nombre;
    private final String apellidos;
    private final int edad;
    private final String sexo;
    private final String fechaEntrada;
    private final String introduccion;
    private final String fotoPerfil;
    private final String telefono;
    private final String correo;

    /** Usuario al que pertenece este perfil (0 si no esta ligado a ninguna cuenta). */
    private int usuarioId;

    /** Companero nuevo sin datos de contacto (el id lo asigna la base de datos). */
    public Companero(
            String nombre,
            String apellidos,
            int edad,
            String sexo,
            String fechaEntrada,
            String introduccion,
            String fotoPerfil
    ) {
        this(
                0,
                nombre,
                apellidos,
                edad,
                sexo,
                fechaEntrada,
                introduccion,
                fotoPerfil,
                "",
                ""
        );
    }

    /** Companero nuevo con datos de contacto (el id lo asigna la base de datos). */
    public Companero(
            String nombre,
            String apellidos,
            int edad,
            String sexo,
            String fechaEntrada,
            String introduccion,
            String fotoPerfil,
            String telefono,
            String correo
    ) {
        this(
                0,
                nombre,
                apellidos,
                edad,
                sexo,
                fechaEntrada,
                introduccion,
                fotoPerfil,
                telefono,
                correo
        );
    }

    /** Companero ya guardado, sin datos de contacto. */
    public Companero(
            int id,
            String nombre,
            String apellidos,
            int edad,
            String sexo,
            String fechaEntrada,
            String introduccion,
            String fotoPerfil
    ) {
        this(
                id,
                nombre,
                apellidos,
                edad,
                sexo,
                fechaEntrada,
                introduccion,
                fotoPerfil,
                "",
                ""
        );
    }

    /** Companero completo, tal y como se lee de la base de datos. */
    public Companero(
            int id,
            String nombre,
            String apellidos,
            int edad,
            String sexo,
            String fechaEntrada,
            String introduccion,
            String fotoPerfil,
            String telefono,
            String correo
    ) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.edad = edad;
        this.sexo = sexo;
        this.fechaEntrada = fechaEntrada;
        this.introduccion = introduccion;
        this.fotoPerfil = fotoPerfil;
        this.telefono = telefono == null ? "" : telefono;
        this.correo = correo == null ? "" : correo;
    }

    // Getters y setters: acceso a los atributos privados.
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public int getEdad() {
        return edad;
    }

    public String getSexo() {
        return sexo;
    }

    public String getFechaEntrada() {
        return fechaEntrada;
    }

    public String getIntroduccion() {
        return introduccion;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    /** Nombre y apellidos juntos; si no tiene apellidos, solo el nombre. */
    public String getNombreCompleto() {
        if (apellidos == null || apellidos.trim().isEmpty()) {
            return nombre;
        }
        return nombre + " " + apellidos;
    }

    /** El Spinner muestra el nombre completo del companero. */
    @Override
    public String toString() {
        return getNombreCompleto();
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    /** Devuelve true si este perfil pertenece al usuario indicado. */
    public boolean esDe(int idUsuario) {
        return usuarioId > 0 && usuarioId == idUsuario;
    }
}
