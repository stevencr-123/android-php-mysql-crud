package johnarrieta.electiva.ejemploconexionhttp.vistas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import johnarrieta.electiva.ejemploconexionhttp.R;
import johnarrieta.electiva.ejemploconexionhttp.controladores.ConexionHttpPostServer;
import johnarrieta.electiva.ejemploconexionhttp.datos.Mensaje;
import johnarrieta.electiva.ejemploconexionhttp.datos.Usuario;

public class PantallaCrudUsuario extends AppCompatActivity {

    private EditText campoEmail;
    private EditText campoPassword;
    private EditText campoNombre;
    private Button botonGuardar;
    private Button botonCancelar;
    private ProgressBar progreso;

    private boolean modoEdicion = false;

    private final ExecutorService hiloDeRed = Executors.newSingleThreadExecutor();
    private final ConexionHttpPostServer conexionServidor = new ConexionHttpPostServer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pantalla_crud_usuario);

        TextView titulo = findViewById(R.id.txtTituloCrud);
        campoEmail = findViewById(R.id.campoCodigo2);
        campoPassword = findViewById(R.id.campoPassword2);
        campoNombre = findViewById(R.id.campoNombre);
        botonGuardar = findViewById(R.id.botonModificar);
        botonCancelar = findViewById(R.id.botonCancelar2);
        progreso = findViewById(R.id.progreso);

        Usuario sesion = (Usuario) getIntent().getSerializableExtra("sesion");
        if (sesion != null) {
            modoEdicion = true;
            titulo.setText("EDITAR MIS DATOS");
            botonGuardar.setText("Modificar");
            campoEmail.setText(sesion.getEmail());
            campoEmail.setEnabled(false); // la llave primaria no se edita
            campoPassword.setText(sesion.getPassword());
            campoNombre.setText(sesion.getNombre());
        } else {
            titulo.setText("REGISTRAR USUARIO");
            botonGuardar.setText("Registrar");
        }

        botonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // cierra esta pantalla y vuelve a la anterior
            }
        });

        botonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verificarDatos()) {
                    guardar();
                }
            }
        });
    }

    private boolean verificarDatos() {
        if (campoEmail.getText().toString().trim().isEmpty()
                || campoPassword.getText().toString().trim().isEmpty()
                || campoNombre.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void guardar() {
        final String email = campoEmail.getText().toString().trim();
        final String clave = campoPassword.getText().toString();
        final String nombre = campoNombre.getText().toString().trim();
        final String accion = modoEdicion ? "editar" : "Agregar";

        mostrarProgreso(true);

        hiloDeRed.execute(new Runnable() {
            @Override
            public void run() {
                // ---- Segundo plano ----
                String resultado;
                try {
                    Map<String, String> parametros = new LinkedHashMap<>();
                    parametros.put("accion", accion);
                    parametros.put("email", email);
                    parametros.put("psw", clave);
                    parametros.put("nombre", nombre);

                    String json = conexionServidor.conexionConElServidor(
                            parametros, ConexionHttpPostServer.direccionDelServidor);

                    if (json != null && !json.isEmpty()) {
                        resultado = new Gson().fromJson(json, Mensaje.class).getMensaje();
                    } else {
                        resultado = "ERROR";
                    }
                } catch (Exception e) {
                    resultado = e.getMessage();
                }

                final String respuesta = resultado;

                // ---- Hilo de la interfaz ----
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        mostrarProgreso(false);
                        if ("OK".equalsIgnoreCase(respuesta)) {
                            Toast.makeText(PantallaCrudUsuario.this,
                                    "Usuario Guardado con Exito", Toast.LENGTH_LONG).show();
                            // Volver al login limpiando las pantallas anteriores
                            Intent intento = new Intent(PantallaCrudUsuario.this, PantallaInicio.class);
                            intento.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intento);
                            finish();
                        } else {
                            Toast.makeText(PantallaCrudUsuario.this,
                                    "ERROR: " + respuesta, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private void mostrarProgreso(boolean visible) {
        progreso.setVisibility(visible ? View.VISIBLE : View.GONE);
        botonGuardar.setEnabled(!visible);
        botonCancelar.setEnabled(!visible);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hiloDeRed.shutdownNow();
    }
}
