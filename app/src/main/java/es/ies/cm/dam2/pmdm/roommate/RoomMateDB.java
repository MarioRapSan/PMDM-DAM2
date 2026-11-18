package es.ies.cm.dam2.pmdm.roommate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Base de datos SQLite de la aplicacion (fichero roommate.db), con SQLiteOpenHelper.
 *
 * Tablas:
 *  - usuarios: login, registro, cambio de clave y baja.
 *  - items_compra: catalogo de productos (se pueden anadir y borrar).
 *  - lista_compra: lista personal de cada usuario (usuario_id + item_id),
 *    con claves ajenas y ON DELETE CASCADE.
 *  - companeros: perfiles de los companeros de piso (CRUD completo). Cada perfil
 *    se liga a una cuenta (usuario_id, con ON DELETE CASCADE) y solo esa cuenta
 *    puede editarlo o borrarlo.
 * La primera vez que se crea, inserta los datos de ejemplo (5 usuarios, productos,
 * 3 companeros y una lista de la compra para admin).
 * Las tareas y sus registros se guardan en Room (AppDatabase).
 */
public class RoomMateDB extends SQLiteOpenHelper {

    public static final String NOMBRE_BD = "roommate.db";
    public static final int VERSION_BD = 3;

    private static final String TABLA_COMPANEROS = "companeros";
    private static final String TABLA_USUARIOS = "usuarios";
    private static final String TABLA_ITEMS_COMPRA = "items_compra";
    private static final String TABLA_LISTA_COMPRA = "lista_compra";

    /** Abre (o crea la primera vez) el fichero roommate.db. */
    public RoomMateDB(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    /** Se ejecuta al abrir la conexion, antes de crear o actualizar las tablas. */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Activa las relaciones entre tablas para que las claves foraneas funcionen.
        db.setForeignKeyConstraintsEnabled(true);
    }

    /** Solo se ejecuta la primera vez: crea las tablas y carga los datos de ejemplo. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        crearTablas(db);
        insertarDatosIniciales(db);
    }

    /** Actualiza las tablas cuando sube VERSION_BD, sin perder los datos guardados. */
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

