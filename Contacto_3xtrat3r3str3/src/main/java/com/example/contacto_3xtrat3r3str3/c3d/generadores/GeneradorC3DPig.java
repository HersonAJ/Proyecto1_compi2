package com.example.contacto_3xtrat3r3str3.c3d.generadores;

import com.example.contacto_3xtrat3r3str3.c3d.Cuarteta;
import com.example.contacto_3xtrat3r3str3.c3d.TablaEtiquetas;
import com.example.contacto_3xtrat3r3str3.c3d.TablaTemporales;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.*;
import com.example.contacto_3xtrat3r3str3.piglatin.semantica.TablaSimbolosPig;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Genera código de tres direcciones (cuartetas) a partir del AST de PigLatin.
 *
 * PigLatin tiene:
 *   - Variables globales (sección VARIABILES>).
 *   - Cuerpo del MAIOR> (sección principal).
 *   - Uso de símbolos importados de Y? y Z.
 *
 * El cuerpo del MAIOR se genera como la función `main` en C.
 */
public class GeneradorC3DPig {

    private final List<Cuarteta> cuarteta = new ArrayList<>();
    private final TablaTemporales temporales = new TablaTemporales();
    private final TablaEtiquetas etiquetas = new TablaEtiquetas();
    private final TablaSimbolosPig tabla;

    // pila de contexto para romper/continuar
    private final Deque<String> pilaBreak = new ArrayDeque<>();
    private final Deque<String> pilaContinue = new ArrayDeque<>();

    public GeneradorC3DPig(TablaSimbolosPig tabla) {
        this.tabla = tabla;
    }

    // PUNTO DE ENTRADA
    public List<Cuarteta> generar(NodoPrograma programa) {
        cuarteta.clear();
        temporales.reiniciar();
        etiquetas.reiniciar();
        pilaBreak.clear();
        pilaContinue.clear();

        // 1. Variables globales (se emiten como declaraciones globales)
        for (NodoSentencia s : programa.variablesGlobales()) {
            generarSentenciaGlobal(s);
        }

        // 2. Cuerpo del MAIOR (se emite como main)
        emitir("main", "-", "-", "-");
        for (NodoSentencia s : programa.cuerpoMain()) {
            generarSentencia(s);
        }
        emitir("end-main", "-", "-", "-");

        return cuarteta;
    }

    // VARIABLES GLOBALES
    private void generarSentenciaGlobal(NodoSentencia s) {
        switch (s.tipoNodo()) {
            case DECLARACION_VARIABLE -> generarDeclaracionVariable((NodoSentencia.DeclaracionVariable) s);
            case DECLARACION_ARREGLO -> generarDeclaracionArreglo((NodoSentencia.DeclaracionArreglo) s);
            case DECLARACION_STRUCT -> generarDeclaracionStruct((NodoSentencia.DeclaracionStruct) s);
            default -> { /* no aplica */ }
        }
    }

    // SENTENCIAS
    private void generarSentencia(NodoSentencia s) {
        switch (s.tipoNodo()) {
            case DECLARACION_VARIABLE -> generarDeclaracionVariable((NodoSentencia.DeclaracionVariable) s);
            case DECLARACION_ARREGLO -> generarDeclaracionArreglo((NodoSentencia.DeclaracionArreglo) s);
            case DECLARACION_STRUCT -> generarDeclaracionStruct((NodoSentencia.DeclaracionStruct) s);
            case ASIGNACION -> generarAsignacion((NodoSentencia.Asignacion) s);
            case INCREMENTO_DECREMENTO -> generarIncrementoDecremento((NodoSentencia.IncrementoDecremento) s);
            case CONDICIONAL -> generarCondicional((NodoSentencia.Condicional) s);
            case CICLO_DUM -> generarCicloDum((NodoSentencia.CicloDum) s);
            case CICLO_FACERE -> generarCicloFacere((NodoSentencia.CicloFacere) s);
            case CICLO_PER -> generarCicloPer((NodoSentencia.CicloPer) s);
            case LECTURA -> generarLectura((NodoSentencia.Lectura) s);
            case ESCRITURA -> generarEscritura((NodoSentencia.Escritura) s);
            case INTERRUPCION_CICLO -> generarInterrupcion((NodoSentencia.InterrupcionCiclo) s);
            case LLAMADA_FUNCION_SENTENCIA -> generarLlamadaFuncionSentencia((NodoSentencia.LlamadaFuncionSentencia) s);
            case LLAMADA_METODO_SENTENCIA -> generarLlamadaMetodoSentencia((NodoSentencia.LlamadaMetodoSentencia) s);
        }
    }

