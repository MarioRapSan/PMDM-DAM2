
package es.ies.cm.dam2.pmdm.roommate;

import java.util.ArrayList;
import java.util.List;

public class CompaneroRepository {

    private static final List<Companero> companeros =
            new ArrayList<>();

    public static List<Companero> getCompaneros() {
        return companeros;
    }

    public static void agregar(Companero companero) {
        companeros.add(companero);
    }

    public static void borrar(int posicion) {
        if (posicion >= 0 && posicion < companeros.size()) {
            companeros.remove(posicion);
        }
    }
}
