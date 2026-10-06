<?php


$bd;

function conectar(){
    try{
        global $bd;

        // Datos de conexion por defecto de XAMPP:
        //   host: localhost | usuario: root | password: "" (vacia)
        $bd = new mysqli("localhost", "root", "", "crudphpjson");

        if ($bd->connect_error) {
            throw new Exception($bd->connect_error);
        }

        $bd->set_charset("utf8mb4");

    }catch(Exception $error){
        // Si la conexion fallo, dejamos $bd en NULL para que el siguiente
        // intento vuelva a conectarse en lugar de usar un objeto roto.
        $bd = NULL;
        throw new Exception("No se pudo conectar con la base de datos: " . $error->getMessage());
    }
}

function consultar($sql){
    global $bd;

    try{
        if($bd == NULL){
            conectar();
        }
        return $bd->query($sql);

    }catch(Exception $error){
        // Solo el texto del error; el JSON lo arma operacion.php
        throw new Exception($error->getMessage());
    }
}