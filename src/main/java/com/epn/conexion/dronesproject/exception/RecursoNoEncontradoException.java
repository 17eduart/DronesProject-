package com.epn.conexion.dronesproject.exception;

/**
 * Se pidio algo que no existe. GlobalException la traduce a 404 con el mismo
 * formato JSON que el resto de errores de la aplicacion.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
