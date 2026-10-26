package es.ies.cm.dam2.pmdm.roommate;

class ListaTareaPrioridad {

    private final String nombre;
    private final String prioridad;
    private final String companero;
    private final String estado;

    public ListaTareaPrioridad(String nombre, String prioridad) {
        this(nombre, prioridad, null, null);
    }

    public ListaTareaPrioridad(
            String nombre,
            String prioridad,
            String companero,
            String estado
    ) {
        this.nombre = nombre;
        this.prioridad = prioridad;
        this.companero = companero;
        this.estado = estado;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPrioridad() {
        return prioridad;
    }

    @Override
    public String toString() {
        if (companero == null) {
            return nombre;
        }

        return nombre + " · " + prioridad + " · " + companero + " · " + estado;
    }
}