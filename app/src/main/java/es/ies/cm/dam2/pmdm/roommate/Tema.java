package es.ies.cm.dam2.pmdm.roommate;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Gestiona el tema de la app (claro u oscuro).
 *
 * El modo elegido se guarda en SharedPreferences para que se mantenga al
 * cerrar y volver a abrir la app. Para cambiarlo se usa
 * AppCompatDelegate.setDefaultNightMode, que recrea las pantallas abiertas
 * con la paleta de values-night/colors.xml.
 */
public final class Tema {

    private static final String PREFS = "roommate_prefs";
    private static final String CLAVE_MODO_OSCURO = "modo_oscuro";

    /** Clase de utilidades: solo tiene metodos estaticos, no se crean objetos. */
    private Tema() {
    }

    /** Devuelve true si el usuario ha elegido el modo oscuro. */
    public static boolean esOscuro(Context context) {
        return prefs(context).getBoolean(CLAVE_MODO_OSCURO, false);
    }

    /** Aplica el tema guardado. Se llama al arrancar la app (RoomMateApp). */
    public static void aplicarGuardado(Context context) {
        aplicar(esOscuro(context));
    }

    /** Guarda el modo elegido y lo aplica a toda la app. */
    public static void cambiar(Context context, boolean oscuro) {
        prefs(context).edit().putBoolean(CLAVE_MODO_OSCURO, oscuro).apply();
        aplicar(oscuro);
    }

    /** Cambia al modo contrario del actual. */
    public static void alternar(Context context) {
        cambiar(context, !esOscuro(context));
    }

    /** Cambia el modo de toda la app (claro u oscuro). */
    private static void aplicar(boolean oscuro) {
        AppCompatDelegate.setDefaultNightMode(oscuro
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    /** Preferencias de la app donde se guarda el tema. */
    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
