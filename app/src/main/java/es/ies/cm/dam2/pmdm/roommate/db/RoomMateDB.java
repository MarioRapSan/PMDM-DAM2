package es.ies.cm.dam2.pmdm.roommate.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class RoomMateDB extends SQLiteOpenHelper {
    public static final String DB_NAME = "instituto.db";
    public static final int DB_VERSION = 1;
    private static RoomMateDB instance;


    public RoomMateDB(Context context){
        super(context.getApplicationContext(), DB_NAME,null,DB_VERSION);
    }
    public static synchronized RoomMateDB getInstance(Context context){
        if(instance == null){
            instance = new RoomMateDB(context);
        }
        return instance;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        // Tabla usuarios
        db.execSQL("CREATE TABLE " + RoomMateContract.TABLE_NAME_USER + " (" +
                RoomMateContract.COLUMN_USER + " TEXT PRIMARY KEY, " +
                RoomMateContract.COLUMN_PASSWORD + " TEXT NOT NULL" +
                ")");

// Tabla listaCompra
        db.execSQL("CREATE TABLE " + RoomMateContract.TABLE_NAME_LISTA_PRODUCTOS + " (" +
                RoomMateContract.COLUMN_ID_PRODUCTO + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RoomMateContract.COLUMN_NOMBRE_PRODUCTO + " TEXT NOT NULL, " +
                RoomMateContract.COLUMN_CATEGORIA_PRODUCTO + " TEXT, " +
                RoomMateContract.COLUMN_PRECIO_PRODUCTO + " REAL" +
                ")");

// Tabla companeros
        db.execSQL("CREATE TABLE " + RoomMateContract.TABLE_NAME_COMPANERO + " (" +
                RoomMateContract.COLUMN_NOMBRE_COMPANERO + " TEXT PRIMARY KEY, " +
                RoomMateContract.COLUMN_EDAD_COMPANERO + " INTEGER, " +
                RoomMateContract.COLUMN_SEXO_COMPANERO + " TEXT, " +
                RoomMateContract.COLUMN_DESCRIPCION_COMPANERO + " TEXT" +
                ")");

// Tabla listaTareas
        db.execSQL("CREATE TABLE " + RoomMateContract.TABLE_NAME_LISTA_TAREAS + " (" +
                RoomMateContract.COLUMN_ID_TAREA + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RoomMateContract.COLUMN_NOMBRE_TAREA + " TEXT NOT NULL, " +
                RoomMateContract.COLUMN_PRIORIDAD_TAREA + " TEXT, " +
                RoomMateContract.COLUMN_DIA_TAREA + " TEXT, " +
                RoomMateContract.COLUMN_DINERO_TAREA + " REAL, " +
                RoomMateContract.COLUMN_HORA_TAREA + " TEXT, " +
                RoomMateContract.COLUMN_COMPANERO_TAREA + " TEXT, " +
                RoomMateContract.COLUMN_HECHA_TAREA + " INTEGER DEFAULT 0, " +
                RoomMateContract.COLUMN_DIFICULTAD_TAREA + " INTEGER DEFAULT 3, " +
                RoomMateContract.COLUMN_DESCRIPCION_TAREA + " TEXT" +
                ")");
        db.execSQL("CREATE INDEX idx_usuarios ON " +
                RoomMateContract.TABLE_NAME_USER + " (" +
                RoomMateContract.COLUMN_USER + ")");

        db.execSQL("CREATE INDEX idx_listaCompra ON " +
                RoomMateContract.TABLE_NAME_LISTA_PRODUCTOS + " (" +
                RoomMateContract.COLUMN_ID_PRODUCTO + ")");

        db.execSQL("CREATE INDEX idx_companeros ON " +
                RoomMateContract.TABLE_NAME_COMPANERO + " (" +
                RoomMateContract.COLUMN_NOMBRE_COMPANERO + ")");

        db.execSQL("CREATE INDEX idx_listaTareas ON " +
                RoomMateContract.TABLE_NAME_LISTA_TAREAS + " (" +
                RoomMateContract.COLUMN_NOMBRE_TAREA + ")");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
