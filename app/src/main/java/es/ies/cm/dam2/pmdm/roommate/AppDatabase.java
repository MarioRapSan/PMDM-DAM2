package es.ies.cm.dam2.pmdm.roommate;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base de datos Room de la aplicacion (fichero roommate_room.db).
 *
 * Guarda la entidad principal (Tarea) y su entidad relacionada (RegistroTarea),
 * unidas 1-N con clave ajena y ON DELETE CASCADE. Los usuarios, los companeros
 * y la lista de la compra personal se guardan en SQLite (RoomMateDB).
 *
 * Todas las consultas se lanzan fuera del hilo principal con {@link #ejecutar(Runnable)}.
 */
@Database(
        entities = {Tarea.class, RegistroTarea.class},
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String NOMBRE_BD_ROOM = "roommate_room.db";
    private static final String PREFS = "roommate_prefs";
    private static final String CLAVE_MIGRACION = "tareas_migradas_room";

    private static volatile AppDatabase instancia;

    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    /**
     * Version 1 -> 2: se anade la columna "compartida" a las tareas.
     * Asi los datos que ya tenia el usuario no se pierden al actualizar la app.
     */
    static final Migration MIGRACION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE tareas ADD COLUMN compartida INTEGER NOT NULL DEFAULT 0");
        }
    };

    /** DAO de la tabla tareas (lo genera Room). */
    public abstract TareaDao tareaDao();

    /** DAO de la tabla registros_tarea (lo genera Room). */
    public abstract RegistroTareaDao registroTareaDao();

    /** Devuelve la unica instancia de la base de datos (patron Singleton). */
    public static AppDatabase getInstance(Context context) {
        if (instancia == null) {
            synchronized (AppDatabase.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            NOMBRE_BD_ROOM
                    ).addMigrations(MIGRACION_1_2).build();
                }
            }
        }
        return instancia;
    }

    /** Ejecuta un trabajo de base de datos en un hilo secundario. */
    public static void ejecutar(Runnable tarea) {
        executor.execute(tarea);
    }

    /**
     * Migra las tareas antiguas de SQLite a Room y crea las tareas iniciales.
     */
    public static void inicializar(Context context, Runnable alTerminar) {
        Context app = context.getApplicationContext();

        ejecutar(() -> {
            try {
                migrarTareasDesdeSqlite(app);
                sembrarTareasIniciales(app);
            } finally {
                if (alTerminar != null) {
                    new Handler(Looper.getMainLooper()).post(alTerminar);
                }
            }
        });
    }

    /** Copia a Room las tareas de la tabla antigua de SQLite. Solo se hace una vez. */
    private static void migrarTareasDesdeSqlite(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        if (prefs.getBoolean(CLAVE_MIGRACION, false)) {
            return;
        }

        File archivo = context.getDatabasePath(RoomMateDB.NOMBRE_BD);

        // Si no existe la base de datos antigua, no hay nada que migrar.
        if (!archivo.exists()) {
            prefs.edit().putBoolean(CLAVE_MIGRACION, true).apply();
            return;
        }

        RoomMateDB db = new RoomMateDB(context);
        SQLiteDatabase sqlite = db.getReadableDatabase();

        try {
            Cursor tabla = sqlite.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='tareas'",
                    null
            );
            boolean existeTabla = tabla.moveToFirst();
            tabla.close();

            if (existeTabla) {
                Cursor c = sqlite.rawQuery("SELECT * FROM tareas", null);
                TareaDao dao = getInstance(context).tareaDao();

                int colId = c.getColumnIndex("id");
                if (colId == -1) {
                    colId = c.getColumnIndex("_id");
                }

                while (c.moveToNext()) {
                    Tarea tarea = new Tarea();

                    if (colId != -1) {
                        tarea.setId(c.getInt(colId));
                    }

                    tarea.setNombre(leerTexto(c, "nombre", ""));
                    tarea.setPrioridad(leerTexto(c, "prioridad", ""));
                    tarea.setDia(leerTexto(c, "dia", ""));
                    tarea.setDinero(leerDouble(c, "dinero", 0));
                    tarea.setHora(leerTexto(c, "hora", ""));
                    tarea.setCompanero(leerTexto(c, "companero", "Sin asignar"));
                    tarea.setFotoResId(R.drawable.ic_persona);
                    tarea.setHecha(leerInt(c, "hecha", 0) == 1);
                    tarea.setDificultad(leerInt(c, "dificultad", 3));
                    tarea.setDescripcion(leerTexto(c, "descripcion", ""));

                    dao.insertarMigrada(tarea);
                }

                c.close();

                // Una vez copiadas, la tabla antigua ya no se necesita.
                sqlite.execSQL("DROP TABLE IF EXISTS tareas");
            }

            prefs.edit().putBoolean(CLAVE_MIGRACION, true).apply();
        } finally {
            db.close();
        }
    }

    /**
     * Carga inicial: si la tabla de tareas esta vacia (primera vez que se abre
     * la app en una instalacion limpia) se crean 6 tareas de ejemplo y un registro
     * asociado a la primera, para que se vea la relacion 1-N.
     * Es un punto de partida: luego se pueden anadir, editar y borrar.
     */
    private static void sembrarTareasIniciales(Context context) {
        AppDatabase bd = getInstance(context);
        TareaDao dao = bd.tareaDao();

        if (dao.contarTareas() > 0) {
            return;
        }

        int ic = R.drawable.ic_persona;

        Tarea bano = new Tarea(0, "Limpiar baño", "Hacer cuanto antes",
                "23/11/2026", 0, "10:00", "Jose", ic,
                false, 3, "Limpiar el baño con lejía y dejar los útiles recogidos.");
        long idBano = dao.insertar(bano);

        dao.insertar(new Tarea(0, "Sacar la basura", "Urgente",
                "19/11/2026", 0, "21:30", "Raul", ic,
                false, 1, "Bajar orgánico, plástico y papel a los contenedores."));

        Tarea compra = new Tarea(0, "Comprar productos de limpieza", "Puede esperar",
                "28/11/2026", 12.50, "12:00", "Winwaleo", ic,
                false, 2, "Comprar lejía, friegasuelos y bayetas.");
        compra.setCompartida(true);
        dao.insertar(compra);

        Tarea luz = new Tarea(0, "Pagar la factura de la luz", "Hacer cuanto antes",
                "30/11/2026", 64.20, "18:00", "Raul", ic,
                false, 2, "Dividir el importe entre los tres compañeros.");
        luz.setCompartida(true);
        dao.insertar(luz);

        dao.insertar(new Tarea(0, "Ordenar el armario", "Puede esperar",
                "15/11/2026", 0, "17:00", "Jose", ic,
                true, 2, "Ordenar la ropa y separar lo que ya no se usa."));

        dao.insertar(new Tarea(0, "Fregar los platos", "Urgente",
                "17/11/2026", 0, "22:00", "Winwaleo", ic,
                true, 1, "Fregar y secar los platos de la cena."));

        // Relacion de ejemplo: un registro de la tarea "Limpiar baño".
        bd.registroTareaDao().insertar(
                new RegistroTarea((int) idBano, "16/11/2026", 8, "Jose"));
    }

    /** Lee una columna de texto del cursor; si no existe, devuelve el valor por defecto. */
    private static String leerTexto(Cursor c, String columna, String defecto) {
        int indice = c.getColumnIndex(columna);
        return indice == -1 ? defecto : c.getString(indice);
    }

    /** Lee una columna entera del cursor; si no existe, devuelve el valor por defecto. */
    private static int leerInt(Cursor c, String columna, int defecto) {
        int indice = c.getColumnIndex(columna);
        return indice == -1 ? defecto : c.getInt(indice);
    }

    /** Lee una columna decimal del cursor; si no existe, devuelve el valor por defecto. */
    private static double leerDouble(Cursor c, String columna, double defecto) {
        int indice = c.getColumnIndex(columna);
        return indice == -1 ? defecto : c.getDouble(indice);
    }
}