        // La version 3 liga cada companero con su cuenta de usuario.
        if (oldVersion < 3) {
            if (!tieneColumna(db, TABLA_COMPANEROS, "usuario_id")) {
                db.execSQL("ALTER TABLE " + TABLA_COMPANEROS + " ADD COLUMN usuario_id "
                        + "INTEGER REFERENCES " + TABLA_USUARIOS + "(id) ON DELETE CASCADE");
            }
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_companeros_usuario ON "
                    + TABLA_COMPANEROS + "(usuario_id)");
            insertarUsuariosDeCompaneros(db);
            vincularCompanerosDeEjemplo(db);
        }
    }

    /** Comprueba si una tabla ya tiene una columna (para no anadirla dos veces). */
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

    /** Crea las cuatro tablas de SQLite con sus claves ajenas. */
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
                        "correo TEXT, " +
                        // Cuenta de usuario duena del perfil: si se borra el
                        // usuario, su perfil de companero se borra con el.
                        "usuario_id INTEGER REFERENCES " + TABLA_USUARIOS + "(id) " +
                        "ON DELETE CASCADE)"
        );
        // Un usuario solo puede tener un perfil de companero.
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_companeros_usuario ON "
                + TABLA_COMPANEROS + "(usuario_id)");

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

    /**
     * Carga inicial: usuarios, productos, companeros y una lista de ejemplo, todo en una
     * transaccion.
     */
    private void insertarDatosIniciales(SQLiteDatabase db) {
        db.beginTransaction();

        try {
            insertarUsuario(db, "admin", "admin");
            insertarUsuario(db, "mario", "mario");
            insertarUsuariosDeCompaneros(db);

            insertarItemsCompra(db);
            insertarCompanerosIniciales(db);
            vincularCompanerosDeEjemplo(db);

            // Ejemplo de lista personal: admin ya tiene Leche y Pan en su lista.
            db.execSQL("INSERT INTO " + TABLA_LISTA_COMPRA + " (usuario_id, item_id) "
                    + "SELECT u.id, i.id FROM " + TABLA_USUARIOS + " u, " + TABLA_ITEMS_COMPRA + " i "
                    + "WHERE u.nombre = 'admin' AND i.nombre IN ('Leche', 'Pan')");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Inserta un usuario de la carga inicial (si ya existe, no hace nada). */
    private void insertarUsuario(SQLiteDatabase db, String nombre, String clave) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("clave", clave);
        db.insertWithOnConflict(TABLA_USUARIOS, null, valores, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /** Cuentas de los companeros de ejemplo (la clave es el propio nombre). */
    private void insertarUsuariosDeCompaneros(SQLiteDatabase db) {
        insertarUsuario(db, "jose", "jose");
        insertarUsuario(db, "raul", "raul");
        insertarUsuario(db, "winwaleo", "winwaleo");
    }

    /**
     * Liga cada companero de ejemplo con la cuenta que tiene su mismo nombre
     * (Jose -> jose, Raul -> raul, Winwaleo -> winwaleo).
     */
    private void vincularCompanerosDeEjemplo(SQLiteDatabase db) {
        for (String nombre : new String[]{"jose", "raul", "winwaleo"}) {
            // Se actualiza una sola fila (el primer companero con ese nombre y sin
            // cuenta) y solo si esa cuenta no tiene ya un perfil: un perfil por usuario.
            db.execSQL("UPDATE " + TABLA_COMPANEROS
                            + " SET usuario_id = (SELECT id FROM " + TABLA_USUARIOS + " WHERE nombre = ?)"
                            + " WHERE id = (SELECT MIN(id) FROM " + TABLA_COMPANEROS
                            + " WHERE lower(nombre) = ? AND usuario_id IS NULL)"
                            + " AND NOT EXISTS (SELECT 1 FROM " + TABLA_COMPANEROS
                            + " WHERE usuario_id = (SELECT id FROM " + TABLA_USUARIOS + " WHERE nombre = ?))",
                    new Object[]{nombre, nombre, nombre});
        }
    }

    /** Inserta el catalogo inicial de productos. */
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

    /** Inserta un producto del catalogo inicial. */
    private void insertarItem(SQLiteDatabase db, String nombre, String categoria) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("categoria", categoria);
        db.insert(TABLA_ITEMS_COMPRA, null, valores);
    }

    /** Inserta los tres companeros de ejemplo. */
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

    /** Inserta un companero de la carga inicial. */
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

    /**
     * CREATE: guarda un companero nuevo y devuelve su id (-1 si falla).
     * Si el companero tiene usuario, el perfil queda ligado a esa cuenta.
     */
    public long insertarCompanero(Companero companero) {
        ContentValues valores = valoresDeCompanero(companero);
        if (companero.getUsuarioId() > 0) {
            valores.put("usuario_id", companero.getUsuarioId());
        }
        return getWritableDatabase().insert(TABLA_COMPANEROS, null, valores);
    }

    /** READ: perfil de companero de un usuario, o null si todavia no tiene. */
    public Companero obtenerCompaneroDeUsuario(int usuarioId) {
        Cursor cursor = getReadableDatabase().query(
                TABLA_COMPANEROS, null, "usuario_id = ?",
                new String[]{String.valueOf(usuarioId)}, null, null, null);

        Companero companero = cursor.moveToFirst() ? leerCompanero(cursor) : null;
        cursor.close();
        return companero;
    }

    /** UPDATE: guarda los cambios de un companero. */
    public int actualizarCompanero(Companero companero) {
        ContentValues valores = valoresDeCompanero(companero);

        return getWritableDatabase().update(
                TABLA_COMPANEROS,
                valores,
                "id = ?",
                new String[]{String.valueOf(companero.getId())}
        );
    }

    /** Pasa los datos del companero a ContentValues (columna -> valor). */
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

    /** UPDATE: cambia solo la foto de perfil de un companero. */
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

    /** READ: todos los companeros ordenados por nombre. */
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

    /** READ: un companero por su id, o null si no existe. */
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

    /** DELETE: borra un companero por su id. */
    public int borrarCompanero(int id) {
        return getWritableDatabase().delete(
                TABLA_COMPANEROS,
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    /** Consulta propia: solo los nombres, para rellenar los Spinner. */
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

    /** Convierte la fila actual del cursor en un objeto Companero. */
    private Companero leerCompanero(Cursor cursor) {
        Companero companero = new Companero(
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

        // Cuenta duena del perfil (0 si no esta ligado a ningun usuario).
        int columna = cursor.getColumnIndex("usuario_id");
        if (columna != -1 && !cursor.isNull(columna)) {
            companero.setUsuarioId(cursor.getInt(columna));
        }
        return companero;
    }

    /** Login: devuelve el usuario si el nombre y la clave coinciden, o null si no. */
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

    // ------------------------------------------------------------------
    // CRUD de usuarios (alta, cambio de clave y baja).
    // Estos metodos acceden a disco: se deben llamar fuera del hilo principal
    // (por ejemplo con AppDatabase.ejecutar).
    // ------------------------------------------------------------------

    /**
     * Da de alta un usuario nuevo.
     *
     * @return el id del usuario creado, o -1 si el nombre ya existe.
     */
    public long registrarUsuario(String nombre, String clave) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("clave", clave);

        // La columna nombre es UNIQUE: si ya existe, insert devuelve -1.
        return getWritableDatabase().insertWithOnConflict(
                TABLA_USUARIOS,
                null,
                valores,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    /**
     * Cambia la clave de un usuario.
     *
     * @return numero de filas modificadas (1 si ha ido bien).
     */
    public int cambiarClave(int usuarioId, String nuevaClave) {
        ContentValues valores = new ContentValues();
        valores.put("clave", nuevaClave);

        return getWritableDatabase().update(
                TABLA_USUARIOS,
                valores,
                "id = ?",
                new String[]{String.valueOf(usuarioId)}
        );
    }

    /**
     * Da de baja un usuario. Gracias al ON DELETE CASCADE de lista_compra,
     * su lista de la compra personal se borra automaticamente.
     *
     * @return numero de filas borradas (1 si ha ido bien).
     */
    public int borrarUsuario(int usuarioId) {
        return getWritableDatabase().delete(
                TABLA_USUARIOS,
                "id = ?",
                new String[]{String.valueOf(usuarioId)}
        );
    }

    /** Cuenta los productos que tiene un usuario en su lista personal. */
    public int contarListaCompra(int usuarioId) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLA_LISTA_COMPRA + " WHERE usuario_id = ?",
                new String[]{String.valueOf(usuarioId)}
        );
        int total = cursor.moveToFirst() ? cursor.getInt(0) : 0;
        cursor.close();
        return total;
    }

    /** READ: todo el catalogo de productos ordenado por nombre. */
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

    // ------------------------------------------------------------------
    // Catalogo de productos (items_compra): alta y baja de productos.
    // ------------------------------------------------------------------

    /**
     * Anade un producto nuevo al catalogo.
     *
     * @return el id del producto, o -1 si ya existe uno con ese nombre (UNIQUE).
     */
    public long insertarItemCompra(String nombre, String categoria) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("categoria", categoria);
        return getWritableDatabase().insertWithOnConflict(
                TABLA_ITEMS_COMPRA,
                null,
                valores,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    /**
     * Borra un producto del catalogo. Por el ON DELETE CASCADE de lista_compra,
     * tambien desaparece de las listas personales de todos los usuarios.
     *
     * @return numero de filas borradas (1 si ha ido bien).
     */
    public int borrarItemCompra(int itemId) {
        return getWritableDatabase().delete(
                TABLA_ITEMS_COMPRA,
                "id = ?",
                new String[]{String.valueOf(itemId)}
        );
    }

    /** READ: productos de la lista personal de un usuario (JOIN con items_compra). */
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

    /** CREATE: anade un producto a la lista del usuario. Devuelve false si ya estaba. */
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

    /** DELETE: quita un producto de la lista del usuario. */
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
