package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Seccion "Companeros": listado de los companeros de piso guardados en SQLite.
 *
 * Cada usuario tiene un unico perfil de companero, ligado a su cuenta, y solo
 * puede editar o borrar el suyo; los demas perfiles son de solo lectura.
 * Sigue el mismo patron que las tareas:
 *  - RecyclerView con CardView (CompaneroRecyclerAdapter).
 *  - Clic corto: abre la ficha. Clic largo en el perfil propio: Editar / Eliminar.
 *  - Boton "Crear mi perfil": solo aparece si el usuario todavia no tiene perfil.
 * La lista se recarga en onResume, asi al volver del alta, la ficha o la
 * edicion nunca se ven datos obsoletos.
 */
public class CompanerosActivity extends AppCompatActivity {

    private static final String CLAVE_CONFIRMAR_BORRADO = "confirmar_borrado_companero";

    private final List<Companero> companeros = new ArrayList<>();
    private CompaneroRecyclerAdapter adaptador;
    private TextView tvSinCompaneros;
    private Button btnCrearPerfil;

    /** Usuario que ha iniciado sesion (llega desde el menu lateral). */
    private int usuarioId;

    /** Companero pendiente de confirmar su borrado. */
    private Companero companeroABorrar;

    /** Prepara la barra superior, la lista, el boton de alta y el dialogo de borrado. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_companeros);

        MaterialToolbar toolbar = findViewById(R.id.toolbarCompaneros);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.companeros);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        usuarioId = getIntent().getIntExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, -1);
        if (usuarioId == -1) {
            AjustesActivity.irAlLogin(this);
            finish();
            return;
        }

        tvSinCompaneros = findViewById(R.id.tvSinCompaneros);
        RecyclerView rvCompaneros = findViewById(R.id.rvCompaneros);
        rvCompaneros.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new CompaneroRecyclerAdapter(
                companeros, usuarioId, this::abrirFicha, this::mostrarMenuContextual);
        rvCompaneros.setAdapter(adaptador);

        // Crear el perfil propio (el boton se oculta si ya existe).
        btnCrearPerfil = findViewById(R.id.btnAnadirCompanero);
        btnCrearPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(this, AltaCompaneroActivity.class);
            intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, usuarioId);
            startActivity(intent);
        });

        // Respuesta del DialogFragment de confirmacion de borrado.
        getSupportFragmentManager().setFragmentResultListener(
                CLAVE_CONFIRMAR_BORRADO, this, (clave, resultado) -> {
                    if (resultado.getBoolean(ConfirmacionDialogFragment.RESULTADO_CONFIRMADO)
                            && companeroABorrar != null) {
                        borrarCompanero(companeroABorrar);
                    }
                    companeroABorrar = null;
                });
    }

    /** Recarga la lista cada vez que se vuelve a esta pantalla. */
    @Override
    protected void onResume() {
        super.onResume();
        cargarCompaneros();
    }

    /** La flecha de la barra superior vuelve a la pantalla principal. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Lee los companeros de SQLite en un hilo secundario y los muestra. */
    private void cargarCompaneros() {
        AppDatabase.ejecutar(() -> {
            RoomMateDB db = new RoomMateDB(this);
            List<Companero> lista = db.obtenerCompaneros();
            db.close();

            runOnUiThread(() -> {
                companeros.clear();
                companeros.addAll(lista);
                adaptador.notifyDataSetChanged();
                tvSinCompaneros.setVisibility(companeros.isEmpty() ? View.VISIBLE : View.GONE);

                // "Crear mi perfil" solo tiene sentido si el usuario aun no tiene perfil.
                boolean tienePerfil = false;
                for (Companero c : companeros) {
                    if (c.esDe(usuarioId)) {
                        tienePerfil = true;
                        break;
                    }
                }
                btnCrearPerfil.setVisibility(tienePerfil ? View.GONE : View.VISIBLE);
            });
        });
    }

    /** Abre la ficha del companero pulsado (clic corto en la tarjeta). */
    private void abrirFicha(Companero companero) {
        Intent intent = new Intent(this, FichaCompaneroActivity.class);
        intent.putExtra(FichaCompaneroActivity.EXTRA_COMPANERO_ID, companero.getId());
        intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, usuarioId);
        startActivity(intent);
    }

    /** Menu contextual (clic largo): Editar / Eliminar, solo en el perfil propio. */
    private void mostrarMenuContextual(View vista, Companero companero) {
        if (!companero.esDe(usuarioId)) {
            Toast.makeText(this, R.string.solo_tu_perfil, Toast.LENGTH_SHORT).show();
            return;
        }

        PopupMenu menu = new PopupMenu(this, vista);
        menu.getMenuInflater().inflate(R.menu.menu_companero_contextual, menu.getMenu());

        menu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_editar_companero) {
                Intent intent = new Intent(this, EditarCompaneroActivity.class);
                intent.putExtra(EditarCompaneroActivity.EXTRA_COMPANERO_ID, companero.getId());
                intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, usuarioId);
                startActivity(intent);
                return true;
            }
            if (id == R.id.action_eliminar_companero) {
                companeroABorrar = companero;
                ConfirmacionDialogFragment.nuevo(
                        CLAVE_CONFIRMAR_BORRADO,
                        R.string.eliminar_companero,
                        R.string.mensaje_confirmar_eliminar_companero,
                        R.string.eliminar
                ).show(getSupportFragmentManager(), "dialogo_borrar_companero");
                return true;
            }
            return false;
        });
        menu.show();
    }

    /** Borra el companero (ya confirmado) y su foto, y recarga la lista. */
    private void borrarCompanero(Companero companero) {
        AppDatabase.ejecutar(() -> {
            RoomMateDB db = new RoomMateDB(this);
            int borrados = db.borrarCompanero(companero.getId());
            db.close();

            runOnUiThread(() -> {
                if (borrados > 0) {
                    FotoPerfilUtils.borrarFotoPerfil(companero.getFotoPerfil());
                    Toast.makeText(this,
                            getString(R.string.companero_eliminado, companero.getNombreCompleto()),
                            Toast.LENGTH_SHORT).show();
                    cargarCompaneros();
                } else {
                    Toast.makeText(this, R.string.error_borrar_companero, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
