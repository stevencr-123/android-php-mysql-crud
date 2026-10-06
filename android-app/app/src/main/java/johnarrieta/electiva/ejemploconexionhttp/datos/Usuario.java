package johnarrieta.electiva.ejemploconexionhttp.datos;

import java.io.Serializable;

/**
 * Entidad de datos: representa una fila de la tabla Usuarios.
 * Los nombres de los atributos (email, password, nombre) deben ser IGUALES
 * a las columnas que devuelve el PHP, porque GSON los une por nombre.
 * Serializable permite enviar el objeto entre pantallas con Intent.putExtra.
 */
public class Usuario implements Serializable {

    private String email;
    private String password;
    private String nombre;

    public Usuario() {
    }

    public Usuario(String email, String password, String nombre) {
        this.email = email;
        this.password = password;
        this.nombre = nombre;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() {
        return "Usuario{email='" + email + "', password='" + password + "', nombre='" + nombre + "'}";
    }
}
