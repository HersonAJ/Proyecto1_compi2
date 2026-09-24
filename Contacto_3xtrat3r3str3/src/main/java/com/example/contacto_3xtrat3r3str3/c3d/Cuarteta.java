package com.example.contacto_3xtrat3r3str3.c3d;

/**
 * Representa una cuarteta de código de tres direcciones.
 *
 * Formato: (operador, arg1, arg2, resultado)
 *
 * Ejemplos:
 *   (+, BP, offset, t1)        → t1 = BP + offset
 *   (=, stack[t1], -, t2)      → t2 = stack[t1]
 *   (+, t2, t3, t4)            → t4 = t2 + t3
 *   (if<, t2, t3, L1)          → if t2 < t3 goto L1
 *   (goto, -, -, L2)           → goto L2
 *   (label, -, -, L1)          → L1:
 *   (call, func, 2, t1)        → t1 = func(a, b)
 *   (return, t1, -, -)         → return t1
 */
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
