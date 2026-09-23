package com.example.contacto_3xtrat3r3str3.c3d.generadores;

import com.example.contacto_3xtrat3r3str3.c3d.Cuarteta;
import com.example.contacto_3xtrat3r3str3.c3d.TablaEtiquetas;
import com.example.contacto_3xtrat3r3str3.c3d.TablaTemporales;
import com.example.contacto_3xtrat3r3str3.y.ast.*;
import com.example.contacto_3xtrat3r3str3.y.semantica.TablaSimbolos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//genera codigo de tres direcciones (cuartetas) a partir del AST de Y
//Y no tiene un main entonces solo se genera Definiciones de estructura y cuerpo de funciones
//
//IMPORTANTE: requiere una TablaSimbolos ya poblada (resultado de correr ValidadorSemantico
//sobre el mismo programa antes de generar) -- se usa para: resolver el nombre real de los
//campos al inicializar una estructura, conocer el tamano de la segunda dimension de una
//matriz (para aplanar el indice), e inferir tipos aproximados (cadena vs numerico) para
//decidir cuando '+' significa concatenacion.
public class GeneradorC3DY {

    private final List<Cuarteta> cuarteta = new ArrayList<>();
    private final TablaTemporales temporales = new TablaTemporales();
    private final TablaEtiquetas etiquetas = new TablaEtiquetas();
    private final TablaSimbolos tabla;

    //pilas de contexto para romper/continuar -- el tope es a donde debe saltar cada uno
    //en el punto exacto del recorrido donde se encuentran
    private final Deque<String> pilaBreak = new ArrayDeque<>();
    private final Deque<String> pilaContinue = new ArrayDeque<>();

    public GeneradorC3DY(TablaSimbolos tabla) {
        this.tabla = tabla;
    }

    //genera cuartetas para el programa de Y
    public List<Cuarteta> generar(NodoPrograma.Programa programa) {
        cuarteta.clear();
        temporales.reiniciar();
        etiquetas.reiniciar();
        pilaBreak.clear();
        pilaContinue.clear();

        //1 estructuras (se omiten comentarios )
        for (NodoEstructura e : programa.estructuras()) {
            generarEstructura((NodoEstructura.Estructura) e);
        }

        //2 funciones
        for (NodoFuncion f : programa.funciones()) {
            generarFuncion((NodoFuncion.Funcion) f);
        }

        return cuarteta;
    }

    private void generarEstructura(NodoEstructura.Estructura e) {
        emitir("struct", e.nombre(), "-", "-");
        for (NodoAtributo a : e.atributos()) {
            NodoAtributo.Atributo at = (NodoAtributo.Atributo) a;
            String tipo = at.tipoPrimitivo() != null ? at.tipoPrimitivo() : at.tipoEstructura();
            String tam = at.tamanoArreglo() > 0 ? "[" + at.tamanoArreglo() + "]" : "";
            emitir("campo", tipo + " " + at.nombre() + tam, "-", "-");
        }
        emitir("end-strcut", "-", "-", "-");
    }

    // FUNCIONES
    private void generarFuncion(NodoFuncion.Funcion f) {
        String params = f.parametros().isEmpty() ? "void" : descripcionParams(f.parametros());
        String retorno = f.tipoRetorno() != null ? f.tipoRetorno() : "void";
        emitir("func", f.nombre() + "(" + params + ")", retorno, "-");

        for (NodoSentencia s : f.cuerpo()) {
            generarSentencia(s);
        }

        if (f.tipoRetorno() == null) {
            emitir("return", "-", "-", "-");
        }

        emitir("end-func", f.nombre(), "-", "-");
    }