    // DECLARACIONES
    private void generarDeclaracionVariable(NodoSentencia.DeclaracionVariable d) {
        if (d.inicializacion() == null) {
            emitir("=", "0", "-", d.nombre());
            return;
        }

        String valor = generarExpresion(d.inicializacion());
        emitir("=", valor, "-", d.nombre());
    }

    private void generarDeclaracionArreglo(NodoSentencia.DeclaracionArreglo d) {
        // 'series arr[3] : tipo {1,2,3}' o sin inicialización
        if (d.inicializacion() != null && !d.inicializacion().isEmpty()) {
            for (int i = 0; i < d.inicializacion().size(); i++) {
                String valor = generarExpresion(d.inicializacion().get(i));
                emitir("=", valor, "-", d.nombre() + "[" + i + "]");
            }
        }
    }

    private void generarDeclaracionStruct(NodoSentencia.DeclaracionStruct d) {
        // 'esto x : Tipo {valores}'
        if (d.inicializacion() == null || d.inicializacion().isEmpty()) {
            // Declaración sin inicialización
            return;
        }

        // Obtener nombres de campos de la estructura importada
        List<String> campos = new ArrayList<>();
        Optional<TablaSimbolosPig.DefinicionEstructura> def = tabla.buscarEstructura(d.tipo());
        if (def.isPresent()) {
            campos = new ArrayList<>(def.get().atributos().keySet());
        }

        for (int i = 0; i < d.inicializacion().size(); i++) {
            String valor = generarExpresion(d.inicializacion().get(i));
            String campo = (i < campos.size()) ? campos.get(i) : String.valueOf(i);
            emitir("=", valor, "-", d.nombre() + "." + campo);
        }
    }

    // ASIGNACIONES
    private void generarAsignacion(NodoSentencia.Asignacion a) {
        String valor = generarExpresion(a.valor());
        String destino = generarReferencia(a.destino());
        emitir("=", valor, "-", destino);
    }

    private void generarIncrementoDecremento(NodoSentencia.IncrementoDecremento inc) {
        String operando = generarReferencia(inc.operando());
        if ("++".equals(inc.operador())) {
            emitir("+", operando, "1", operando);
        } else {
            emitir("-", operando, "1", operando);
        }
    }

    // CONDICIONAL
    private void generarCondicional(NodoSentencia.Condicional c) {
        String L_si = etiquetas.nueva();
        String L_siguiente = etiquetas.nueva();
        String L_fin = etiquetas.nueva();

        generarCondicion(c.condicion(), L_si, L_siguiente);

        emitir("label", "-", "-", L_si);
        for (NodoSentencia s : c.cuerpoSi()) generarSentencia(s);
        emitir("goto", "-", "-", L_fin);

        emitir("label", "-", "-", L_siguiente);

        // Ramas aliter con condición
        for (NodoSentencia.RamaAliter r : c.ramasAliter()) {
            String L_aliterSi = etiquetas.nueva();
            String L_aliterSig = etiquetas.nueva();
            generarCondicion(r.condicion(), L_aliterSi, L_aliterSig);

            emitir("label", "-", "-", L_aliterSi);
            for (NodoSentencia s : r.cuerpo()) generarSentencia(s);
            emitir("goto", "-", "-", L_fin);

            emitir("label", "-", "-", L_aliterSig);
        }

        // Rama aliter final (else)
        if (c.cuerpoAliter() != null) {
            for (NodoSentencia s : c.cuerpoAliter()) generarSentencia(s);
        }

        emitir("label", "-", "-", L_fin);
    }

    // CICLOS
    private void generarCicloDum(NodoSentencia.CicloDum c) {
        String L_inicio = etiquetas.nueva();
        String L_cuerpo = etiquetas.nueva();
        String L_fin = etiquetas.nueva();

        emitir("label", "-", "-", L_inicio);
        generarCondicion(c.condicion(), L_cuerpo, L_fin);
        emitir("label", "-", "-", L_cuerpo);

        pilaBreak.push(L_fin);
        pilaContinue.push(L_inicio);

        for (NodoSentencia s : c.cuerpo()) generarSentencia(s);

        pilaBreak.pop();
        pilaContinue.pop();

        emitir("goto", "-", "-", L_inicio);
        emitir("label", "-", "-", L_fin);
    }

