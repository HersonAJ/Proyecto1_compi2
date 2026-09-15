package com.example.contacto_3xtrat3r3str3.y.lexer;


/**
 * Se lanza cuando el nivel de indentación de una línea no coincide
 * con ningún nivel de bloque actualmente abierto (por ejemplo, un
 * DEDENT parcial: bajar a un número de tabs que nunca se usó al abrir
 * un bloque).
 */
public class IndentacionInvalidaException extends RuntimeException {
    public IndentacionInvalidaException(String mensaje) {
        super(mensaje);
    }
}