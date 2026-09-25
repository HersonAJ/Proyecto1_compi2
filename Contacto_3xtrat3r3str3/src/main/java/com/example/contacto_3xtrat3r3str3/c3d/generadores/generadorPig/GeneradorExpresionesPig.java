package com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorPig;

import com.example.contacto_3xtrat3r3str3.c3d.CuartetaV1;
import com.example.contacto_3xtrat3r3str3.c3d.TablaEtiquetas;
import com.example.contacto_3xtrat3r3str3.c3d.TablaOffsets;
import com.example.contacto_3xtrat3r3str3.c3d.TablaTemporales;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoExpr;

import java.util.List;

// Genera cuartetas para las expresiones de PigLatin
public class GeneradorExpresionesPig {

    private final List<CuartetaV1> cuartetas;
    private final TablaTemporales temporales;
    private final TablaEtiquetas etiquetas;
    private final TablaOffsets offsets;

    public GeneradorExpresionesPig(List<CuartetaV1> cuartetas,
                                   TablaTemporales temporales,
                                   TablaEtiquetas etiquetas,
                                   TablaOffsets offsets) {
        this.cuartetas = cuartetas;
        this.temporales = temporales;
        this.etiquetas = etiquetas;
        this.offsets = offsets;
    }

    // GENERACION DE VALOR
    public String generarValor(NodoExpr expr) {
        if (expr == null) return "-";

        return switch (expr.tipoNodo()) {
            case LITERAL_ENTERO -> String.valueOf(((NodoExpr.LiteralEntero) expr).valor());
            case LITERAL_DECIMAL -> String.valueOf(((NodoExpr.LiteralDecimal) expr).valor());
            case LITERAL_TEXTO -> "\"" + ((NodoExpr.LiteralTexto) expr).valor() + "\"";
            case LITERAL_CARACTER -> "'" + ((NodoExpr.LiteralCaracter) expr).valor() + "'";
            case LITERAL_BOOL -> ((NodoExpr.LiteralBool) expr).valor() ? "1" : "0";

            case IDENTIFICADOR -> leerVariable((NodoExpr.Identificador) expr);

            case ACCESO_ARRAY -> leerArreglo((NodoExpr.AccesoArray) expr);

            case ACCESO_ATRIBUTO -> leerAtributo((NodoExpr.AccesoAtributo) expr);

            case BINARIA -> generarBinaria((NodoExpr.Binaria) expr);

            case INCREMENTO_DECREMENTO -> generarIncrementoDecremento((NodoExpr.IncrementoDecremento) expr);

            case LLAMADA_FUNCION -> generarLlamadaFuncion((NodoExpr.LlamadaFuncion) expr);

            case LLAMADA_METODO -> generarLlamadaMetodo((NodoExpr.LlamadaMetodo) expr);

            case INSTANCIA_OBJETO -> generarInstanciaObjeto((NodoExpr.InstanciaObjeto) expr);

            case LISTA_LITERAL -> "?";
        };
    }

    // LECTURA DE VARIABLES
    private String leerVariable(NodoExpr.Identificador id) {
        int offset = offsets.obtener(id.nombre());
        if (offset < 0) return id.nombre();

        String t1 = temporales.nuevo();
        emitir("+", "BP", String.valueOf(offset), t1);

        String t2 = temporales.nuevo();
        emitir("=", "stack[" + t1 + "]", "-", t2);

        return t2;
    }

    // LECTURA DE ARREGLOS
    private String leerArreglo(NodoExpr.AccesoArray acceso) {
        if (acceso.arreglo() instanceof NodoExpr.Identificador id) {
            int offset = offsets.obtener(id.nombre());
            if (offset < 0) return "?";

            String indice = generarValor(acceso.indice());

            String t1 = temporales.nuevo();
            emitir("+", "BP", String.valueOf(offset), t1);

            String t2 = temporales.nuevo();
            emitir("+", t1, indice, t2);

            String t3 = temporales.nuevo();
            emitir("=", "stack[" + t2 + "]", "-", t3);

            return t3;
        }
        return "?";
    }

    // LECTURA DE ATRIBUTOS
    private String leerAtributo(NodoExpr.AccesoAtributo acceso) {
        String obj = generarValor(acceso.objeto());
        return obj + "." + acceso.atributo();
    }

    // OPERACIONES BINARIAS
    private String generarBinaria(NodoExpr.Binaria bin) {
        String op = bin.operador();

        if ("&&".equals(op) || "||".equals(op)) {
            return generarLogico(bin);
        }

        String izq = generarValor(bin.izquierda());
        String der = generarValor(bin.derecha());
        String t = temporales.nuevo();

        String opFinal = op;
        if ("+".equals(op) && esTipoCadena(bin.izquierda())) {
            opFinal = "concat";
        }

        emitir(opFinal, izq, der, t);
        return t;
    }