    private void generarCicloFacere(NodoSentencia.CicloFacere c) {
        String L_inicio = etiquetas.nueva();
        String L_condicion = etiquetas.nueva();
        String L_fin = etiquetas.nueva();

        emitir("label", "-", "-", L_inicio);

        pilaBreak.push(L_fin);
        pilaContinue.push(L_condicion);

        for (NodoSentencia s : c.cuerpo()) generarSentencia(s);

        pilaBreak.pop();
        pilaContinue.pop();

        emitir("label", "-", "-", L_condicion);
        generarCondicion(c.condicion(), L_inicio, L_fin);
        emitir("label", "-", "-", L_fin);
    }

    private void generarCicloPer(NodoSentencia.CicloPer c) {
        String L_inicio = etiquetas.nueva();
        String L_cuerpo = etiquetas.nueva();
        String L_actualizacion = etiquetas.nueva();
        String L_fin = etiquetas.nueva();

        // Inicialización
        if (c.inicializacion() != null) generarSentencia(c.inicializacion());

        emitir("label", "-", "-", L_inicio);
        generarCondicion(c.condicion(), L_cuerpo, L_fin);
        emitir("label", "-", "-", L_cuerpo);

        pilaBreak.push(L_fin);
        pilaContinue.push(L_actualizacion);

        for (NodoSentencia s : c.cuerpo()) generarSentencia(s);

        pilaBreak.pop();
        pilaContinue.pop();

        emitir("label", "-", "-", L_actualizacion);
        if (c.actualizacion() != null) generarSentencia(c.actualizacion());

        emitir("goto", "-", "-", L_inicio);
        emitir("label", "-", "-", L_fin);
    }

    // LECTURA / ESCRITURA / INTERRUPCION
    private void generarLectura(NodoSentencia.Lectura l) {
        if (l.variable() != null) {
            emitir("read", "-", "-", l.variable());
        } else {
            emitir("read", "-", "-", "-");
        }
    }

    private void generarEscritura(NodoSentencia.Escritura e) {
        for (NodoExpr v : e.valores()) {
            String valor = generarExpresion(v);
            emitir("print", valor, "-", "-");
        }
    }

    private void generarInterrupcion(NodoSentencia.InterrupcionCiclo i) {
        if ("perge".equals(i.tipo())) {
            // continue
            if (!pilaContinue.isEmpty()) {
                emitir("goto", "-", "-", pilaContinue.peek());
            }
        } else {
            // break
            if (!pilaBreak.isEmpty()) {
                emitir("goto", "-", "-", pilaBreak.peek());
            }
        }
    }

    // LLAMADAS COMO SENTENCIA
    private void generarLlamadaFuncionSentencia(NodoSentencia.LlamadaFuncionSentencia l) {
        generarExpresion(l.llamada());
    }

    private void generarLlamadaMetodoSentencia(NodoSentencia.LlamadaMetodoSentencia l) {
        generarExpresion(l.llamada());
    }