    private String descripcionParams(List<NodoParametro> parametros) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parametros.size(); i++) {
            if (i > 0) sb.append(", ");
            NodoParametro.Parametro p = (NodoParametro.Parametro) parametros.get(i);
            String tipo = p.tipoPrimitivo() != null ? p.tipoPrimitivo() : p.tipoEstructura();
            if (p.esArreglo()) tipo = "[]" + tipo;
            if (p.esEstructura()) tipo = "{}" + tipo;
            sb.append(tipo).append(" ").append(p.nombre());
        }
        return sb.toString();
    }

    // SENTENCIAS
    private void generarSentencia(NodoSentencia s) {
        switch (s.tipoNodo()) {
            case DECLARACION_VARIABLE -> generarDeclaracionVariable((NodoSentencia.DeclaracionVariable) s);
            case DECLARACION_ARREGLO -> generarDeclaracionArreglo((NodoSentencia.DeclaracionArreglo) s);
            case DECLARACION_MATRIZ -> generarDeclaracionMatriz((NodoSentencia.DeclaracionMatriz) s);
            case DECLARACION_ESTRUCTURA -> generarDeclaracionEstructura((NodoSentencia.DeclaracionEstructura) s);
            case ASIGNACION -> generarAsignacion((NodoSentencia.Asignacion) s);
            case INCREMENTO_DECREMENTO -> generarIncrementoDecremento((NodoSentencia.IncrementoDecremento) s);
            case CONDICIONAL -> generarCondicional((NodoSentencia.Condicional) s);
            case ELEGIR -> generarElegir((NodoSentencia.Elegir) s);
            case CICLO_PARA -> generarCicloPara((NodoSentencia.CicloPara) s);
            case CICLO_MIENTRAS -> generarCicloMientras((NodoSentencia.CicloMientras) s);
            case CICLO_HACER_MIENTRAS -> generarCicloHacerMientras((NodoSentencia.CicloHacerMientras) s);
            case RETORNO -> generarRetorno((NodoSentencia.Retorno) s);
            case IMPRIMIR -> generarImprimir((NodoSentencia.Imprimir) s);
            case LEER -> generarLeer((NodoSentencia.Leer) s);
            case ROMPER -> emitir("goto", "-", "-", pilaBreak.peek());
            case CONTINUAR -> emitir("goto", "-", "-", pilaContinue.peek());
        }
    }

    // DECLARACIONES
    private void generarDeclaracionVariable(NodoSentencia.DeclaracionVariable d) {
        if (d.inicializacion() == null) {
            emitir("=", "0", "-", d.nombre());
            return;
        }

        //caso especial: 'tipo x = leer()' -> instruccion de lectura tipada, no una llamada normal
        if (esLlamadaALeer(d.inicializacion())) {
            emitirLecturaTipada(d.tipo(), d.nombre());
            return;
        }

        //booleano compuesto (relacional, logico, o negacion) -> patron de cortocircuito con etiquetas
        if ("bool".equals(d.tipo()) && esExpresionBooleanaCompuesta(d.inicializacion())) {
            generarAsignacionBooleana(d.inicializacion(), d.nombre());
            return;
        }

        String valor = generarExpresion(d.inicializacion());
        emitir("=", valor, "-", d.nombre());
    }

    private void generarDeclaracionArreglo(NodoSentencia.DeclaracionArreglo d) {
        if (d.inicializacion() != null && !d.inicializacion().isEmpty()) {
            for (int i = 0; i < d.inicializacion().size(); i++) {
                String valor = generarExpresion(d.inicializacion().get(i));
                emitir("=", valor, "-", d.nombre() + "[" + i + "]");
            }
        }
    }

    private void generarDeclaracionMatriz(NodoSentencia.DeclaracionMatriz d) {
        emitir("decl-matriz", d.tipo(), d.nombre(), d.filas() + "x" + d.columnas());
    }

    private void generarDeclaracionEstructura(NodoSentencia.DeclaracionEstructura d) {
        if (d.inicializacion() == null || d.inicializacion().isEmpty()) return;

        List<String> nombresCampos = nombresDeCamposDe(d.tipoEstructura());

        for (int i = 0; i < d.inicializacion().size(); i++) {
            String valor = generarExpresion(d.inicializacion().get(i));
            String campo = (i < nombresCampos.size()) ? nombresCampos.get(i) : String.valueOf(i);
            emitir("=", valor, "-", d.nombre() + "." + campo);
        }
    }

    private List<String> nombresDeCamposDe(String tipoEstructura) {
        Optional<TablaSimbolos.DefinicionEstructura> def = tabla.buscarEstructura(tipoEstructura);
        if (def.isEmpty()) return List.of();
        return new ArrayList<>(def.get().atributos().keySet());
    }

    // ASIGNACIONES
    private void generarAsignacion(NodoSentencia.Asignacion a) {
        //caso especial: 'x = leer()'
        if (esLlamadaALeer(a.valor())) {
            String tipoDestino = tipoDeclaradoDe(a.destino());
            emitirLecturaTipada(tipoDestino, generarReferencia(a.destino()));
            return;
        }

        //booleano compuesto sobre una variable ya declarada como bool
        String tipoDestino = tipoDeclaradoDe(a.destino());
        if ("bool".equals(tipoDestino) && esExpresionBooleanaCompuesta(a.valor())) {
            generarAsignacionBooleana(a.valor(), generarReferencia(a.destino()));
            return;
        }

        String valor = generarExpresion(a.valor());
        String destino = generarReferencia(a.destino());
        emitir("=", valor, "-", destino);
    }

    private String tipoDeclaradoDe(NodoExpr destino) {
        if (destino instanceof NodoExpr.Identificador id) {
            return tabla.buscarVariable(id.nombre()).map(TablaSimbolos.SimboloVariable::tipo).orElse(null);
        }
        return null;
    }

    private void generarIncrementoDecremento(NodoSentencia.IncrementoDecremento inc) {
        String op = inc.operador();
        String nombre = inc.nombre();
        if (op.equals("++")) {
            emitir("+", nombre, "1", nombre);
        } else {
            emitir("-", nombre, "1", nombre);
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
            String L_sinoSi = etiquetas.nueva();
            String L_siguiente2 = etiquetas.nueva();
            generarCondicion(c.condicionSino(), L_sinoSi, L_siguiente2);

            emitir("label", "-", "-", L_sinoSi);
            for (NodoSentencia s : c.cuerpoSino()) generarSentencia(s);
            emitir("goto", "-", "-", L_fin);

            emitir("label", "-", "-", L_siguiente2);
        }

        if (c.cuerpoContrario() != null) {
            for (NodoSentencia s : c.cuerpoContrario()) generarSentencia(s);
        }

        emitir("label", "-", "-", L_fin);
    }

    // ELEGIR
    private void generarElegir(NodoSentencia.Elegir e) {
        String expr = generarExpresion(e.expresion());
        String L_fin = etiquetas.nueva();

        List<String> etiquetasCasos = new ArrayList<>();
        for (int i = 0; i < e.casos().size(); i++) {
            etiquetasCasos.add(etiquetas.nueva());
        }
        String L_default = e.siempre() != null ? etiquetas.nueva() : L_fin;

        for (int i = 0; i < e.casos().size(); i++) {
            NodoSentencia.CasoElegir caso = e.casos().get(i);
            String valorCaso = generarExpresion(caso.valor());
            String t = temporales.nuevo();
            emitir("==", expr, valorCaso, t);
            emitir("if_true", t, "-", etiquetasCasos.get(i));
        }
        emitir("goto", "-", "-", L_default);

        pilaBreak.push(L_fin);

        for (int i = 0; i < e.casos().size(); i++) {
            emitir("label", "-", "-", etiquetasCasos.get(i));
            for (NodoSentencia s : e.casos().get(i).cuerpo()) generarSentencia(s);
            emitir("goto", "-", "-", L_fin);
        }

        if (e.siempre() != null) {
            emitir("label", "-", "-", L_default);
            for (NodoSentencia s : e.siempre().cuerpo()) generarSentencia(s);
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

        emitir("=", generarExpresion(c.valorInicial()), "-", c.nombreVariable());

        emitir("label", "-", "-", L_inicio);
        generarCondicion(c.condicion(), L_cuerpo, L_fin);
        emitir("label", "-", "-", L_cuerpo);

        //break salta al final; continue salta directo a la actualizacion (no se la debe saltar)
        pilaBreak.push(L_fin);
        pilaContinue.push(L_actualizacion);

        for (NodoSentencia s : c.cuerpo()) generarSentencia(s);

        pilaBreak.pop();
        pilaContinue.pop();

        emitir("label", "-", "-", L_actualizacion);
        if (c.operadorActualizacion().equals("++")) {
            emitir("+", c.nombreVariable(), "1", c.nombreVariable());
        } else {
            emitir("-", c.nombreVariable(), "1", c.nombreVariable());
        }

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

        //continue reevalua la condicion desde el inicio; no hay paso de actualizacion que saltarse
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

        //continue salta a re-evaluar la condicion (el cuerpo ya se ejecuto al menos una vez)
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
        //'leer()' sin asignacion: solo lee y descarta, no hay tipo destino que tipar la instruccion
        emitir("read", "-", "-", "-");
    }

    private boolean esLlamadaALeer(NodoExpr expr) {
        return expr instanceof NodoExpr.LlamadaFuncion ll && "leer".equals(ll.nombre());
    }

    private void emitirLecturaTipada(String tipo, String destino) {
        String opcode = switch (tipo != null ? tipo : "") {
            case "entero" -> "SCAN_INT";
            case "flotante" -> "SCAN_FLOAT";
            case "caracter" -> "SCAN_CHAR";
            case "cadena" -> "SCAN_TEXT";
            default -> "SCAN_TEXT"; //valor por defecto si el tipo no se pudo resolver
        };
        emitir(opcode, destino, "-", "-");
    }

    // EXPRESIONES (calculan un VALOR -- para booleanos compuestos usados como valor,
    private String generarExpresion(NodoExpr expr) {
        switch (expr.tipoNodo()) {
            case LITERAL_ENTERO -> {
                return String.valueOf(((NodoExpr.LiteralEntero) expr).valor());
            }
            case LITERAL_FLOTANTE -> {
                return String.valueOf(((NodoExpr.LiteralFlotante) expr).valor());
            }
            case LITERAL_CADENA -> {
                return "\"" + ((NodoExpr.LiteralCadena) expr).valor() + "\"";
            }
            case LITERAL_CARACTER -> {
                return "'" + ((NodoExpr.LiteralCaracter) expr).valor() + "'";
            }
            case LITERAL_BOOL -> {
                return ((NodoExpr.LiteralBool) expr).valor() ? "1" : "0";
            }
            case IDENTIFICADOR -> {
                return ((NodoExpr.Identificador) expr).nombre();
            }
            case ACCESO_ARRAY -> {
                return resolverAccesoArray((NodoExpr.AccesoArray) expr);
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
                //'+' entre cadenas es concatenacion, no suma aritmetica
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
            default -> {
                return "?";
            }
        }
    }

    private String generarReferencia(NodoExpr expr) {
        if (expr instanceof NodoExpr.Identificador id) return id.nombre();
        if (expr instanceof NodoExpr.AccesoArray a) return resolverAccesoArray(a);
        if (expr instanceof NodoExpr.AccesoAtributo a) {
            String obj = generarExpresion(a.objeto());
            return obj + "." + a.atributo();
        }
        return "?";
    }

    /**
     * Resuelve un acceso a arreglo o matriz. Si el nivel mas externo es 'matriz[i][j]'
     * (AccesoArray anidado sobre un identificador con 2 dimensiones conocidas en la tabla),
     * aplana el indice a una sola dimension: posicion = indiceA * columnas + indiceB.
     */
    private String resolverAccesoArray(NodoExpr.AccesoArray externo) {
        if (externo.arreglo() instanceof NodoExpr.AccesoArray interno
                && interno.arreglo() instanceof NodoExpr.Identificador id) {

            Optional<TablaSimbolos.SimboloVariable> simbolo = tabla.buscarVariable(id.nombre());
            if (simbolo.isPresent() && simbolo.get().tamanos().size() == 2) {
                int columnas = simbolo.get().tamanos().get(1);

                String indiceA = generarExpresion(interno.indice());
                String indiceB = generarExpresion(externo.indice());

                String t1 = temporales.nuevo();
                emitir("*", indiceA, String.valueOf(columnas), t1);
                String t2 = temporales.nuevo();
                emitir("+", t1, indiceB, t2);

                return id.nombre() + "[" + t2 + "]";
            }
        }

        //caso normal: arreglo de una dimension
        String arr = generarExpresion(externo.arreglo());
        String idx = generarExpresion(externo.indice());
        return arr + "[" + idx + "]";
    }

    // EVALUACION DE EXPRESIONES BOOLEANAS
    private boolean esExpresionBooleanaCompuesta(NodoExpr expr) {
        if (expr instanceof NodoExpr.Binaria b) {
            return switch (b.operador()) {
                case "&&", "||", "==", "!=", "<", ">", "<=", ">=" -> true;
                default -> false;
            };
        }
        return expr instanceof NodoExpr.Unaria u && "!".equals(u.operador());
    }

    /**
     * Genera saltos condicionales para 'expr' sin nunca materializarla en un temporal:
     * si es verdadera, el flujo termina saltando a Ltrue; si es falsa, a Lfalse.
     * Sigue exactamente las reglas de NOT/AND/OR de las diapositivas del curso.
     */
    private void generarCondicion(NodoExpr expr, String Ltrue, String Lfalse) {
        if (expr instanceof NodoExpr.Unaria u && "!".equals(u.operador())) {
            //NOT: intercambia las etiquetas, no genera ninguna instruccion propia
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

        //caso base: cualquier otro valor booleano (identificador, literal, llamada a funcion)
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

    /** 'destino = <expr booleana compuesta>' usando el patron x=1/x=0 con etiquetas de salida. */
    private void generarAsignacionBooleana(NodoExpr expr, String destino) {
        String Lverdadero = etiquetas.nueva();
        String Lfalso = etiquetas.nueva();
        String Lsalida = etiquetas.nueva();

        generarCondicion(expr, Lverdadero, Lfalso);

        emitir("label", "-", "-", Lverdadero);
        emitir("=", "1", "-", destino);
        emitir("goto", "-", "-", Lsalida);

        emitir("label", "-", "-", Lfalso);
        emitir("=", "0", "-", destino);

        emitir("label", "-", "-", Lsalida);
    }

    private boolean esTipoCadena(NodoExpr expr) {
        if (expr instanceof NodoExpr.LiteralCadena) return true;

        if (expr instanceof NodoExpr.Identificador id) {
            return tabla.buscarVariable(id.nombre())
                    .map(v -> "cadena".equals(v.tipo()))
                    .orElse(false);
        }

        if (expr instanceof NodoExpr.AccesoAtributo a) {
            return false;
        }

        //una cadena de '+' es concatenacion completa si CUALQUIER operando de la cadena es cadena
        //ej. "hola" + 5 + "mundo" -> todo concatena
        if (expr instanceof NodoExpr.Binaria b && "+".equals(b.operador())) {
            return esTipoCadena(b.izquierda()) || esTipoCadena(b.derecha());
        }

        return false;
    }

    private void emitir(String op, String a1, String a2, String res) {
        cuarteta.add(new Cuarteta(op, a1, a2, res));
    }
}