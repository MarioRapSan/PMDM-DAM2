package es.ies.cm.dam2.pmdm.roommate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Base de datos local de la aplicacion.
 * Guarda companeros, usuarios y la lista de la compra.
 * Las tareas y sus registros se guardan en Room.
 */
public class RoomMateDB extends SQLiteOpenHelper {

    public static final String NOMBRE_BD = "roommate.db";
    public static final int VERSION_BD = 2;

    private static final String TABLA_COMPANEROS = "companeros";
    private static final String TABLA_USUARIOS = "usuarios";
    private static final String TABLA_ITEMS_COMPRA = "items_compra";
    private static final String TABLA_LISTA_COMPRA = "lista_compra";

    public RoomMateDB(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Activa las relaciones entre tablas para que las claves foraneas funcionen.
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        crearTablas(db);
        insertarDatosIniciales(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // La version 2 anade los datos de contacto de cada companero.
        if (oldVersion < 2) {
            if (!tieneColumna(db, TABLA_COMPANEROS, "telefono")) {
                db.execSQL(
                        "ALTER TABLE " + TABLA_COMPANEROS +
                                " ADD COLUMN telefono TEXT"
                );
            }

            if (!tieneColumna(db, TABLA_COMPANEROS, "correo")) {
                db.execSQL(
                        "ALTER TABLE " + TABLA_COMPANEROS +
                                " ADD COLUMN correo TEXT"
                );
            }
        }
    }

    private boolean tieneColumna(
            SQLiteDatabase db,
            String tabla,
            String columna
    ) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tabla + ")", null);
        int indiceNombre = cursor.getColumnIndex("name");

        try {
            while (cursor.moveToNext()) {
                if (columna.equals(cursor.getString(indiceNombre))) {
                    return true;
                }
            }
        } finally {
            cursor.close();
        }