    // EXPRESIONES
    private String generarExpresion(NodoExpr expr) {
        if (expr == null) return "-";

        switch (expr.tipoNodo()) {
            case LITERAL_ENTERO -> { return String.valueOf(((NodoExpr.LiteralEntero) expr).valor()); }
            case LITERAL_DECIMAL -> { return String.valueOf(((NodoExpr.LiteralDecimal) expr).valor()); }
            case LITERAL_TEXTO -> { return "\"" + ((NodoExpr.LiteralTexto) expr).valor() + "\""; }
            case LITERAL_CARACTER -> { return "'" + ((NodoExpr.LiteralCaracter) expr).valor() + "'"; }
            case LITERAL_BOOL -> { return ((NodoExpr.LiteralBool) expr).valor() ? "1" : "0"; }

            case IDENTIFICADOR -> {
                return ((NodoExpr.Identificador) expr).nombre();
            }

            case ACCESO_ARRAY -> {
                NodoExpr.AccesoArray a = (NodoExpr.AccesoArray) expr;
                String arr = generarExpresion(a.arreglo());
                String idx = generarExpresion(a.indice());
                return arr + "[" + idx + "]";
            }

            case ACCESO_ATRIBUTO -> {
                NodoExpr.AccesoAtributo a = (NodoExpr.AccesoAtributo) expr;
                String obj = generarExpresion(a.objeto());
                return obj + "." + a.atributo();
            }

            case BINARIA -> {
                NodoExpr.Binaria b = (NodoExpr.Binaria) expr;
                String izq = generarExpresion(b.izquierda());
                String der = generarExpresion(b.derecha());
                String t = temporales.nuevo();
                String op = ("+".equals(b.operador()) && esTipoCadena(b.izquierda()))
                        ? "concat" : b.operador();
                emitir(op, izq, der, t);
                return t;
            }

            case INCREMENTO_DECREMENTO -> {
                NodoExpr.IncrementoDecremento inc = (NodoExpr.IncrementoDecremento) expr;
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

            case LLAMADA_FUNCION -> {
                NodoExpr.LlamadaFuncion l = (NodoExpr.LlamadaFuncion) expr;
                for (NodoExpr arg : l.argumentos()) {
                    String val = generarExpresion(arg);
                    emitir("param", val, "-", "-");
                }
                String t = temporales.nuevo();
                emitir("call", l.nombre(), String.valueOf(l.argumentos().size()), t);
                return t;
            }

            case LLAMADA_METODO -> {
                NodoExpr.LlamadaMetodo lm = (NodoExpr.LlamadaMetodo) expr;
                String obj = generarExpresion(lm.objeto());
                for (NodoExpr arg : lm.argumentos()) {
                    String val = generarExpresion(arg);
                    emitir("param", val, "-", "-");
                }
                String t = temporales.nuevo();
                emitir("call", obj + "." + lm.nombre(), String.valueOf(lm.argumentos().size()), t);
                return t;
            }

            case INSTANCIA_OBJETO -> {
                NodoExpr.InstanciaObjeto inst = (NodoExpr.InstanciaObjeto) expr;
                for (NodoExpr arg : inst.argumentos()) {
                    String val = generarExpresion(arg);
                    emitir("param", val, "-", "-");
                }
                String t = temporales.nuevo();
                emitir("new", inst.tipoClase(), String.valueOf(inst.argumentos().size()), t);
                return t;
            }

            case LISTA_LITERAL -> {
                return "?"; // solo aparece en contextos específicos
            }
        }

        return "?";
    }

    private String generarReferencia(NodoExpr expr) {
        if (expr == null) return "-";
        if (expr instanceof NodoExpr.Identificador id) return id.nombre();
        if (expr instanceof NodoExpr.AccesoArray a) {
            String arr = generarExpresion(a.arreglo());
            String idx = generarExpresion(a.indice());
            return arr + "[" + idx + "]";
        }
        if (expr instanceof NodoExpr.AccesoAtributo a) {
            String obj = generarExpresion(a.objeto());
            return obj + "." + a.atributo();
        }
        return "?";
    }

    // EVALUACION EN CONDICIONALES
    private void generarCondicion(NodoExpr expr, String Ltrue, String Lfalse) {
        if (expr == null) {
            emitir("goto", "-", "-", Lfalse);
            return;
        }

        if (expr instanceof NodoExpr.Binaria b && "&&".equals(b.operador())) {
            String Lsiguiente = etiquetas.nueva();
            generarCondicion(b.izquierda(), Lsiguiente, Lfalse);
            emitir("label", "-", "-", Lsiguiente);
            generarCondicion(b.derecha(), Ltrue, Lfalse);
            return;
        }

        if (expr instanceof NodoExpr.Binaria b && "||".equals(b.operador())) {
            String Lsiguiente = etiquetas.nueva();
            generarCondicion(b.izquierda(), Ltrue, Lsiguiente);
            emitir("label", "-", "-", Lsiguiente);
            generarCondicion(b.derecha(), Ltrue, Lfalse);
            return;
        }

        if (expr instanceof NodoExpr.Binaria b && esRelacional(b.operador())) {
            String izq = generarExpresion(b.izquierda());
            String der = generarExpresion(b.derecha());
            emitir("if" + b.operador(), izq, der, Ltrue);
            emitir("goto", "-", "-", Lfalse);
            return;
        }

        // caso base
        String valor = generarExpresion(expr);
        emitir("if", valor, "-", Ltrue);
        emitir("goto", "-", "-", Lfalse);
    }

    private boolean esRelacional(String operador) {
        return switch (operador) {
            case "==", "!=", "<", ">", "<=", ">=" -> true;
            default -> false;
        };
    }

    // INFERENCIA APROXIMADA DE TIPO
    private boolean esTipoCadena(NodoExpr expr) {
        if (expr instanceof NodoExpr.LiteralTexto) return true;

        if (expr instanceof NodoExpr.Identificador id) {
            return tabla.buscarVariable(id.nombre())
                    .map(v -> "textum".equals(v.tipo()))
                    .orElse(false);
        }

        if (expr instanceof NodoExpr.Binaria b && "+".equals(b.operador())) {
            return esTipoCadena(b.izquierda()) || esTipoCadena(b.derecha());
        }

        return false;
    }

    private void emitir(String op, String a1, String a2, String res) {
        cuarteta.add(new Cuarteta(op, a1, a2, res));
    }
}
