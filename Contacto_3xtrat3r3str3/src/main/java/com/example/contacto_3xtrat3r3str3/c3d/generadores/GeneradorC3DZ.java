package com.example.contacto_3xtrat3r3str3.c3d.generadores;

import com.example.contacto_3xtrat3r3str3.c3d.Cuarteta;
import com.example.contacto_3xtrat3r3str3.c3d.TablaEtiquetas;
import com.example.contacto_3xtrat3r3str3.c3d.TablaTemporales;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoClase;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoConstructor;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoMetodo;
import com.example.contacto_3xtrat3r3str3.zetariano.semantica.TablaSimbolosZ;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoPrograma;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.*;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoAtributoZ;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Genera código de tres direcciones (cuartetas) a partir del AST de Zetariano.
 *
 * Zetariano tiene una clase con:
 *   - Atributos.
 *   - Constructores.
 *   - Métodos.
 *
 * Se genera:
 *   - La definición de la clase (como marcadores).
 *   - El cuerpo de cada constructor.
 *   - El cuerpo de cada método.
 */
public class GeneradorC3DZ {

    private final List<Cuarteta> cuarteta = new ArrayList<>();
    private final TablaTemporales temporales = new TablaTemporales();
    private final TablaEtiquetas etiquetas = new TablaEtiquetas();
    private final TablaSimbolosZ tabla;

    // pila de contexto para romper/continuar
    private final Deque<String> pilaBreak = new ArrayDeque<>();
    private final Deque<String> pilaContinue = new ArrayDeque<>();

    // nombre de la clase actual (para `this` implícito)
    private String claseActual;

    // tipo de retorno del método actual (para validar return)
    private String tipoRetornoActual;

    public GeneradorC3DZ(TablaSimbolosZ tabla) {
        this.tabla = tabla;
    }

    // PUNTO DE ENTRADA
    public List<Cuarteta> generar(NodoPrograma programa) {
        cuarteta.clear();
        temporales.reiniciar();
        etiquetas.reiniciar();
        pilaBreak.clear();
        pilaContinue.clear();

        NodoClase clase = programa.clase();
        claseActual = clase.nombre();

        // 1. Definición de la clase (con atributos)
        generarClase(clase);

        // 2. Constructores
        for (NodoConstructor c : clase.constructores()) {
            generarConstructor(c);
        }

        // 3. Métodos
        for (NodoMetodo m : clase.metodos()) {
            generarMetodo(m);
        }

        return cuarteta;
    }

    // CLASE
    private void generarClase(NodoClase clase) {
        emitir("class", clase.nombre(), "-", "-");
        for (NodoAtributoZ a : clase.atributos()) {
            String tipo = a.tipo();
            emitir("campo", tipo + " " + a.nombre(), "-", "-");
        }
        emitir("end-class", "-", "-", "-");
    }


    // CONSTRUCTORES
    private void generarConstructor(NodoConstructor c) {
        String params = c.parametros().isEmpty() ? "void" : descripcionParams(c.parametros());
        emitir("constructor", c.nombre() + "(" + params + ")", "-", "-");

        tipoRetornoActual = null; // los constructores no retornan

        for (NodoSentencia s : c.cuerpo()) {
            generarSentencia(s);
        }

        emitir("return", "-", "-", "-");
        emitir("end-constructor", c.nombre(), "-", "-");
    }

    // METODOS
    private void generarMetodo(NodoMetodo m) {
        String params = m.parametros().isEmpty() ? "void" : descripcionParams(m.parametros());
        String retorno = m.tipoRetorno() != null ? m.tipoRetorno() : "void";
        emitir("method", m.nombre() + "(" + params + ")", retorno, "-");

        tipoRetornoActual = m.tipoRetorno();

        for (NodoSentencia s : m.cuerpo()) {
            generarSentencia(s);
        }

        if (m.tipoRetorno() == null) {
            emitir("return", "-", "-", "-");
        }

        emitir("end-method", m.nombre(), "-", "-");
    }

