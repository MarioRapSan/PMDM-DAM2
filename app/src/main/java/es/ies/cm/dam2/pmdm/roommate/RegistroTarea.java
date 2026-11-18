package es.ies.cm.dam2.pmdm.roommate;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Registro de una tarea realizada: fecha, puntos obtenidos y companero que la hizo
 * (campo "ganador").
 *
 * Es la entidad relacionada de Tarea en Room (relacion 1-N): cada registro
 * pertenece a una tarea mediante la clave ajena tareaId. Con ON DELETE CASCADE,
 * al borrar una tarea se borran automaticamente todos sus registros.
 */
@Entity(
        tableName = "registros_tarea",
        foreignKeys = @ForeignKey(
                entity = Tarea.class,
                parentColumns = "id",
                childColumns = "tareaId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("tareaId")}
)
public class RegistroTarea {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int tareaId;
    private String fecha;
    private int puntuacion;
    private String ganador;

    /** Constructor vacio que necesita Room para crear los objetos al leer la tabla. */
    public RegistroTarea() {
    }

    /** Registro nuevo de una tarea (el id lo asigna Room). */
    @Ignore
    public RegistroTarea(int tareaId, String fecha, int puntuacion, String ganador) {
        this.tareaId = tareaId;
        this.fecha = fecha;
        this.puntuacion = puntuacion;
        this.ganador = ganador;
    }

    // Getters y setters: acceso a los atributos privados.
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTareaId() {
        return tareaId;
    }

    public void setTareaId(int tareaId) {
        this.tareaId = tareaId;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }

    public String getGanador() {
        return ganador;
    }

    public void setGanador(String ganador) {
        this.ganador = ganador;
    }
}