    private String generarLogico(NodoExpr.Binaria bin) {
        String t = temporales.nuevo();
        String L_verdadero = etiquetas.nueva();
        String L_falso = etiquetas.nueva();
        String L_fin = etiquetas.nueva();

        generarCondicion(bin, L_verdadero, L_falso);

        emitir("label", "-", "-", L_verdadero);
        emitir("=", "1", "-", t);
        emitir("goto", "-", "-", L_fin);

        emitir("label", "-", "-", L_falso);
        emitir("=", "0", "-", t);

        emitir("label", "-", "-", L_fin);
        return t;
    }

    // INCREMENTO / DECREMENTO
    private String generarIncrementoDecremento(NodoExpr.IncrementoDecremento inc) {
        String operando = generarReferencia(inc.operando());
        String t = temporales.nuevo();
        emitir("=", operando, "-", t);
        if ("++".equals(inc.operador())) {
            emitir("+", operando, "1", operando);
        } else {
            emitir("-", operando, "1", operando);
        }
        return t;
    }

    // LLAMADAS
    private String generarLlamadaFuncion(NodoExpr.LlamadaFuncion llamada) {
        for (NodoExpr arg : llamada.argumentos()) {
            String valor = generarValor(arg);
            emitir("param", valor, "-", "-");
        }
        String t = temporales.nuevo();
        emitir("call", llamada.nombre(), String.valueOf(llamada.argumentos().size()), t);
        return t;
    }

    private String generarLlamadaMetodo(NodoExpr.LlamadaMetodo llamada) {
        String obj = generarValor(llamada.objeto());
        for (NodoExpr arg : llamada.argumentos()) {
            String valor = generarValor(arg);
            emitir("param", valor, "-", "-");
        }
        String t = temporales.nuevo();
        emitir("call", obj + "." + llamada.nombre(), String.valueOf(llamada.argumentos().size()), t);
        return t;
    }

    private String generarInstanciaObjeto(NodoExpr.InstanciaObjeto inst) {
        for (NodoExpr arg : inst.argumentos()) {
            String valor = generarValor(arg);
            emitir("param", valor, "-", "-");
        }
        String t = temporales.nuevo();
        emitir("new", inst.tipoClase(), String.valueOf(inst.argumentos().size()), t);
        return t;
    }

    // REFERENCIAS
    public String generarReferencia(NodoExpr expr) {
        if (expr == null) return "-";
        if (expr instanceof NodoExpr.Identificador id) return id.nombre();
        if (expr instanceof NodoExpr.AccesoArray a) {
            String arr = generarValor(a.arreglo());
            String idx = generarValor(a.indice());
            return arr + "[" + idx + "]";
        }
        if (expr instanceof NodoExpr.AccesoAtributo a) {
            String obj = generarValor(a.objeto());
            return obj + "." + a.atributo();
        }
        return "?";
    }

    // GENERACION DE CONDICIONES
    public void generarCondicion(NodoExpr expr, String Ltrue, String Lfalse) {
        if (expr == null) {
            emitir("goto", "-", "-", Lfalse);
            return;
        }

        if (expr instanceof NodoExpr.Binaria bin && "&&".equals(bin.operador())) {
            String L_siguiente = etiquetas.nueva();
            generarCondicion(bin.izquierda(), L_siguiente, Lfalse);
            emitir("label", "-", "-", L_siguiente);
            generarCondicion(bin.derecha(), Ltrue, Lfalse);
            return;
        }

        if (expr instanceof NodoExpr.Binaria bin && "||".equals(bin.operador())) {
            String L_siguiente = etiquetas.nueva();
            generarCondicion(bin.izquierda(), Ltrue, L_siguiente);
            emitir("label", "-", "-", L_siguiente);
            generarCondicion(bin.derecha(), Ltrue, Lfalse);
            return;
        }

        if (expr instanceof NodoExpr.Binaria bin && esRelacional(bin.operador())) {
            String izq = generarValor(bin.izquierda());
            String der = generarValor(bin.derecha());
            emitir("if" + bin.operador(), izq, der, Ltrue);
            emitir("goto", "-", "-", Lfalse);
            return;
        }

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

    private boolean esTipoCadena(NodoExpr expr) {
        if (expr instanceof NodoExpr.LiteralTexto) return true;
        if (expr instanceof NodoExpr.Binaria bin && "+".equals(bin.operador())) {
            return esTipoCadena(bin.izquierda()) || esTipoCadena(bin.derecha());
        }
        return false;
    }

    private void emitir(String op, String a1, String a2, String res) {
        cuartetas.add(new CuartetaV1(op, a1, a2, res));
    }
}
