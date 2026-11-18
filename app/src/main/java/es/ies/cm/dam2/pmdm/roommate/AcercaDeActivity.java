package es.ies.cm.dam2.pmdm.roommate;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * Muestra informacion basica sobre la aplicacion.
 */
public class AcercaDeActivity extends AppCompatActivity {

    /** Muestra la barra superior con la flecha de volver y la informacion de la app. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acerca_de);

        MaterialToolbar toolbar = findViewById(R.id.toolbarAcercaDe);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.acerca_de);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