        return false;
    }

    private void crearTablas(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLA_COMPANEROS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre TEXT NOT NULL, " +
                        "apellidos TEXT, " +
                        "edad INTEGER, " +
                        "sexo TEXT, " +
                        "fecha_entrada TEXT, " +
                        "introduccion TEXT, " +
                        "foto_perfil TEXT, " +
                        "telefono TEXT, " +
                        "correo TEXT)"
        );

        db.execSQL(
                "CREATE TABLE " + TABLA_USUARIOS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre TEXT NOT NULL UNIQUE, " +
                        "clave TEXT NOT NULL)"
        );

        // Tabla auxiliar con los productos que se pueden elegir.
        db.execSQL(
                "CREATE TABLE " + TABLA_ITEMS_COMPRA + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre TEXT NOT NULL UNIQUE, " +
                        "categoria TEXT)"
        );

        db.execSQL(
                "CREATE TABLE " + TABLA_LISTA_COMPRA + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "usuario_id INTEGER NOT NULL, " +
                        "item_id INTEGER NOT NULL, " +
                        "UNIQUE(usuario_id, item_id), " +
                        "FOREIGN KEY(usuario_id) REFERENCES " + TABLA_USUARIOS + "(id) " +
                        "ON DELETE CASCADE, " +
                        "FOREIGN KEY(item_id) REFERENCES " + TABLA_ITEMS_COMPRA + "(id) " +
                        "ON DELETE CASCADE)"
        );
}

    private void insertarDatosIniciales(SQLiteDatabase db) {
        db.beginTransaction();

        try {
            insertarUsuario(db, "admin", "admin");
            insertarUsuario(db, "mario", "mario");

            insertarItemsCompra(db);
            insertarCompanerosIniciales(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void insertarUsuario(SQLiteDatabase db, String nombre, String clave) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("clave", clave);
        db.insert(TABLA_USUARIOS, null, valores);
    }

    private void insertarItemsCompra(SQLiteDatabase db) {
        insertarItem(db, "Leche", "Lácteos");
        insertarItem(db, "Pan", "Panadería");
        insertarItem(db, "Huevos", "Lácteos");
        insertarItem(db, "Manzanas", "Frutas");
        insertarItem(db, "Arroz", "Despensa");
        insertarItem(db, "Pasta", "Despensa");
        insertarItem(db, "Tomates", "Verduras");
        insertarItem(db, "Detergente", "Limpieza");
        insertarItem(db, "Papel higiénico", "Hogar");
        insertarItem(db, "Aceite de oliva", "Despensa");
        insertarItem(db, "Café", "Despensa");
        insertarItem(db, "Yogur", "Lácteos");
    }

    private void insertarItem(SQLiteDatabase db, String nombre, String categoria) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("categoria", categoria);
        db.insert(TABLA_ITEMS_COMPRA, null, valores);
    }

    private void insertarCompanerosIniciales(SQLiteDatabase db) {
        insertarCompaneroInicial(
                db, "Jose", "García", 24, "Hombre",
                "01/09/2024", "Me gusta cocinar y ver series.", "ic_persona",
                "600 111 222", "jose@roommate.es"
        );
        insertarCompaneroInicial(
                db, "Raul", "López", 27, "Hombre",
                "01/09/2023", "Soy ordenado y suelo ayudar con la limpieza.", "ic_persona",
                "600 333 444", "raul@roommate.es"
        );
        insertarCompaneroInicial(
                db, "Winwaleo", "Martínez", 22, "Mujer",
                "15/01/2025", "Estudio y trabajo, pero siempre saco tiempo para el piso.", "ic_persona",
                "600 555 666", "winwaleo@roommate.es"
        );
    }

    private void insertarCompaneroInicial(
            SQLiteDatabase db,
            String nombre,
            String apellidos,
            int edad,
            String sexo,
            String fechaEntrada,
            String introduccion,
            String fotoPerfil,
            String telefono,
            String correo
    ) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("apellidos", apellidos);
        valores.put("edad", edad);
        valores.put("sexo", sexo);
        valores.put("fecha_entrada", fechaEntrada);
        valores.put("introduccion", introduccion);
        valores.put("foto_perfil", fotoPerfil);
        valores.put("telefono", telefono);
        valores.put("correo", correo);
        db.insert(TABLA_COMPANEROS, null, valores);
    }

    

    public long insertarCompanero(Companero companero) {
        ContentValues valores = valoresDeCompanero(companero);
        return getWritableDatabase().insert(TABLA_COMPANEROS, null, valores);
    }

    public int actualizarCompanero(Companero companero) {
        ContentValues valores = valoresDeCompanero(companero);

        return getWritableDatabase().update(
                TABLA_COMPANEROS,
                valores,
                "id = ?",
                new String[]{String.valueOf(companero.getId())}
        );
    }

    private ContentValues valoresDeCompanero(Companero companero) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", companero.getNombre());
        valores.put("apellidos", companero.getApellidos());
        valores.put("edad", companero.getEdad());
        valores.put("sexo", companero.getSexo());
        valores.put("fecha_entrada", companero.getFechaEntrada());
        valores.put("introduccion", companero.getIntroduccion());
        valores.put("foto_perfil", companero.getFotoPerfil());
        valores.put("telefono", companero.getTelefono());
        valores.put("correo", companero.getCorreo());
        return valores;
    }

    public int actualizarFotoPerfil(int id, String fotoPerfil) {
        ContentValues valores = new ContentValues();
        valores.put("foto_perfil", fotoPerfil);

        return getWritableDatabase().update(
                TABLA_COMPANEROS,
                valores,
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public List<Companero> obtenerCompaneros() {
        List<Companero> companeros = new ArrayList<>();

        Cursor cursor = getReadableDatabase().query(
                TABLA_COMPANEROS,
                null,
                null,
                null,
                null,
                null,
                "nombre COLLATE NOCASE ASC"
        );

        while (cursor.moveToNext()) {
            companeros.add(leerCompanero(cursor));
        }

        cursor.close();
        return companeros;
    }

    public Companero obtenerCompanero(int id) {
        Cursor cursor = getReadableDatabase().query(
                TABLA_COMPANEROS,
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        Companero companero = null;
        if (cursor.moveToFirst()) {
            companero = leerCompanero(cursor);
        }

        cursor.close();
        return companero;
    }

    public int borrarCompanero(int id) {
        return getWritableDatabase().delete(
                TABLA_COMPANEROS,
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public List<String> obtenerNombresCompaneros() {
        List<String> nombres = new ArrayList<>();

        Cursor cursor = getReadableDatabase().query(
                TABLA_COMPANEROS,
                new String[]{"nombre"},
                null,
                null,
                null,
                null,
                "nombre COLLATE NOCASE ASC"
        );

        while (cursor.moveToNext()) {
            nombres.add(cursor.getString(0));
        }

        cursor.close();
        return nombres;
    }

    private Companero leerCompanero(Cursor cursor) {
        return new Companero(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                cursor.getString(cursor.getColumnIndexOrThrow("apellidos")),
                cursor.getInt(cursor.getColumnIndexOrThrow("edad")),
                cursor.getString(cursor.getColumnIndexOrThrow("sexo")),
                cursor.getString(cursor.getColumnIndexOrThrow("fecha_entrada")),
                cursor.getString(cursor.getColumnIndexOrThrow("introduccion")),
                cursor.getString(cursor.getColumnIndexOrThrow("foto_perfil")),
                cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                cursor.getString(cursor.getColumnIndexOrThrow("correo"))
        );
    }

    public Usuario validarUsuario(String nombre, String clave) {
        Cursor cursor = getReadableDatabase().query(
                TABLA_USUARIOS,
                new String[]{"id", "nombre", "clave"},
                "nombre = ? AND clave = ?",
                new String[]{nombre, clave},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            Usuario usuario = new Usuario(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    cursor.getString(cursor.getColumnIndexOrThrow("clave"))
            );
            cursor.close();
            return usuario;
        }

        cursor.close();
        return null;
    }

    

    

    

    

    private ContentValues valoresDeTarea(Tarea tarea) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", tarea.getNombre());
        valores.put("prioridad", tarea.getPrioridad());
        valores.put("dia", tarea.getDia());
        valores.put("dinero", tarea.getDinero());
        valores.put("hora", tarea.getHora());
        valores.put("companero", tarea.getCompanero());
        valores.put("hecha", tarea.isHecha() ? 1 : 0);
        valores.put("dificultad", tarea.getDificultad());
        valores.put("descripcion", tarea.getDescripcion());
        return valores;
    }

    

    

    

    private Tarea leerTarea(Cursor cursor) {
        return new Tarea(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                cursor.getString(cursor.getColumnIndexOrThrow("prioridad")),
                cursor.getString(cursor.getColumnIndexOrThrow("dia")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("dinero")),
                cursor.getString(cursor.getColumnIndexOrThrow("hora")),
                cursor.getString(cursor.getColumnIndexOrThrow("companero")),
                R.drawable.ic_persona,
                cursor.getInt(cursor.getColumnIndexOrThrow("hecha")) == 1,
                cursor.getInt(cursor.getColumnIndexOrThrow("dificultad")),
                cursor.getString(cursor.getColumnIndexOrThrow("descripcion"))
        );
    }

    public List<ItemCompra> obtenerItemsCompra() {
        List<ItemCompra> items = new ArrayList<>();

        Cursor cursor = getReadableDatabase().query(
                TABLA_ITEMS_COMPRA,
                null,
                null,
                null,
                null,
                null,
                "nombre COLLATE NOCASE ASC"
        );

        while (cursor.moveToNext()) {
            items.add(new ItemCompra(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    cursor.getString(cursor.getColumnIndexOrThrow("categoria"))
            ));
        }

        cursor.close();
        return items;
    }

    public List<ItemCompra> obtenerListaCompra(int usuarioId) {
        List<ItemCompra> items = new ArrayList<>();

        String consulta =
                "SELECT i.id, i.nombre, i.categoria " +
                        "FROM " + TABLA_ITEMS_COMPRA + " i " +
                        "INNER JOIN " + TABLA_LISTA_COMPRA + " l " +
                        "ON l.item_id = i.id " +
                        "WHERE l.usuario_id = ? " +
                        "ORDER BY i.nombre COLLATE NOCASE ASC";

        Cursor cursor = getReadableDatabase().rawQuery(
                consulta,
                new String[]{String.valueOf(usuarioId)}
        );

        while (cursor.moveToNext()) {
            items.add(new ItemCompra(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2)
            ));
        }

        cursor.close();
        return items;
    }

    public boolean anadirListaCompra(int usuarioId, int itemId) {
        ContentValues valores = new ContentValues();
        valores.put("usuario_id", usuarioId);
        valores.put("item_id", itemId);

        // Si la relacion ya existe, no se duplica.
        long id = getWritableDatabase().insertWithOnConflict(
                TABLA_LISTA_COMPRA,
                null,
                valores,
                SQLiteDatabase.CONFLICT_IGNORE
        );

        return id != -1;
    }

    public void eliminarListaCompra(int usuarioId, int itemId) {
        getWritableDatabase().delete(
                TABLA_LISTA_COMPRA,
                "usuario_id = ? AND item_id = ?",
                new String[]{
                        String.valueOf(usuarioId),
                        String.valueOf(itemId)
                }
        );
    }
}