    private String descripcionParams(List<NodoParametroZ> parametros) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parametros.size(); i++) {
            if (i > 0) sb.append(", ");
            NodoParametroZ p = parametros.get(i);
            sb.append(p.tipo()).append(" ").append(p.nombre());
        }
        return sb.toString();
    }

    // SENTENCIAS
    private void generarSentencia(NodoSentencia s) {
        switch (s.tipoNodo()) {
            case DECLARACION_VARIABLE -> generarDeclaracionVariable((NodoSentencia.DeclaracionVariable) s);
            case ASIGNACION -> generarAsignacion((NodoSentencia.Asignacion) s);
            case EXPRESION_COMO_SENTENCIA -> generarExpresionComoSentencia((NodoSentencia.ExpresionComoSentencia) s);
            case CONDICIONAL -> generarCondicional((NodoSentencia.Condicional) s);
            case SWITCH -> generarSwitch((NodoSentencia.Switch) s);
            case CICLO_PARA -> generarCicloPara((NodoSentencia.CicloPara) s);
            case CICLO_MIENTRAS -> generarCicloMientras((NodoSentencia.CicloMientras) s);
            case CICLO_HACER_MIENTRAS -> generarCicloHacerMientras((NodoSentencia.CicloHacerMientras) s);
            case RETORNO -> generarRetorno((NodoSentencia.Retorno) s);
            case IMPRIMIR -> generarImprimir((NodoSentencia.Imprimir) s);
            case LEER -> generarLeer((NodoSentencia.Leer) s);
            case ROMPER -> emitir("goto", "-", "-", pilaBreak.peek());
            case CONTINUAR -> emitir("goto", "-", "-", pilaContinue.peek());
            case CASO_SWITCH, CASO_DEFAULT -> { /* se manejan desde generarSwitch */ }
        }
    }

    // DECLARACIONES
    private void generarDeclaracionVariable(NodoSentencia.DeclaracionVariable d) {
        if (d.inicializacion() == null) {
            emitir("=", "0", "-", d.nombre());
            return;
        }

        // caso especial: 'tipo x = leer()'
        if (esLlamadaALeer(d.inicializacion())) {
            emitirLecturaTipada(d.tipo(), d.nombre());
            return;
        }

        // caso especial: inicialización de arreglo 'new int[5]' o '{1,2,3}'
        if (d.inicializacion() instanceof NodoExpr.ArregloNuevo an) {
            generarArregloNuevo(an, d.nombre());
            return;
        }

        if (d.inicializacion() instanceof NodoExpr.ListaLiteral ll) {
            for (int i = 0; i < ll.elementos().size(); i++) {
                String valor = generarExpresion(ll.elementos().get(i));
                emitir("=", valor, "-", d.nombre() + "[" + i + "]");
            }
            return;
        }

        String valor = generarExpresion(d.inicializacion());
        emitir("=", valor, "-", d.nombre());
    }

    private void generarArregloNuevo(NodoExpr.ArregloNuevo an, String nombre) {
        int dims = an.dimensiones().size();
        StringBuilder tam = new StringBuilder();
        for (int i = 0; i < dims; i++) {
            if (i > 0) tam.append("x");
            NodoExpr dim = an.dimensiones().get(i);
            tam.append(dim != null ? generarExpresion(dim) : "0");
        }
        emitir("decl-arreglo", an.tipoBase(), nombre, tam.toString());
    }

    // ASIGNACIONES
    private void generarAsignacion(NodoSentencia.Asignacion a) {
        String operador = a.operador();

        if ("=".equals(operador)) {
            String valor = generarExpresion(a.valor());
            String destino = generarReferencia(a.destino());
            emitir("=", valor, "-", destino);
            return;
        }

        // Asignación compuesta: +=, -=, *=
        String opBinario = switch (operador) {
            case "+=" -> "+";
            case "-=" -> "-";
            case "*=" -> "*";
            default -> "+";
        };

        String destino = generarReferencia(a.destino());
        String valor = generarExpresion(a.valor());
        String t = temporales.nuevo();
        emitir(opBinario, destino, valor, t);
        emitir("=", t, "-", destino);
    }

    private void generarExpresionComoSentencia(NodoSentencia.ExpresionComoSentencia e) {
        // Llamada a método, ++, --, etc.
        NodoExpr expr = e.expresion();

        if (expr instanceof NodoExpr.IncrementoDecremento inc) {
            generarIncrementoDecremento(inc);
            return;
        }

        if (expr instanceof NodoExpr.LlamadaMetodo lm) {
            generarLlamadaMetodo(lm);
            return;
        }

        if (expr instanceof NodoExpr.LlamadaFuncion lf) {
            generarExpresion(lf);
            return;
        }

        // Cualquier otra expresión
        generarExpresion(expr);
    }

    private void generarIncrementoDecremento(NodoExpr.IncrementoDecremento inc) {
        String operando = generarReferencia(inc.operando());
        String op = inc.operador();
        if ("++".equals(op)) {
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

        if (c.cuerpoSino() != null) {
            for (NodoSentencia s : c.cuerpoSino()) generarSentencia(s);
        }

        emitir("label", "-", "-", L_fin);
    }

    // SWITCH
    private void generarSwitch(NodoSentencia.Switch sw) {
        String expr = generarExpresion(sw.expresion());
        String L_fin = etiquetas.nueva();

        List<String> etiquetasCasos = new ArrayList<>();
        for (int i = 0; i < sw.casos().size(); i++) {
            etiquetasCasos.add(etiquetas.nueva());
        }
        String L_default = sw.casoDefault() != null ? etiquetas.nueva() : L_fin;

        for (int i = 0; i < sw.casos().size(); i++) {
            NodoSentencia.CasoSwitch caso = sw.casos().get(i);
            String valorCaso = generarExpresion(caso.valor());
            String t = temporales.nuevo();
            emitir("==", expr, valorCaso, t);
            emitir("if_true", t, "-", etiquetasCasos.get(i));
        }
        emitir("goto", "-", "-", L_default);

        pilaBreak.push(L_fin);

        for (int i = 0; i < sw.casos().size(); i++) {
            emitir("label", "-", "-", etiquetasCasos.get(i));
            for (NodoSentencia s : sw.casos().get(i).cuerpo()) generarSentencia(s);
            emitir("goto", "-", "-", L_fin);
        }

        if (sw.casoDefault() != null) {
            emitir("label", "-", "-", L_default);
            for (NodoSentencia s : sw.casoDefault().cuerpo()) generarSentencia(s);
        }

        pilaBreak.pop();

        emitir("label", "-", "-", L_fin);
    }

    // CICLOS
    private void generarCicloPara(NodoSentencia.CicloPara c) {
        String L_inicio = etiquetas.nueva();
        String L_cuerpo = etiquetas.nueva();
        String L_actualizacion = etiquetas.nueva();
        String L_fin = etiquetas.nueva();

        // Inicialización (puede ser DeclaracionVariable o ExpresionComoSentencia)
        if (c.inicializacion() != null) generarSentencia(c.inicializacion());

        emitir("label", "-", "-", L_inicio);

        if (c.condicion() != null) {
            generarCondicion(c.condicion(), L_cuerpo, L_fin);
        }

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

    private void generarCicloMientras(NodoSentencia.CicloMientras c) {
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

    private void generarCicloHacerMientras(NodoSentencia.CicloHacerMientras c) {
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

    // RETORNO
    private void generarRetorno(NodoSentencia.Retorno r) {
        if (r.valor() != null) {
            String valor = generarExpresion(r.valor());
            emitir("return", valor, "-", "-");
        } else {
            emitir("return", "-", "-", "-");
        }
    }

    // IMPRIMIR / LEER
    private void generarImprimir(NodoSentencia.Imprimir i) {
        String valor = generarExpresion(i.expresion());
        emitir("print", valor, "-", "-");
    }

    private void generarLeer(NodoSentencia.Leer l) {
        emitir("read", "-", "-", "-");
    }

    private boolean esLlamadaALeer(NodoExpr expr) {
        return false; // Zetariano no tiene 'leer' como función
    }

    private void emitirLecturaTipada(String tipo, String destino) {
        // No aplica en Zetariano
    }

    // EXPRESIONES
    private String generarExpresion(NodoExpr expr) {
        if (expr == null) return "-";

        switch (expr.tipoNodo()) {
            case LITERAL_ENTERO -> { return String.valueOf(((NodoExpr.LiteralEntero) expr).valor()); }
            case LITERAL_DECIMAL -> { return String.valueOf(((NodoExpr.LiteralDecimal) expr).valor()); }
            case LITERAL_CADENA -> { return "\"" + ((NodoExpr.LiteralCadena) expr).valor() + "\""; }
            case LITERAL_CARACTER -> { return "'" + ((NodoExpr.LiteralCaracter) expr).valor() + "'"; }
            case LITERAL_BOOL -> { return ((NodoExpr.LiteralBool) expr).valor() ? "1" : "0"; }
            case LITERAL_NULO -> { return "null"; }
            case LISTA_LITERAL -> { return "?"; } // solo se usa en contextos específicos

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

            case UNARIA -> {
                NodoExpr.Unaria u = (NodoExpr.Unaria) expr;
                String op = generarExpresion(u.operando());
                String t = temporales.nuevo();
                emitir(u.operador(), op, "-", t);
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

            case TERNARIA -> {
                NodoExpr.Ternaria t = (NodoExpr.Ternaria) expr;
                String L_verdadero = etiquetas.nueva();
                String L_falso = etiquetas.nueva();
                String L_fin = etiquetas.nueva();
                String resultado = temporales.nuevo();

                generarCondicion(t.condicion(), L_verdadero, L_falso);

                emitir("label", "-", "-", L_verdadero);
                String vVerdadero = generarExpresion(t.siVerdadero());
                emitir("=", vVerdadero, "-", resultado);
                emitir("goto", "-", "-", L_fin);

                emitir("label", "-", "-", L_falso);
                String vFalso = generarExpresion(t.siFalso());
                emitir("=", vFalso, "-", resultado);

                emitir("label", "-", "-", L_fin);
                return resultado;
            }

            case LLAMADA_FUNCION -> {
                NodoExpr.LlamadaFuncion l = (NodoExpr.LlamadaFuncion) expr;
                for (NodoExpr arg : l.argumentos()) {
                    String val = generarExpresion(arg);
                    emitir("param", val, "-", "-");
                }
                String t = temporales.nuevo();
                emitir("call", "this." + l.nombre(), String.valueOf(l.argumentos().size()), t);
                return t;
            }

            case LLAMADA_METODO -> {
                NodoExpr.LlamadaMetodo lm = (NodoExpr.LlamadaMetodo) expr;
                return generarLlamadaMetodo(lm);
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

            case ARREGLO_NUEVO -> {
                NodoExpr.ArregloNuevo an = (NodoExpr.ArregloNuevo) expr;
                StringBuilder tam = new StringBuilder();
                for (int i = 0; i < an.dimensiones().size(); i++) {
                    if (i > 0) tam.append("x");
                    NodoExpr dim = an.dimensiones().get(i);
                    tam.append(dim != null ? generarExpresion(dim) : "0");
                }
                String t = temporales.nuevo();
                emitir("new-array", an.tipoBase(), tam.toString(), t);
                return t;
            }
        }

        return "?";
    }

    private String generarLlamadaMetodo(NodoExpr.LlamadaMetodo lm) {
        String obj = generarExpresion(lm.objeto());
        for (NodoExpr arg : lm.argumentos()) {
            String val = generarExpresion(arg);
            emitir("param", val, "-", "-");
        }
        String t = temporales.nuevo();
        emitir("call", obj + "." + lm.nombre(), String.valueOf(lm.argumentos().size()), t);
        return t;
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
        if (expr instanceof NodoExpr.Unaria u && "!".equals(u.operador())) {
            generarCondicion(u.operando(), Lfalse, Ltrue);
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
        if (expr instanceof NodoExpr.LiteralCadena) return true;

        if (expr instanceof NodoExpr.Identificador id) {
            return tabla.buscarVariable(id.nombre())
                    .map(v -> "String".equals(v.tipo()))
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