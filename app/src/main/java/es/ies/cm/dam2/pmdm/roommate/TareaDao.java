package es.ies.cm.dam2.pmdm.roommate;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * DAO de Room para la entidad principal (tabla tareas).
 * Contiene el CRUD completo y las consultas propias de la app.
 * Los metodos se llaman siempre fuera del hilo principal (AppDatabase.ejecutar).
 */
@Dao
public interface TareaDao {

    /** CREATE: inserta una tarea nueva y devuelve su id. */
    @Insert
    long insertar(Tarea tarea);

    /** Inserta conservando el id (solo para migrar datos antiguos de SQLite). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertarMigrada(Tarea tarea);

    /** UPDATE: guarda los cambios de una tarea (se localiza por su id). */
    @Update
    void actualizar(Tarea tarea);

    /** DELETE: borra la tarea. Sus registros se borran en cascada. */
    @Delete
    void borrar(Tarea tarea);

    /** READ: todas las tareas en orden de alta. */
    @Query("SELECT * FROM tareas ORDER BY id")
    List<Tarea> obtenerTodas();

    /** READ: una tarea por su id (para la ficha). */
    @Query("SELECT * FROM tareas WHERE id = :id")
    Tarea obtenerPorId(int id);

    /** Consulta propia: tareas cuyo nombre contiene el texto. */
    @Query("SELECT * FROM tareas WHERE nombre LIKE '%' || :texto || '%' ORDER BY nombre")
    List<Tarea> buscarPorNombre(String texto);

    /** Consulta propia: numero de tareas (para saber si hay que hacer la carga inicial). */
    @Query("SELECT COUNT(*) FROM tareas")
    int contarTareas();
}
