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
 * Guarda companeros, usuarios, tareas y la lista de la compra.
 */
public class RoomMateDB extends SQLiteOpenHelper {

    public static final String NOMBRE_BD = "roommate.db";
    public static final int VERSION_BD = 1;

    private static final String TABLA_COMPANEROS = "companeros";
    private static final String TABLA_USUARIOS = "usuarios";
    private static final String TABLA_ITEMS_COMPRA = "items_compra";
    private static final String TABLA_LISTA_COMPRA = "lista_compra";
    private static final String TABLA_TAREAS = "tareas";

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
        // La base de datos nace en la version 1.
        // No hay versiones anteriores que actualizar por ahora.
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
                        "foto_perfil TEXT)"
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

        db.execSQL(
                "CREATE TABLE " + TABLA_TAREAS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nombre TEXT NOT NULL, " +
                        "prioridad TEXT, " +
                        "dia TEXT, " +
                        "dinero REAL DEFAULT 0, " +
                        "hora TEXT, " +
                        "companero TEXT, " +
                        "hecha INTEGER DEFAULT 0, " +
                        "dificultad INTEGER DEFAULT 3, " +
                        "descripcion TEXT)"
        );
    }

    private void insertarDatosIniciales(SQLiteDatabase db) {
        db.beginTransaction();

        try {
            insertarUsuario(db, "admin", "admin");
            insertarUsuario(db, "mario", "mario");

            insertarItemsCompra(db);
            insertarCompanerosIniciales(db);
            insertarTareasIniciales(db);

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
                "01/09/2024", "Me gusta cocinar y ver series.", "ic_persona"
        );
        insertarCompaneroInicial(
                db, "Raul", "López", 27, "Hombre",
                "01/09/2023", "Soy ordenado y suelo ayudar con la limpieza.", "ic_persona"
        );
        insertarCompaneroInicial(
                db, "Winwaleo", "Martínez", 22, "Mujer",
                "15/01/2025", "Estudio y trabajo, pero siempre saco tiempo para el piso.", "ic_persona"
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
            String fotoPerfil
    ) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("apellidos", apellidos);
        valores.put("edad", edad);
        valores.put("sexo", sexo);
        valores.put("fecha_entrada", fechaEntrada);
        valores.put("introduccion", introduccion);
        valores.put("foto_perfil", fotoPerfil);
        db.insert(TABLA_COMPANEROS, null, valores);
    }

    private void insertarTareasIniciales(SQLiteDatabase db) {
        insertarTareaInicial(db, "Limpiar baño", "Puede esperar",
                "Lunes", 0, "10:00", "Jose", true, 2,
                "Limpiar el baño con lejía y dejar los útiles recogidos.");
        insertarTareaInicial(db, "Ordenar el armario", "Puede esperar",
                "Jueves", 0, "17:00", "Jose", true, 2,
                "Ordenar la ropa y separar lo que ya no se usa.");
        insertarTareaInicial(db, "Comprar productos de limpieza", "Hacer cuanto antes",
                "Sábado", 12.50, "12:00", "Winwaleo", true, 4,
                "Comprar lejía, jabón y bayetas.");
        insertarTareaInicial(db, "Cambiar las sábanas", "Hacer cuanto antes",
                "Lunes", 0, "19:00", "Raul", true, 4,
                "Cambiar las sábanas de todas las camas.");
        insertarTareaInicial(db, "Comprar productos de cocina", "Urgente",
                "Miércoles", 23.40, "13:00", "Jose", true, 5,
                "Comprar aceite, arroz y conservas.");
        insertarTareaInicial(db, "Pagar la luz", "Urgente",
                "Viernes", 48.75, "09:00", "Winwaleo", true, 5,
                "Pagar el recibo de la luz antes de la fecha límite.");

        insertarTareaInicial(db, "Limpiar salón", "Puede esperar",
                "Lunes", 0, "08:00", "Raul", false, 2,
                "Pasar la aspiradora y quitar el polvo del salón.");
        insertarTareaInicial(db, "Regar las plantas", "Puede esperar",
                "Martes", 0, "09:00", "Winwaleo", false, 2,
                "Regar las plantas de la terraza y del salón.");
        insertarTareaInicial(db, "Limpiar cocina", "Hacer cuanto antes",
                "Miércoles", 0, "10:00", "Raul", false, 4,
                "Limpiar la cocina, la nevera y los fogones.");
        insertarTareaInicial(db, "Lavar la ropa", "Hacer cuanto antes",
                "Jueves", 0, "11:00", "Jose", false, 4,
                "Lavar la ropa acumulada y tenderla.");
        insertarTareaInicial(db, "Sacar la basura", "Urgente",
                "Viernes", 0, "12:00", "Winwaleo", false, 5,
                "Sacar la basura y reciclar el plástico.");
        insertarTareaInicial(db, "Arreglar la persiana", "Urgente",
                "Sábado", 0, "13:00", "Raul", false, 5,
                "Arreglar la persiana que no sube bien.");
        insertarTareaInicial(db, "Barrer el pasillo", "Puede esperar",
                "Domingo", 0, "14:00", "Jose", false, 2,
                "Barrer el pasillo y limpiar los rodapiés.");
        insertarTareaInicial(db, "Fregar los platos", "Puede esperar",
                "Lunes", 0, "15:00", "Raul", false, 2,
                "Fregar los platos y guardarlos.");
        insertarTareaInicial(db, "Cambiar la bombilla", "Urgente",
                "Martes", 0, "16:00", "Winwaleo", false, 5,
                "Cambiar la bombilla fundida del pasillo.");
        insertarTareaInicial(db, "Organizar la despensa", "Hacer cuanto antes",
                "Miércoles", 0, "17:00", "Jose", false, 4,
                "Organizar la despensa y revisar las fechas.");
        insertarTareaInicial(db, "Tender la ropa", "Puede esperar",
                "Jueves", 0, "18:00", "Raul", false, 2,
                "Tender la ropa y doblarla cuando esté seca.");
        insertarTareaInicial(db, "Arreglar la silla", "Urgente",
                "Viernes", 0, "19:00", "Winwaleo", false, 5,
                "Arreglar la silla que se mueve.");
        insertarTareaInicial(db, "Limpiar las ventanas", "Puede esperar",
                "Sábado", 0, "08:30", "Jose", false, 2,
                "Limpiar las ventanas por dentro y por fuera.");
        insertarTareaInicial(db, "Hacer la cama", "Puede esperar",
                "Domingo", 0, "09:30", "Raul", false, 2,
                "Hacer la cama y ordenar la habitación.");
        insertarTareaInicial(db, "Ordenar el escritorio", "Hacer cuanto antes",
                "Lunes", 0, "10:30", "Winwaleo", false, 4,
                "Ordenar el escritorio y tirar los papeles viejos.");
        insertarTareaInicial(db, "Limpiar el frigorífico", "Hacer cuanto antes",
                "Martes", 0, "11:30", "Jose", false, 4,
                "Limpiar el frigorífico y revisar la comida.");
        insertarTareaInicial(db, "Reponer el papel higiénico", "Urgente",
                "Miércoles", 0, "12:30", "Raul", false, 5,
                "Reponer el papel higiénico en los baños.");
        insertarTareaInicial(db, "Barrer la terraza", "Puede esperar",
                "Jueves", 0, "13:30", "Winwaleo", false, 2,
                "Barrer la terraza y recoger las hojas.");
        insertarTareaInicial(db, "Limpiar los zapatos", "Puede esperar",
                "Viernes", 0, "14:30", "Jose", false, 2,
                "Limpiar los zapatos y guardarlos.");
        insertarTareaInicial(db, "Preparar la mochila", "Hacer cuanto antes",
                "Sábado", 0, "15:30", "Raul", false, 4,
                "Preparar la mochila para el día siguiente.");
    }

    private void insertarTareaInicial(
            SQLiteDatabase db,
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
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("prioridad", prioridad);
        valores.put("dia", dia);
        valores.put("dinero", dinero);
        valores.put("hora", hora);
        valores.put("companero", companero);
        valores.put("hecha", hecha ? 1 : 0);
        valores.put("dificultad", dificultad);
        valores.put("descripcion", descripcion);
        db.insert(TABLA_TAREAS, null, valores);
    }

    public long insertarCompanero(Companero companero) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", companero.getNombre());
        valores.put("apellidos", companero.getApellidos());
        valores.put("edad", companero.getEdad());
        valores.put("sexo", companero.getSexo());
        valores.put("fecha_entrada", companero.getFechaEntrada());
        valores.put("introduccion", companero.getIntroduccion());
        valores.put("foto_perfil", companero.getFotoPerfil());

        return getWritableDatabase().insert(TABLA_COMPANEROS, null, valores);
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
                cursor.getString(cursor.getColumnIndexOrThrow("foto_perfil"))
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

    public long insertarTarea(Tarea tarea) {
        ContentValues valores = valoresDeTarea(tarea);
        return getWritableDatabase().insert(TABLA_TAREAS, null, valores);
    }

    public int actualizarTarea(Tarea tarea) {
        ContentValues valores = valoresDeTarea(tarea);
        return getWritableDatabase().update(
                TABLA_TAREAS,
                valores,
                "id = ?",
                new String[]{String.valueOf(tarea.getId())}
        );
    }

    public int actualizarHecha(int id, boolean hecha) {
        ContentValues valores = new ContentValues();
        valores.put("hecha", hecha ? 1 : 0);

        return getWritableDatabase().update(
                TABLA_TAREAS,
                valores,
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public int borrarTarea(int id) {
        return getWritableDatabase().delete(
                TABLA_TAREAS,
                "id = ?",
                new String[]{String.valueOf(id)}
        );
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

    public List<Tarea> obtenerTareas() {
        List<Tarea> tareas = new ArrayList<>();

        Cursor cursor = getReadableDatabase().query(
                TABLA_TAREAS,
                null,
                null,
                null,
                null,
                null,
                "id ASC"
        );

        while (cursor.moveToNext()) {
            tareas.add(leerTarea(cursor));
        }

        cursor.close();
        return tareas;
    }

    public Tarea obtenerTarea(int id) {
        Cursor cursor = getReadableDatabase().query(
                TABLA_TAREAS,
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        Tarea tarea = null;
        if (cursor.moveToFirst()) {
            tarea = leerTarea(cursor);
        }

        cursor.close();
        return tarea;
    }

    public List<Tarea> buscarTareas(String texto, String modo, String prioridad) {
        StringBuilder seleccion = new StringBuilder("1 = 1");
        List<String> argumentos = new ArrayList<>();

        if (texto != null && !texto.trim().isEmpty()) {
            if (modo != null && modo.startsWith("Empieza")) {
                seleccion.append(" AND nombre LIKE ?");
                argumentos.add(texto + "%");
            } else {
                seleccion.append(" AND nombre LIKE ?");
                argumentos.add("%" + texto + "%");
            }
        }

        if (prioridad != null
                && !prioridad.isEmpty()
                && !prioridad.equals("Todas")) {
            seleccion.append(" AND prioridad = ?");
            argumentos.add(prioridad);
        }

        List<Tarea> tareas = new ArrayList<>();

        Cursor cursor = getReadableDatabase().query(
                TABLA_TAREAS,
                null,
                seleccion.toString(),
                argumentos.toArray(new String[0]),
                null,
                null,
                "nombre COLLATE NOCASE ASC"
        );

        while (cursor.moveToNext()) {
            tareas.add(leerTarea(cursor));
        }

        cursor.close();
        return tareas;
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
