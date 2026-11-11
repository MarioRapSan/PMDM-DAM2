package es.ies.cm.dam2.pmdm.roommate;

/**
 * Datos de un companero de piso.
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

    public String getNombreCompleto() {
        if (apellidos == null || apellidos.trim().isEmpty()) {
            return nombre;
        }
        return nombre + " " + apellidos;
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}
