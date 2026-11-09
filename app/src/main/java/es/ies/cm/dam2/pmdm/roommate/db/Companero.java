package es.ies.cm.dam2.pmdm.roommate.db;

import java.io.Serializable;

/**
 * Datos de un companero de piso.
 */
public class Companero implements Serializable {

    private final int id;
    private final String nombre;
    private final String apellidos;
    private final int edad;
    private final String sexo;
    private final String fechaEntrada;
    private final String introduccion;

    public Companero(
            String nombre,
            String apellidos,
            int edad,
            String sexo,
            String fechaEntrada,
            String introduccion
    ) {
        this(0, nombre, apellidos, edad, sexo, fechaEntrada, introduccion);
    }

    public Companero(
            int id,
            String nombre,
            String apellidos,
            int edad,
            String sexo,
            String fechaEntrada,
            String introduccion
    ) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.edad = edad;
        this.sexo = sexo;
        this.fechaEntrada = fechaEntrada;
        this.introduccion = introduccion;
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
