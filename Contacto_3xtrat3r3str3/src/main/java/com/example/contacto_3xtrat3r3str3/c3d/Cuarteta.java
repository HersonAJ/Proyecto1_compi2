package com.example.contacto_3xtrat3r3str3.c3d;

public record Cuarteta(String operador, String arg1, String arg2, String resultado) {

    public static final String NO_APLICA = "-";

    public Cuarteta(String operador, String arg1, String arg2, String resultado) {
        this.operador = operador;
        this.arg1 = arg1 != null ? arg1 : NO_APLICA;
        this.arg2 = arg2 != null ? arg2 : NO_APLICA;
        this.resultado = resultado != null ? resultado : NO_APLICA;
    }

    @Override
    public String toString() {
        return "(" + operador + ", " + arg1 + ", " + arg2 + ", " + resultado + ")";
    }
}
