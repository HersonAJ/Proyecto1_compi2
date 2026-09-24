package com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorY;

import com.example.contacto_3xtrat3r3str3.c3d.Cuarteta;
import com.example.contacto_3xtrat3r3str3.c3d.TablaEtiquetas;
import com.example.contacto_3xtrat3r3str3.c3d.TablaOffsets;
import com.example.contacto_3xtrat3r3str3.c3d.TablaTemporales;
import com.example.contacto_3xtrat3r3str3.y.ast.*;
import java.util.List;

/**
 * Genera cuartetas para las expresiones de Y?.
 *
 * Modelo del video:
 *   - Variables: se acceden por offset desde BP.
 *   - Literales: se usan directamente.
 *   - Operaciones binarias: generan temporales.
 *   - Booleanos: se evalúan con saltos condicionales.
 *   - Cortocircuito: && y || usan saltos.
 */
public class GeneradorExpresionesY {

    private final List<Cuarteta> cuartetas;
    private final TablaTemporales temporales;
    private final TablaEtiquetas etiquetas;
    private final TablaOffsets offsets;

    public GeneradorExpresionesY(List<Cuarteta> cuartetas,
                                 TablaTemporales temporales,
                                 TablaEtiquetas etiquetas,
                                 TablaOffsets offsets) {
        this.cuartetas = cuartetas;
        this.temporales = temporales;
        this.etiquetas = etiquetas;
        this.offsets = offsets;
    }

    // GENERACION DE VALOR (expresión → nombre del lugar donde quedó)
    /**
     * Genera cuartetas para calcular el valor de una expresión.
     * Devuelve el nombre del lugar donde quedó el valor:
     *   - Un literal ("5", "\"hola\"", "1").
     *   - Un temporal ("t1", "t2").
     *   - Una referencia al stack ("stack[t1]").
     */
    public String generarValor(NodoExpr expr) {
        if (expr == null) return "-";

        return switch (expr.tipoNodo()) {
            case LITERAL_ENTERO -> String.valueOf(((NodoExpr.LiteralEntero) expr).valor());
            case LITERAL_FLOTANTE -> String.valueOf(((NodoExpr.LiteralFlotante) expr).valor());
            case LITERAL_CADENA -> "\"" + ((NodoExpr.LiteralCadena) expr).valor() + "\"";
            case LITERAL_CARACTER -> "'" + ((NodoExpr.LiteralCaracter) expr).valor() + "'";
            case LITERAL_BOOL -> ((NodoExpr.LiteralBool) expr).valor() ? "1" : "0";

            case IDENTIFICADOR -> leerVariable((NodoExpr.Identificador) expr);

            case ACCESO_ARRAY -> leerArreglo((NodoExpr.AccesoArray) expr);

            case ACCESO_ATRIBUTO -> leerAtributo((NodoExpr.AccesoAtributo) expr);

            case BINARIA -> generarBinaria((NodoExpr.Binaria) expr);

            case UNARIA -> generarUnaria((NodoExpr.Unaria) expr);

            case LLAMADA_FUNCION -> generarLlamadaFuncion((NodoExpr.LlamadaFuncion) expr);
        };
    }

    // LECTURA DE VARIABLES
    /**
     * Lee una variable del stack.
     * Genera:
     *   (+, BP, offset, t1)
     *   (=, stack[t1], -, t2)
     * Devuelve "t2".
     */
    private String leerVariable(NodoExpr.Identificador id) {
        int offset = offsets.obtener(id.nombre());
        if (offset < 0) {
            // No debería pasar tras validación semántica
            return id.nombre();
        }

        String t1 = temporales.nuevo();
        emitir("+", "BP", String.valueOf(offset), t1);

        String t2 = temporales.nuevo();
        emitir("=", "stack[" + t1 + "]", "-", t2);

        return t2;
    }

    // LECTURA DE ARREGLOS
    /**
     * Lee un elemento de un arreglo.
     * Por ahora asume que el arreglo es una variable local.
     * Genera:
     *   (+, BP, offset_arreglo, t1)
     *   (+, t1, indice, t2)
     *   (=, stack[t2], -, t3)
     * Devuelve "t3".
     */
    private String leerArreglo(NodoExpr.AccesoArray acceso) {
        // Caso simple: arreglo es un identificador
        if (acceso.arreglo() instanceof NodoExpr.Identificador id) {
            int offset = offsets.obtener(id.nombre());
            if (offset < 0) return "?";

            String indice = generarValor(acceso.indice());

            // Dirección base del arreglo
            String t1 = temporales.nuevo();
            emitir("+", "BP", String.valueOf(offset), t1);

            // Dirección del elemento: base + índice
            String t2 = temporales.nuevo();
            emitir("+", t1, indice, t2);

            // Valor del elemento
            String t3 = temporales.nuevo();
            emitir("=", "stack[" + t2 + "]", "-", t3);

            return t3;
        }
        return "?";
    }

    // LECTURA DE ATRIBUTOS
    /**
     * Lee un atributo de una estructura.
     * Por ahora asume que el objeto es una variable local con offset.
     */
    private String leerAtributo(NodoExpr.AccesoAtributo acceso) {
        String obj = generarValor(acceso.objeto());
        return obj + "." + acceso.atributo();
    }

    // OPERACIONES BINARIAS
    /**
     * Genera cuartetas para una operación binaria.
     * Para operadores aritméticos y relacionales simples, devuelve un temporal.
     * Para operadores lógicos (&&, ||), usa saltos condicionales.
     */
    private String generarBinaria(NodoExpr.Binaria bin) {
        String op = bin.operador();

        // Cortocircuito para && y ||
        if ("&&".equals(op) || "||".equals(op)) {
            return generarLogico(bin);
        }

        // Operadores aritméticos y relacionales simples
        String izq = generarValor(bin.izquierda());
        String der = generarValor(bin.derecha());
        String t = temporales.nuevo();

        emitir(op, izq, der, t);
        return t;
    }

