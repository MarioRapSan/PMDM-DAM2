package es.ies.cm.dam2.pmdm.roommate;

import android.content.Context;

/**
 * Traduce las prioridades entre el valor que se guarda en la base de datos
 * y el texto que se muestra en pantalla.
 *
 * En la BD siempre se guarda el valor fijo en espanol (array no traducible
 * "prioridades_valores"), y en pantalla se muestra el texto del array
 * "prioridades_tarea", que si se traduce en values-en. Asi los datos no
 * dependen del idioma del movil.
 */
public final class Prioridades {

    /** Clase de utilidades: solo tiene metodos estaticos, no se crean objetos. */
    private Prioridades() {
    }

    /** Valor que se guarda en la BD para la posicion elegida en el Spinner. */
    public static String valorDePosicion(Context context, int posicion) {
        String[] valores = context.getResources().getStringArray(R.array.prioridades_valores);
        return (posicion > 0 && posicion < valores.length) ? valores[posicion] : "";
    }

    /** Posicion del Spinner que corresponde a un valor guardado (0 si no existe). */
    public static int posicionDeValor(Context context, String valor) {
        String[] valores = context.getResources().getStringArray(R.array.prioridades_valores);
        for (int i = 1; i < valores.length; i++) {
            if (valores[i].equals(valor)) {
                return i;
            }
        }
        return 0;
    }

    /** Texto traducido para mostrar un valor guardado en la BD. */
    public static String etiqueta(Context context, String valor) {
        int posicion = posicionDeValor(context, valor);
        if (posicion == 0) {
            // Valor desconocido (por ejemplo, datos antiguos): se muestra tal cual.
            return valor == null ? "" : valor;
        }
        return context.getResources().getStringArray(R.array.prioridades_tarea)[posicion];
    }
}
