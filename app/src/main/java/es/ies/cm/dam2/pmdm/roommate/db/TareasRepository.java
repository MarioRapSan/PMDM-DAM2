
package es.ies.cm.dam2.pmdm.roommate.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class TareasRepository {

    private SQLiteDatabase db;
    private RoomMateDB dbHelper;

    public TareasRepository(Context context) {
        dbHelper = RoomMateDB.getInstance(context);
    }

    // CREATE: Insertar tarea
    // CREATE: Insertar varias tareas
    public boolean insertarTarea(ArrayList<Tarea> tareas) {

        db = dbHelper.getWritableDatabase();

        db.beginTransaction();

        try {
            for (Tarea tarea : tareas) {

                ContentValues values = new ContentValues();

                values.put(RoomMateContract.COLUMN_NOMBRE_TAREA, tarea.getNombre());
                values.put(RoomMateContract.COLUMN_PRIORIDAD_TAREA, tarea.getPrioridad());
                values.put(RoomMateContract.COLUMN_DIA_TAREA, tarea.getDia());
                values.put(RoomMateContract.COLUMN_DINERO_TAREA, tarea.getDinero());
                values.put(RoomMateContract.COLUMN_HORA_TAREA, tarea.getHora());
                values.put(RoomMateContract.COLUMN_COMPANERO_TAREA, tarea.getCompanero());
                values.put(RoomMateContract.COLUMN_HECHA_TAREA, tarea.isHecha() ? 1 : 0);
                values.put(RoomMateContract.COLUMN_DIFICULTAD_TAREA, tarea.getDificultad());
                values.put(RoomMateContract.COLUMN_DESCRIPCION_TAREA, tarea.getDescripcion());

                long resultado = db.insert(
                        RoomMateContract.TABLE_NAME_LISTA_TAREAS,
                        null,
                        values
                );

                if (resultado == -1) {
                    return false;
                }

                tarea.setId((int) resultado);
            }

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
        }
    }
    // READ: Obtener todas las tareas
    public ArrayList<Tarea> obtenerTareas() {

        ArrayList<Tarea> lista = new ArrayList<>();

        db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                RoomMateContract.TABLE_NAME_LISTA_TAREAS,
                null,
                null,
                null,
                null,
                null,
                RoomMateContract.COLUMN_ID_TAREA + " ASC"
        )) {
            if (cursor.moveToFirst()) {
                do {
                    Tarea tarea = new Tarea(
                            cursor.getInt(cursor.getColumnIndexOrThrow(RoomMateContract.COLUMN_ID_TAREA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(RoomMateContract.COLUMN_NOMBRE_TAREA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(RoomMateContract.COLUMN_PRIORIDAD_TAREA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(RoomMateContract.COLUMN_DIA_TAREA)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(RoomMateContract.COLUMN_DINERO_TAREA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(RoomMateContract.COLUMN_HORA_TAREA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(RoomMateContract.COLUMN_COMPANERO_TAREA)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(RoomMateContract.COLUMN_HECHA_TAREA)) == 1,
                            cursor.getInt(cursor.getColumnIndexOrThrow(RoomMateContract.COLUMN_DIFICULTAD_TAREA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(RoomMateContract.COLUMN_DESCRIPCION_TAREA))
                    );

                    lista.add(tarea);

                } while (cursor.moveToNext());
            }
        }

        return lista;
    }

    // UPDATE: Actualizar tarea
    public boolean actualizarTarea(Tarea tarea) {

        db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(RoomMateContract.COLUMN_NOMBRE_TAREA, tarea.getNombre());
        values.put(RoomMateContract.COLUMN_PRIORIDAD_TAREA, tarea.getPrioridad());
        values.put(RoomMateContract.COLUMN_DIA_TAREA, tarea.getDia());
        values.put(RoomMateContract.COLUMN_DINERO_TAREA, tarea.getDinero());
        values.put(RoomMateContract.COLUMN_HORA_TAREA, tarea.getHora());
        values.put(RoomMateContract.COLUMN_COMPANERO_TAREA, tarea.getCompanero());
        values.put(RoomMateContract.COLUMN_HECHA_TAREA, tarea.isHecha() ? 1 : 0);
        values.put(RoomMateContract.COLUMN_DIFICULTAD_TAREA, tarea.getDificultad());
        values.put(RoomMateContract.COLUMN_DESCRIPCION_TAREA, tarea.getDescripcion());

        int resultado = db.update(
                RoomMateContract.TABLE_NAME_LISTA_TAREAS,
                values,
                RoomMateContract.COLUMN_ID_TAREA + " = ?",
                new String[]{String.valueOf(tarea.getId())}
        );

        return resultado > 0;
    }

    // DELETE: Eliminar tarea
    public boolean eliminarTarea(int id) {

        db = dbHelper.getWritableDatabase();

        int resultado = db.delete(
                RoomMateContract.TABLE_NAME_LISTA_TAREAS,
                RoomMateContract.COLUMN_ID_TAREA + " = ?",
                new String[]{String.valueOf(id)}
        );

        return resultado > 0;
    }

    // Cerrar conexión
    public void cerrar() {
        // No cerrar aquí el helper Singleton,
        // porque se comparte con los demás repositorios.
    }
}
