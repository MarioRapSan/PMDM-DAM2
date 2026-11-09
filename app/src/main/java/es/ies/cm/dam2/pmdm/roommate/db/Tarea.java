package es.ies.cm.dam2.pmdm.roommate.db;

import java.io.Serializable;

/**
 * Datos de una tarea del piso.
 */
public class Tarea implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private final String nombre;
    private final String prioridad;
    private final String dia;
    private final double dinero;
    private final String hora;
    private final String companero;
    private boolean hecha;
    private int dificultad;
    private String descripcion;

    public Tarea(String nombre, String prioridad, String dia, double dinero, String hora) {
        this(0, nombre, prioridad, dia, dinero, hora, "Sin asignar", false, 3, "");
    }

    public Tarea(String nombre, String prioridad, String dia, double dinero, String hora,
                 String companero) {
        this(0, nombre, prioridad, dia, dinero, hora, companero
               , false, 3, "");
    }

    public Tarea(String nombre, String prioridad, String dia, double dinero, String hora,
                 String companero, boolean hecha) {
        this(0, nombre, prioridad, dia, dinero, hora, companero, hecha, 3, "");
    }

    public Tarea(
            int id,
            String nombre,
            String prioridad,
            String dia,
            double dinero,
            String hora,
            String companero,
            boolean hecha,
            int dificultad,
            String descripcion
    ) {
        this.id = id;
        this.nombre = nombre;
        this.prioridad = prioridad;
        this.dia = dia;
        this.dinero = dinero;
        this.hora = hora;
        this.companero = companero;
        this.hecha = hecha;
        this.dificultad = dificultad;
        this.descripcion = descripcion == null ? "" : descripcion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public String getDia() {
        return dia;
    }

    public double getDinero() {
        return dinero;
    }

    public String getHora() {
        return hora;
    }

    public String getCompanero() {
        return companero;
    }


    public boolean isHecha() {
        return hecha;
    }

    public void setHecha(boolean hecha) {
        this.hecha = hecha;
    }

    public int getDificultad() {
        return dificultad;
    }

    public void setDificultad(int dificultad) {
        this.dificultad = dificultad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion == null ? "" : descripcion;
    }

    public static int dificultadPorPrioridad(String prioridad) {
        if (prioridad == null) {
            return 3;
        }

        switch (prioridad) {
            case "Urgente":
                return 5;
            case "Hacer cuanto antes":
                return 4;
            case "Puede esperar":
                return 2;
            default:
                return 3;
        }
    }

    @Override
    public String toString() {
        return nombre + " | " + prioridad + " | " + dia + " | " + hora
                + " | " + dinero + " euros | " + companero
                + " | Dificultad: " + dificultad
                + " | " + (hecha ? "Hecha" : "No hecha");
    }
}
