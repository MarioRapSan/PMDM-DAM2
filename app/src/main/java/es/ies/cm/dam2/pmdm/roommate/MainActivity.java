package es.ies.cm.dam2.pmdm.roommate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

/**
 * Pantalla de alta de companeros.
 * Tambien sirve como punto de entrada a las demas pantallas.
 */
public class MainActivity extends AppCompatActivity {

    private EditText etNombre;
    private EditText etApellidos;
    private EditText etEdad;
    private EditText etFechaEntrada;
    private EditText etTelefono;
    private EditText etCorreo;
    private EditText etIntroduccion;
    private RadioGroup rgSexo;
    private ImageView ivFotoCompanero;
    private String fotoPerfilSeleccionada = "ic_persona";
    private ActivityResultLauncher<String> selectorFoto;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private int usuarioId = 1;
    private String usuarioNombre = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ivFotoCompanero = findViewById(R.id.ivFotoCompanero);
        Button btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);

        selectorFoto = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::seleccionarFoto
        );

        btnSeleccionarFoto.setOnClickListener(
                v -> selectorFoto.launch("image/*")
        );

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        MaterialToolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.abrir_menu,
                R.string.cerrar_menu
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(
                this::onNavigationItemSelected
        );

        // Usuario que ha iniciado sesion. Si no llega ninguno, se usa admin.
        usuarioId = getIntent().getIntExtra(
                ColeccionTareasActivity.EXTRA_USUARIO_ID,
                1
        );
        usuarioNombre = getIntent().getStringExtra(
                ColeccionTareasActivity.EXTRA_USUARIO_NOMBRE
        );
        if (usuarioNombre == null || usuarioNombre.isEmpty()) {
            usuarioNombre = "admin";
        }

        etNombre = findViewById(R.id.etNombre);
        etApellidos = findViewById(R.id.etApellidos);
        etEdad = findViewById(R.id.etEdad);
        etFechaEntrada = findViewById(R.id.etFechaEntrada);
        etTelefono = findViewById(R.id.etTelefono);
        etCorreo = findViewById(R.id.etCorreo);
        etIntroduccion = findViewById(R.id.etIntroduccion);
        rgSexo = findViewById(R.id.rgSexo);

        Button btnCheck = findViewById(R.id.btnCheck);
        Button btnSiguiente = findViewById(R.id.btnSiguiente);
        Button btnMasAcciones = findViewById(R.id.btnMasAcciones);

        // Guarda el companero en la base de datos.
        btnCheck.setOnClickListener(v -> enviarFormulario());

        // Abre la ficha de los companeros guardados.
        btnSiguiente.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FichaCompaneroActivity.class);
            startActivity(intent);
        });

        // Agrupa las acciones secundarias en un desplegable.
        btnMasAcciones.setOnClickListener(this::mostrarMenuAcciones);
    }

    private void mostrarMenuAcciones(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(
                R.menu.menu_main_acciones,
                popup.getMenu()
        );
        popup.setOnMenuItemClickListener(this::onAccionSeleccionada);
        popup.show();
    }

    private boolean onAccionSeleccionada(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_coleccion) {
            abrirColeccion();
            return true;
        }

        if (id == R.id.action_buscar) {
            abrirBuscar();
            return true;
        }

        if (id == R.id.action_compra) {
            abrirCompra();
            return true;
        }

        if (id == R.id.action_lista_tareas) {
            startActivity(new Intent(this, ListaTareas.class));
            return true;
        }

        if (id == R.id.action_invitacion) {
            startActivity(new Intent(this, InvitacionActivity.class));
            return true;
        }

        if (id == R.id.action_correo) {
            startActivity(new Intent(this, CorreoActivity.class));
            return true;
        }

        return false;
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_inicio) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_tareas) {
            abrirColeccion();
        } else if (id == R.id.nav_buscar) {
            abrirBuscar();
        } else if (id == R.id.nav_anadir_tarea) {
            startActivity(new Intent(this, AnadirTareasActivity.class));
        } else if (id == R.id.nav_editar_perfil) {
            // La edicion se hace siempre desde la ficha del companero seleccionado.
            startActivity(new Intent(this, FichaCompaneroActivity.class));
        } else if (id == R.id.nav_ajustes) {
            startActivity(new Intent(this, AjustesActivity.class));
        } else if (id == R.id.nav_acerca_de) {
            startActivity(new Intent(this, AcercaDeActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void abrirColeccion() {
        Intent intent = new Intent(MainActivity.this, ColeccionTareasActivity.class);
        intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, usuarioId);
        intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_NOMBRE, usuarioNombre);
        startActivity(intent);
    }

    private void abrirBuscar() {
        Intent intent = new Intent(MainActivity.this, BuscarActivity.class);
        intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, usuarioId);
        intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_NOMBRE, usuarioNombre);
        startActivity(intent);
    }

    private void abrirCompra() {
        Intent intent = new Intent(MainActivity.this, ListaCompraActivity.class);
        intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_ID, usuarioId);
        intent.putExtra(ColeccionTareasActivity.EXTRA_USUARIO_NOMBRE, usuarioNombre);
        startActivity(intent);
    }

    private void seleccionarFoto(Uri uri) {
        if (uri == null) {
            return;
        }

        String ruta = FotoPerfilUtils.guardarFotoPerfil(this, uri);
        if (ruta == null) {
            Toast.makeText(
                    this,
                    R.string.error_foto_perfil,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        fotoPerfilSeleccionada = ruta;
        FotoPerfilUtils.mostrarFotoPerfil(ivFotoCompanero, ruta);
    }

    private void enviarFormulario() {
        String nombre = etNombre.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String edadTexto = etEdad.getText().toString().trim();
        String fechaEntrada = etFechaEntrada.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String introduccion = etIntroduccion.getText().toString().trim();

        int sexoId = rgSexo.getCheckedRadioButtonId();

        // El nombre, los apellidos, la edad y el sexo son obligatorios.
        if (nombre.isEmpty()
                || apellidos.isEmpty()
                || edadTexto.isEmpty()
                || sexoId == -1) {
            mostrarError();
            return;
        }

        int edad;
        try {
            edad = Integer.parseInt(edadTexto);
        } catch (NumberFormatException e) {
            mostrarError();
            return;
        }

        if (edad < 18 || edad > 100) {
            mostrarError();
            return;
        }

        if (!correo.isEmpty()
                && !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(
                    this,
                    R.string.error_correo,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        RadioButton radioSexo = findViewById(sexoId);
        String sexo = radioSexo.getText().toString();

        Companero nuevo = new Companero(
                nombre,
                apellidos,
                edad,
                sexo,
                fechaEntrada,
                introduccion,
                fotoPerfilSeleccionada,
                telefono,
                correo
        );

        RoomMateDB db = new RoomMateDB(this);
        long id = db.insertarCompanero(nuevo);
        db.close();

        if (id == -1) {
            Toast.makeText(
                    this,
                    R.string.error_guardar_companero,
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        etNombre.setText("");
        etApellidos.setText("");
        etEdad.setText("");
        etFechaEntrada.setText("");
        etTelefono.setText("");
        etCorreo.setText("");
        etIntroduccion.setText("");
        rgSexo.clearCheck();

        fotoPerfilSeleccionada = "ic_persona";
        FotoPerfilUtils.mostrarFotoPerfil(ivFotoCompanero, null);

        Toast.makeText(
                this,
                R.string.companero_registrado,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void mostrarError() {
        Toast.makeText(
                this,
                R.string.datos_incorrectos,
                Toast.LENGTH_SHORT
        ).show();
    }
}
