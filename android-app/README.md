# EjemploConexionHttp (App Android + servicio PHP/MySQL CrudPhpJson)

## Cómo abrirlo
1. Android Studio > File > Open > selecciona esta carpeta (`EjemploConexionHttp`).
2. Espera el "Gradle Sync" (necesita internet la primera vez). Si Android Studio ofrece
   actualizar el Android Gradle Plugin / Gradle, acepta.
3. Si usas el emulador, crea un dispositivo en Device Manager.

## Antes de ejecutar (servidor)
- XAMPP: Apache y MySQL en verde.
- Carpeta `CrudPhpJson` dentro de `htdocs` y la BD `crudphpjson` creada.
- Prueba en el navegador: `http://localhost/CrudPhpJson/crud/operacion.php?accion=listar`

## Configurar la URL (un solo lugar)
Archivo `controladores/ConexionHttpPostServer.java`, variable `direccionDelServidor`:
- Emulador:        `http://10.0.2.2/CrudPhpJson/crud/operacion.php`
- Celular físico:  `http://IP_DE_TU_PC/CrudPhpJson/crud/operacion.php`
  (misma red WiFi; IP con `ipconfig`; permitir Apache en el Firewall de Windows)

## Estructura
- `datos/`          Usuario (entidad) y Mensaje (respuestas {"mensaje": ...})
- `controladores/`  ConexionHttpPostServer (peticiones HTTP POST)
- `vistas/`         PantallaInicio (login), PantallaCrudUsuario (registro/edición), PantallaListado
- `res/layout/`     un XML por pantalla
