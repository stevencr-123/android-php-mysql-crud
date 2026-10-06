package johnarrieta.electiva.ejemploconexionhttp.controladores;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Clase responsable de TODA la comunicación HTTP con el servicio PHP.
 * Hace una petición POST con los parámetros (accion, email, psw, nombre...)
 * y devuelve el texto JSON que responde el servidor.
 *
 * NOTA: la guía usa Apache HttpClient, pero esa librería fue eliminada de
 * Android (API 23+). Aquí se usa HttpURLConnection, que ya viene incluido
 * en Android y hace exactamente lo mismo, sin agregar .jar.
 */
public class ConexionHttpPostServer {

    /**
     * URL del servicio PHP. ¡ES LO ÚNICO QUE DEBES CAMBIAR SEGÚN DÓNDE PRUEBES!
     *  - Emulador de Android Studio:  http://10.0.2.2/CrudPhpJson/crud/operacion.php
     *    (10.0.2.2 es el "localhost" de tu PC visto desde el emulador)
     *  - Celular físico (mismo WiFi): http://IP_DE_TU_PC/CrudPhpJson/crud/operacion.php
     *    (ej: http://192.168.1.31/CrudPhpJson/crud/operacion.php; mira tu IP con ipconfig)
     */
    public static String direccionDelServidor = "http://10.0.2.2/CrudPhpJson/crud/operacion.php";

    private static final int TIEMPO_ESPERA_MS = 10000;

    /**
     * Envía una petición POST y devuelve la respuesta del servidor como String.
     * IMPORTANTE: es una operación de red, NO se puede llamar desde el hilo
     * principal de la interfaz (Android lanza NetworkOnMainThreadException).
     */
    public String conexionConElServidor(Map<String, String> parametros, String rutaDeLaAplicacionWeb)
            throws Exception {

        HttpURLConnection conexion = null;
        try {
            // 1. Armar el cuerpo: accion=login&email=...&psw=...  (codificado para URL)
            StringBuilder cuerpo = new StringBuilder();
            for (Map.Entry<String, String> p : parametros.entrySet()) {
                if (cuerpo.length() > 0) {
                    cuerpo.append('&');
                }
                String valor = p.getValue() == null ? "" : p.getValue();
                cuerpo.append(URLEncoder.encode(p.getKey(), "UTF-8"))
                      .append('=')
                      .append(URLEncoder.encode(valor, "UTF-8"));
            }
            byte[] datos = cuerpo.toString().getBytes(StandardCharsets.UTF_8);

            // 2. Abrir la conexión y configurarla como POST
            URL url = new URL(rutaDeLaAplicacionWeb);
            conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("POST");
            conexion.setConnectTimeout(TIEMPO_ESPERA_MS);
            conexion.setReadTimeout(TIEMPO_ESPERA_MS);
            conexion.setDoOutput(true);
            conexion.setFixedLengthStreamingMode(datos.length);
            conexion.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            // 3. Enviar los parámetros
            try (OutputStream salida = conexion.getOutputStream()) {
                salida.write(datos);
            }

            // 4. Validar el código HTTP (200 = OK)
            int codigo = conexion.getResponseCode();
            if (codigo == HttpURLConnection.HTTP_NOT_IMPLEMENTED) {
                throw new Exception("ERROR 1: Parametros mal codificados");
            } else if (codigo != HttpURLConnection.HTTP_OK) {
                throw new Exception("ERROR 2: URL invalida (HTTP " + codigo + ")");
            }

            // 5. Leer y devolver el JSON
            return procesarRespuestaDelServidor(conexion.getInputStream());

        } catch (IOException e) {
            throw new Exception("ERROR 3: Conexion fallida:\n" + e.getMessage());
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    /** Convierte el flujo de entrada en un String (une las líneas no vacías). */
    private String procesarRespuestaDelServidor(InputStream datosEntrada) throws Exception {
        StringBuilder json = new StringBuilder();
        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(datosEntrada, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    json.append(linea);
                }
            }
        } catch (IOException error) {
            throw new Exception("ERROR 5: Sin respuesta\n" + error.getMessage());
        }
        return json.toString();
    }
}
