-- =====================================================================
-- Guia: Desarrollo de App Android conectada a una aplicacion Web
-- Script de creacion de la Base de Datos para el proyecto CrudPhpJson
--
-- Como usarlo (elige una opcion):
--   A) phpMyAdmin: Importar > seleccionar este archivo > Continuar.
--   B) MySQL Workbench: File > Open SQL Script... > abrir este archivo
--      > boton del rayo (Execute) para correrlo completo.
--
-- Este script crea la BD si no existe, la selecciona y crea la tabla
-- Usuarios exactamente como la pide la guia (email, password, nombre).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS crudphpjson
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE crudphpjson;

-- CREAR LA TABLA
CREATE TABLE IF NOT EXISTS Usuarios (
    email    VARCHAR(70)  PRIMARY KEY NOT NULL,
    password VARCHAR(40)  NOT NULL,
    nombre   VARCHAR(100) NOT NULL
) ENGINE=INNODB;

-- (Opcional) Un usuario de prueba para poder probar "login" y "listar"
-- de inmediato sin tener que registrar uno primero.
-- Descomenta la siguiente linea si quieres datos de prueba:
-- INSERT INTO Usuarios (email, password, nombre) VALUES ('arrietajohn@gmail.com', '1234', 'JOHN ARRIETA');
