package johnarrieta.electiva.ejemploconexionhttp.vistas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import johnarrieta.electiva.ejemploconexionhttp.R;
import johnarrieta.electiva.ejemploconexionhttp.controladores.ConexionHttpPostServer;
import johnarrieta.electiva.ejemploconexionhttp.datos.Mensaje;
import johnarrieta.electiva.ejemploconexionhttp.datos.Usuario;

/**
 * Pantalla 1: INICIAR SESIÓN (pantalla de arranque).
 *  - "Entrar": envía accion=login&email=..&psw=.. al PHP.
 *      Si el servidor devuelve un usuario -> abre PantallaListado.
 *      Si devuelve {"mensaje":"Acceso denegado"} -> muestra un Toast.
 *  - "Registrate": abre PantallaCrudUsuario en modo registro.
 */
public class PantallaInicio extends AppCompatActivity {

    private EditText campoEmail;
    private EditText campoClave;
    private Button btnRegistrate;
    private Button btnIniciarSesion;
    private ProgressBar progreso;

    // Hilo de fondo para la red (reemplaza al AsyncTask de la guía, ya obsoleto)
    private final ExecutorService hiloDeRed = Executors.newSingleThreadExecutor();
    private final ConexionHttpPostServer conexionServidor = new ConexionHttpPostServer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pantalla_inicio);

        campoEmail = findViewById(R.id.campoEmail);
        campoClave = findViewById(R.id.campoClave);
        btnRegistrate = findViewById(R.id.btnRegistrate);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        progreso = findViewById(R.id.progreso);

        btnRegistrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sin extra "sesion" => PantallaCrudUsuario entra en modo REGISTRO
                startActivity(new Intent(PantallaInicio.this, PantallaCrudUsuario.class));
            }
        });

        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verificarDatos()) {
                    iniciarSesion(campoEmail.getText().toString().trim(),
                                  campoClave.getText().toString());
                }
            }
        });
    }

    /** Valida que los campos no estén vacíos antes de molestar al servidor. */
    private boolean verificarDatos() {
        if (campoEmail.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Debe ingresar el Email", Toast.LENGTH_LONG).show();
            return false;
        }
        if (campoClave.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Debe ingresar el Password", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void iniciarSesion(final String email, final String clave) {
        mostrarProgreso(true);

        hiloDeRed.execute(new Runnable() {
            @Override
            public void run() {
                // ---- Esto corre en SEGUNDO PLANO (puede tardar) ----
                Usuario usuarioEncontrado = null;
                String mensajeError = null;
                try {
                    Map<String, String> parametros = new LinkedHashMap<>();
                    parametros.put("accion", "login");
                    parametros.put("email", email);
                    parametros.put("psw", clave);

                    String json = conexionServidor.conexionConElServidor(
                            parametros, ConexionHttpPostServer.direccionDelServidor);

                    if (json == null || json.isEmpty()) {
                        mensajeError = "Acceso Negado, Error en el Servidor";
                    } else {
                        Gson gson = new Gson();
                        JsonElement elemento = JsonParser.parseString(json);
                        JsonObject objeto = elemento.getAsJsonObject();
                        if (objeto.has("email")) {
                            // Login correcto: el JSON es una fila de la tabla
                            usuarioEncontrado = gson.fromJson(json, Usuario.class);
                        } else {
                            // Login incorrecto: {"mensaje":"Acceso denegado"}
                            mensajeError = gson.fromJson(json, Mensaje.class).getMensaje();
                        }
                    }
                } catch (Exception e) {
                    mensajeError = e.getMessage();
                }

                final Usuario usuario = usuarioEncontrado;
                final String error = mensajeError;

                // ---- Volver al hilo de la interfaz para tocar la pantalla ----
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        mostrarProgreso(false);
                        if (usuario != null) {
                            Intent intento = new Intent(PantallaInicio.this, PantallaListado.class);
                            intento.putExtra("sesion", usuario);
                            startActivity(intento);
                        } else {
                            Toast.makeText(PantallaInicio.this, error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private void mostrarProgreso(boolean visible) {
        progreso.setVisibility(visible ? View.VISIBLE : View.GONE);
        btnIniciarSesion.setEnabled(!visible);
        btnRegistrate.setEnabled(!visible);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hiloDeRed.shutdownNow();
    }
}
