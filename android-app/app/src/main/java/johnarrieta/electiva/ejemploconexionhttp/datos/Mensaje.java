package johnarrieta.electiva.ejemploconexionhttp.datos;

/**
 * Representa las respuestas simples del servidor: {"mensaje":"OK"},
 * {"mensaje":"Acceso denegado"}, {"mensaje":"No hay Usuarios"}, etc.
 */
public class Mensaje {
    private String mensaje;

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
