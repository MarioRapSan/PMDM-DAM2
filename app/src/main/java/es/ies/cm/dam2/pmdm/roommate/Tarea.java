package es.ies.cm.dam2.pmdm.roommate;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Datos de una tarea del piso.
 * Ahora se guarda en Room, en la tabla tareas.
 */
@Entity(tableName = "tareas")
public class Tarea implements Serializable {

    private static final long serialVersionUID = 1L;

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String nombre;
    private String prioridad;
    private String dia;
    private double dinero;
    private String hora;
    private String companero;

    // No se guarda en la base de datos: solo se usa para la interfaz.
    @Ignore
    private int fotoResId;

    private boolean hecha;
    private int dificultad;
    private String descripcion;

    public Tarea() {
        this(0, "", "", "", 0, "", "Sin asignar",
                R.drawable.ic_persona, false, 3, "");
    }

    @Ignore
    public Tarea(String nombre, String prioridad, String dia, double dinero, String hora) {
        this(0, nombre, prioridad, dia, dinero, hora, "Sin asignar",
                R.drawable.ic_persona, false, 3, "");
    }

    @Ignore
    public Tarea(String nombre, String prioridad, String dia, double dinero, String hora,
                 String companero, int fotoResId) {
        this(0, nombre, prioridad, dia, dinero, hora, companero,
                fotoResId, false, 3, "");
    }

    @Ignore
    public Tarea(int id, String nombre, String prioridad, String dia, double dinero,
                 String hora, String companero, int fotoResId, boolean hecha,
                 int dificultad, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.prioridad = prioridad;
        this.dia = dia;
        this.dinero = dinero;
        this.hora = hora;
        this.companero = companero;
        this.fotoResId = fotoResId;
        this.hecha = hecha;
        this.dificultad = dificultad;
        this.descripcion = descripcion;
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

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public double getDinero() {
        return dinero;
    }

    public void setDinero(double dinero) {
        this.dinero = dinero;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getCompanero() {
        return companero;
    }

    public void setCompanero(String companero) {
        this.companero = companero;
    }

    public int getFotoResId() {
        return fotoResId;
    }

    public void setFotoResId(int fotoResId) {
        this.fotoResId = fotoResId;
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
        this.descripcion = descripcion;
    }
}
