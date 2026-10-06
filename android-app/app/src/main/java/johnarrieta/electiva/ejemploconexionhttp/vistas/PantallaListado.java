package johnarrieta.electiva.ejemploconexionhttp.vistas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import johnarrieta.electiva.ejemploconexionhttp.R;
import johnarrieta.electiva.ejemploconexionhttp.controladores.ConexionHttpPostServer;
import johnarrieta.electiva.ejemploconexionhttp.datos.Mensaje;
import johnarrieta.electiva.ejemploconexionhttp.datos.Usuario;

/**
 * Pantalla 3: LISTADO de usuarios (solo se llega después de un login correcto).
 * Al abrirse pide accion=listar al PHP, convierte el JSON en List<Usuario>
 * y lo muestra en un ListView. El botón "Edita Tus Datos" abre
 * PantallaCrudUsuario en modo edición con el usuario que inició sesión.
 */
public class PantallaListado extends AppCompatActivity {

    private ListView listaUsuariosView;
    private Button btnEditar;
    private ProgressBar progreso;
    private ArrayAdapter<String> items;

    private Usuario sesion; // el usuario que inició sesión

    private final ExecutorService hiloDeRed = Executors.newSingleThreadExecutor();
    private final ConexionHttpPostServer conexionServidor = new ConexionHttpPostServer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pantalla_listado);

        listaUsuariosView = findViewById(R.id.listaUsuarios);
        btnEditar = findViewById(R.id.btnGuardar);
        progreso = findViewById(R.id.progreso);

        sesion = (Usuario) getIntent().getSerializableExtra("sesion");

        // El adaptador es el "puente" entre los datos (Strings) y el ListView
        items = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listaUsuariosView.setAdapter(items);

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intento = new Intent(PantallaListado.this, PantallaCrudUsuario.class);
                intento.putExtra("sesion", sesion);
                startActivity(intento);
            }
        });

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        progreso.setVisibility(View.VISIBLE);

        hiloDeRed.execute(new Runnable() {
            @Override
            public void run() {
                // ---- Segundo plano ----
                List<Usuario> usuarios = null;
                String mensajeError = null;
                try {
                    Map<String, String> parametros = new LinkedHashMap<>();
                    parametros.put("accion", "listar");

                    String json = conexionServidor.conexionConElServidor(
                            parametros, ConexionHttpPostServer.direccionDelServidor);

                    if (json == null || json.isEmpty()) {
                        mensajeError = "Respuesta vacia del servidor";
                    } else {
                        Gson gson = new Gson();
                        JsonElement elemento = JsonParser.parseString(json);
                        if (elemento.isJsonArray()) {
                            // Caso normal: [ {email,password,nombre}, {...}, ... ]
                            Type tipoLista = new TypeToken<List<Usuario>>() { }.getType();
                            usuarios = gson.fromJson(json, tipoLista);
                        } else {
                            // Caso {"mensaje":"No hay Usuarios"}
                            mensajeError = gson.fromJson(json, Mensaje.class).getMensaje();
                        }
                    }
                } catch (Exception e) {
                    mensajeError = e.getMessage();
                }

                final List<Usuario> lista = usuarios;
                final String error = mensajeError;

                // ---- Hilo de la interfaz ----
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        progreso.setVisibility(View.GONE);
                        if (lista != null) {
                            mostrarUsuariosEnLista(lista);
                        } else {
                            Toast.makeText(PantallaListado.this, error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private void mostrarUsuariosEnLista(List<Usuario> lista) {
        items.clear();
        for (Usuario alguien : lista) {
            items.add(alguien.getEmail() + " - " + alguien.getNombre());
        }
        items.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hiloDeRed.shutdownNow();
    }
}
