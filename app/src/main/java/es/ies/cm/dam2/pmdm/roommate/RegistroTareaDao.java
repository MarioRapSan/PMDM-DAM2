package es.ies.cm.dam2.pmdm.roommate;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * DAO de Room para la entidad relacionada (tabla registros_tarea).
 * CRUD completo y consultas de los registros de cada tarea.
 */
@Dao
public interface RegistroTareaDao {

    /** CREATE: anade un registro a una tarea. */
    @Insert
    long insertar(RegistroTarea registro);

    /** UPDATE: modifica un registro existente. */
    @Update
    void actualizar(RegistroTarea registro);

    /** DELETE: borra un registro. */
    @Delete
    void borrar(RegistroTarea registro);

    /** READ: registros de una tarea, del mas reciente al mas antiguo. */
    @Query("SELECT * FROM registros_tarea WHERE tareaId = :tareaId ORDER BY fecha DESC")
    List<RegistroTarea> obtenerPorTarea(int tareaId);

    /** Consulta propia: cuantos registros tiene una tarea. */
    @Query("SELECT COUNT(*) FROM registros_tarea WHERE tareaId = :tareaId")
    int contarPorTarea(int tareaId);

    /** Consulta propia: total de registros de todas las tareas. */
    @Query("SELECT COUNT(*) FROM registros_tarea")
    int contarTotal();
}
