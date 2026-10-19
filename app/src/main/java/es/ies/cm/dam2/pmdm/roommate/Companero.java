
package es.ies.cm.dam2.pmdm.roommate;

public class Companero {

    private String nombre;
    private String sexo;
    private int edad;
    private boolean lgtbFriendly;
    private String introduccion;

    public Companero(String nombre, String sexo, int edad,
                     boolean lgtbFriendly, String introduccion) {
        this.nombre = nombre;
        this.sexo = sexo;
        this.edad = edad;
        this.lgtbFriendly = lgtbFriendly;
        this.introduccion = introduccion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSexo() {
        return sexo;
    }

    public int getEdad() {
        return edad;
    }

    public boolean isLgtbFriendly() {
        return lgtbFriendly;
    }

    public String getIntroduccion() {
        return introduccion;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
