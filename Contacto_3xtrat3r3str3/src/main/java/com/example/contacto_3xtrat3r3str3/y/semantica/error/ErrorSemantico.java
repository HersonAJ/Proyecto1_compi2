package com.example.contacto_3xtrat3r3str3.y.semantica.error;

public record ErrorSemantico(int linea, int columna, String mensaje, String categoria) {

    @Override
    public String toString() {
        return "[" + categoria + "] línea " + linea + ", columna " + columna + ": " + mensaje;
    }
}