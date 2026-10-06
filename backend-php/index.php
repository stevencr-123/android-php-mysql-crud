<?php
/**
 * index.php
 *
 * Pagina de prueba rapida. Al abrir http://localhost/CrudPhpJson/ en el
 * navegador, intenta una consulta de login con credenciales vacias; si
 * la conexion a la BD funciona pero no hay coincidencia, veras:
 *   { "mensaje": "Acceso denegado" }
 * Esto confirma que Apache, PHP y MySQL (via mysqli) estan hablando
 * correctamente entre si, igual que en la guia original.
 *
 * El servicio real para la app movil es crud/operacion.php (ver README.md).
 */

require_once 'bd/conexion_bd.php';

header('Content-Type: application/json; charset=utf-8');

try {
    $res = consultar("SELECT * FROM Usuarios WHERE email = '' AND password = ''");
    if ($res != NULL && $res->num_rows > 0) {
        echo json_encode($res->fetch_assoc());
    } else {
        echo json_encode(array("mensaje" => "Acceso denegado"));
    }
} catch (Exception $error) {
    echo json_encode(array("mensaje" => $error->getMessage()));
}