    /**
     * Genera cuartetas para una operación lógica (&&, ||) con cortocircuito.
     * Sigue el patrón del video:
     *   - El resultado se guarda en un temporal (1 o 0).
     *   - Se usan etiquetas para las ramas verdadera y falsa.
     */
    private String generarLogico(NodoExpr.Binaria bin) {
        String t = temporales.nuevo();
        String L_verdadero = etiquetas.nueva();
        String L_falso = etiquetas.nueva();
        String L_fin = etiquetas.nueva();

        generarCondicion(bin, L_verdadero, L_falso);

        // Etiqueta verdadera: t = 1
        emitir("label", "-", "-", L_verdadero);
        emitir("=", "1", "-", t);
        emitir("goto", "-", "-", L_fin);

        // Etiqueta falsa: t = 0
        emitir("label", "-", "-", L_falso);
        emitir("=", "0", "-", t);

        // Fin
        emitir("label", "-", "-", L_fin);

        return t;
    }

    // OPERACIONES UNARIAS
    /**
     * Genera cuartetas para una operación unaria.
     *   - '-' → negación aritmética.
     *   - '!' → negación lógica (se maneja con condicional).
     *   - '++' / '--' → incremento/decremento.
     */
    private String generarUnaria(NodoExpr.Unaria un) {
        String op = un.operador();

        if ("!".equals(op)) {
            // Negación lógica: usar condicional invertido
            String t = temporales.nuevo();
            String L_verdadero = etiquetas.nueva();
            String L_falso = etiquetas.nueva();
            String L_fin = etiquetas.nueva();

            // Para '!expr', la condición se invierte: expr verdadero → t = 0
            generarCondicion(un.operando(), L_falso, L_verdadero);

            emitir("label", "-", "-", L_verdadero);
            emitir("=", "1", "-", t);
            emitir("goto", "-", "-", L_fin);

            emitir("label", "-", "-", L_falso);
            emitir("=", "0", "-", t);

            emitir("label", "-", "-", L_fin);
            return t;
        }

        // Negación aritmética
        String valor = generarValor(un.operando());
        String t = temporales.nuevo();
        emitir(op, valor, "-", t);
        return t;
    }

    // LLAMADAS A FUNCION
    /**
     * Genera cuartetas para llamar a una función.
     *   - Se pasan los parámetros con 'param'.
     *   - Se llama con 'call'.
     *   - Se devuelve el temporal con el resultado.
     */
    private String generarLlamadaFuncion(NodoExpr.LlamadaFuncion llamada) {
        // Pasar parámetros
        for (NodoExpr arg : llamada.argumentos()) {
            String valor = generarValor(arg);
            emitir("param", valor, "-", "-");
        }

        // Llamar
        String t = temporales.nuevo();
        emitir("call", llamada.nombre(), String.valueOf(llamada.argumentos().size()), t);
        return t;
    }

    // GENERACION DE CONDICIONES (para booleanos)
    /**
     * Genera saltos condicionales para evaluar una expresión booleana.
     * Si la expresión es verdadera, salta a Ltrue.
     * Si es falsa, salta a Lfalse.
     *
     * Sigue el patrón del video para && y || con cortocircuito.
     */
    public void generarCondicion(NodoExpr expr, String Ltrue, String Lfalse) {
        if (expr == null) {
            emitir("goto", "-", "-", Lfalse);
            return;
        }

        // Negación lógica: intercambiar etiquetas
        if (expr instanceof NodoExpr.Unaria un && "!".equals(un.operador())) {
            generarCondicion(un.operando(), Lfalse, Ltrue);
            return;
        }

        // Cortocircuito AND
        if (expr instanceof NodoExpr.Binaria bin && "&&".equals(bin.operador())) {
            String L_siguiente = etiquetas.nueva();
            generarCondicion(bin.izquierda(), L_siguiente, Lfalse);
            emitir("label", "-", "-", L_siguiente);
            generarCondicion(bin.derecha(), Ltrue, Lfalse);
            return;
        }

        // Cortocircuito OR
        if (expr instanceof NodoExpr.Binaria bin && "||".equals(bin.operador())) {
            String L_siguiente = etiquetas.nueva();
            generarCondicion(bin.izquierda(), Ltrue, L_siguiente);
            emitir("label", "-", "-", L_siguiente);
            generarCondicion(bin.derecha(), Ltrue, Lfalse);
            return;
        }

        // Comparación relacional
        if (expr instanceof NodoExpr.Binaria bin && esRelacional(bin.operador())) {
            String izq = generarValor(bin.izquierda());
            String der = generarValor(bin.derecha());
            emitir("if" + bin.operador(), izq, der, Ltrue);
            emitir("goto", "-", "-", Lfalse);
            return;
        }

        // Caso base: cualquier otro valor (identificador, literal, llamada)
        String valor = generarValor(expr);
        emitir("if", valor, "-", Ltrue);
        emitir("goto", "-", "-", Lfalse);
    }

    private boolean esRelacional(String operador) {
        return switch (operador) {
            case "==", "!=", "<", ">", "<=", ">=" -> true;
            default -> false;
        };
    }

    // HELPER
    private void emitir(String op, String a1, String a2, String res) {
        cuartetas.add(new Cuarteta(op, a1, a2, res));
    }
}