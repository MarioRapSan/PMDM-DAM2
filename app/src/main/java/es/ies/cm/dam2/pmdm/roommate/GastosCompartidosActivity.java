package es.ies.cm.dam2.pmdm.roommate;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * Gastos compartidos (PROXIMAMENTE).
 *
 * Por ahora solo muestra un aviso de que la pantalla estara disponible pronto.
 * Idea de lo que hara cuando se construya:
 *  - Un usuario apunta los productos que ha comprado y cuanto le han costado.
 *  - Elige entre que companeros se reparte el gasto.
 *  - A cada uno de ellos, y a la persona que introdujo el producto, le aparece
 *    la cantidad que le toca poner.
 */
public class GastosCompartidosActivity extends AppCompatActivity {

    /** Muestra la barra superior con la flecha de volver y el aviso "Proximamente". */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gastos_compartidos);

        MaterialToolbar toolbar = findViewById(R.id.toolbarGastos);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.gastos_compartidos);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Volver a la pantalla anterior (inicio), que sigue debajo en la pila.
        Button btnVolver = findViewById(R.id.btnVolverGastos);
        btnVolver.setOnClickListener(v -> finish());
    }

    /** La flecha de la barra superior cierra la pantalla y vuelve a la anterior. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
