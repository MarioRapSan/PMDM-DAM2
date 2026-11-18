package es.ies.cm.dam2.pmdm.roommate;

import android.app.Application;

/**
 * Clase Application de RoomMate: se ejecuta una vez al arrancar la app,
 * antes que cualquier pantalla. Aqui se aplica el tema (claro u oscuro)
 * que el usuario dejo guardado, para que el login ya salga con ese tema.
 */
public class RoomMateApp extends Application {

    /** Aplica el tema guardado antes de que se abra la primera pantalla. */
    @Override
    public void onCreate() {
        super.onCreate();
        Tema.aplicarGuardado(this);
    }
}
