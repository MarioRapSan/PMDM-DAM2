package es.ies.cm.dam2.pmdm.roommate.db;

public class RoomMateContract {

    private RoomMateContract() {
    }

    public static final String TABLE_NAME_USER = "usuarios";
    public static final String COLUMN_USER = "nombreUsuario";
    public static final String COLUMN_PASSWORD = "password";
    public static final String TABLE_NAME_LISTA_PRODUCTOS = "listaCompra";
    public static final String COLUMN_ID_PRODUCTO = "idProducto";
    public static final String COLUMN_NOMBRE_PRODUCTO = "nombreProducto";
    public static final String COLUMN_CATEGORIA_PRODUCTO = "categoriaProducto";
    public static final String COLUMN_PRECIO_PRODUCTO = "precioProducto";
    public static final String TABLE_NAME_COMPANERO = "companeros";
    public static final String COLUMN_NOMBRE_COMPANERO = "nombreCompanero";
    public static final String COLUMN_EDAD_COMPANERO = "edadCompanero";
    public static final String COLUMN_SEXO_COMPANERO = "sexoCompanero";
    public static final String COLUMN_DESCRIPCION_COMPANERO = "descripcionCompanero";
    public static final String TABLE_NAME_LISTA_TAREAS = "listaTareas";
    public static final String COLUMN_ID_TAREA = "idTarea" ;
    public static final String COLUMN_NOMBRE_TAREA = "nombreTarea";
    public static final String COLUMN_PRIORIDAD_TAREA = "prioridadTarea";
    public static final String COLUMN_DIA_TAREA = "diaTarea";
    public static final String COLUMN_DINERO_TAREA = "dineroTarea";
    public static final String COLUMN_HORA_TAREA = "horaTarea";
    public static final String COLUMN_FECHA_TAREA = "fechaTarea";
    public static final String COLUMN_COMPANERO_TAREA = "companeroTarea";
    public static final String COLUMN_HECHA_TAREA = "hechaTarea";
    public static final String COLUMN_DIFICULTAD_TAREA = "dificultadTarea";
    public static final String COLUMN_DESCRIPCION_TAREA = "descripcionTarea";
}
