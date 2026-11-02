package es.ies.cm.dam2.pmdm.roommate;

import java.io.Serializable;

public class Tarea implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final String prioridad;
    private final String dia;
    private final double dinero;
    private final String hora;
    private final String companero;
    private final int fotoResId;
    private boolean hecha;
    private int dificultad;

    public Tarea(String nombre, String prioridad, String dia, double dinero, String hora) {
        this(nombre, prioridad, dia, dinero, hora, "Sin asignar", 0, false);
    }

    public Tarea(String nombre, String prioridad, String dia, double dinero, String hora,
                 String companero, int fotoResId) {
        this(nombre, prioridad, dia, dinero, hora, companero, fotoResId, false);
    }

    public Tarea(String nombre, String prioridad, String dia, double dinero, String hora,
                 String companero, int fotoResId, boolean hecha) {
        this.nombre = nombre;
        this.prioridad = prioridad;
        this.dia = dia;
        this.dinero = dinero;
        this.hora = hora;
        this.companero = companero;
        this.fotoResId = fotoResId;
        this.hecha = hecha;
        this.dificultad = 3;
    }

    public String getNombre() { return nombre; }
    public String getPrioridad() { return prioridad; }
    public String getDia() { return dia; }
    public double getDinero() { return dinero; }
    public String getHora() { return hora; }
    public String getCompanero() { return companero; }
    public int getFotoResId() { return fotoResId; }
    public boolean isHecha() { return hecha; }
    public void setHecha(boolean hecha) { this.hecha = hecha; }
    public int getDificultad() { return dificultad; }
    public void setDificultad(int dificultad) { this.dificultad = dificultad; }

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
                + " | " + dinero + " € | " + companero
                + " | Dificultad: " + dificultad
                + " | " + (hecha ? "Hecha" : "No hecha");
    }
}