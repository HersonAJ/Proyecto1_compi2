package com.example.contacto_3xtrat3r3str3.c3d.generadores;


import com.example.contacto_3xtrat3r3str3.c3d.Cuarteta;
import com.example.contacto_3xtrat3r3str3.c3d.TablaEtiquetas;
import com.example.contacto_3xtrat3r3str3.c3d.TablaTemporales;
import com.example.contacto_3xtrat3r3str3.y.ast.*;

import java.util.ArrayList;
import java.util.List;

//genera codigo de tres direcciones (cuartetas) a partir del AST de Y
//Y no tiene un main entonces solo se genera Definiciones de estructura y cuerpo de funciones
public class GeneradorC3DY {

    private final List<Cuarteta> cuarteta = new ArrayList<>();
    private final TablaTemporales temporales = new TablaTemporales();
    private final TablaEtiquetas etiquetas = new TablaEtiquetas();

    //genera cuartetas para el programa de Y
    public List<Cuarteta> generar(NodoPrograma.Programa programa) {
        cuarteta.clear();
        temporales.reiniciar();
        etiquetas.reiniciar();

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
        for(NodoAtributo a : e.atributos()) {
            NodoAtributo.Atributo at = (NodoAtributo.Atributo) a;
            String tipo  = at.tipoPrimitivo() != null ? at.tipoPrimitivo() : at.tipoEstructura();
            String tam = at.tamanoArreglo() > 0 ? "[" + at.tamanoArreglo() + "]" : "";
            emitir("campo", tipo + " " + at.nombre() + tam, "-", "-");
        }
        emitir("end-strcut", "-", "-", "-");
    }

    // FUNCIONES
    private void generarFuncion(NodoFuncion.Funcion f) {
        // Encabezado de la función
        String params = f.parametros().isEmpty() ? "void" : descripcionParams(f.parametros());
        String retorno = f.tipoRetorno() != null ? f.tipoRetorno() : "void";
        emitir("func", f.nombre() + "(" + params + ")", retorno, "-");

        // Cuerpo
        for (NodoSentencia s : f.cuerpo()) {
            generarSentencia(s);
        }

        // Retorno implícito si es void
        if (f.tipoRetorno() == null) {
            emitir("return", "-", "-", "-");
        }

        emitir("end-func", f.nombre(), "-", "-");
    }

    private String descripcionParams(java.util.List<NodoParametro> parametros) {
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
            case ROMPER -> emitir("goto", "-", "-", "BREAK_PENDIENTE");
            case CONTINUAR -> emitir("goto", "-", "-", "CONTINUE_PENDIENTE");
        }
    }

    // DECLARACIONES
    private void generarDeclaracionVariable(NodoSentencia.DeclaracionVariable d) {
        if (d.inicializacion() != null) {
            String valor = generarExpresion(d.inicializacion());
            emitir("=", valor, "-", d.nombre());
        } else {
            emitir("=", "0", "-", d.nombre());
        }
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
        if (d.inicializacion() != null && !d.inicializacion().isEmpty()) {
            for (int i = 0; i < d.inicializacion().size(); i++) {
                String valor = generarExpresion(d.inicializacion().get(i));
                emitir("=", valor, "-", d.nombre() + "." + i);
            }
        }
    }

    // ASIGNACIONES
    private void generarAsignacion(NodoSentencia.Asignacion a) {
        String valor = generarExpresion(a.valor());
        String destino = generarReferencia(a.destino());
        emitir("=", valor, "-", destino);
    }

    private void generarIncrementoDecremento(NodoSentencia.IncrementoDecremento inc) {
        // Busca el nodo como sentencia; el operando puede ser Identificador o AccesoArray/Atributo.
        // Por simplicidad, asumimos identificador.
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
        String L_siguiente = etiquetas.nueva();
        String L_fin = etiquetas.nueva();

        // Condición principal
        String cond = generarExpresion(c.condicion());
        emitir("if_false", cond, "-", L_siguiente);

        // Cuerpo del si
        for (NodoSentencia s : c.cuerpoSi()) generarSentencia(s);
        emitir("goto", "-", "-", L_fin);

        // Etiqueta del siguiente (sino o contrario)
        emitir("label", "-", "-", L_siguiente);

        // Sino (opcional)
        if (c.cuerpoSino() != null) {
            String L_siguiente2 = etiquetas.nueva();
            String condSino = generarExpresion(c.condicionSino());
            emitir("if_false", condSino, "-", L_siguiente2);

            for (NodoSentencia s : c.cuerpoSino()) generarSentencia(s);
            emitir("goto", "-", "-", L_fin);

            emitir("label", "-", "-", L_siguiente2);
        }

        // Contrario (opcional)
        if (c.cuerpoContrario() != null) {
            for (NodoSentencia s : c.cuerpoContrario()) generarSentencia(s);
        }

        emitir("label", "-", "-", L_fin);
    }

    // ELEGIR
    private void generarElegir(NodoSentencia.Elegir e) {
        String expr = generarExpresion(e.expresion());
        String L_fin = etiquetas.nueva();

        java.util.List<String> etiquetasCasos = new ArrayList<>();
        for (int i = 0; i < e.casos().size(); i++) {
            etiquetasCasos.add(etiquetas.nueva());
        }
        String L_default = e.siempre() != null ? etiquetas.nueva() : L_fin;

        // Comparar con cada caso
        for (int i = 0; i < e.casos().size(); i++) {
            NodoSentencia.CasoElegir caso = e.casos().get(i);
            String valorCaso = generarExpresion(caso.valor());
            String t = temporales.nuevo();
            emitir("==", expr, valorCaso, t);
            emitir("if_true", t, "-", etiquetasCasos.get(i));
        }
        emitir("goto", "-", "-", L_default);

        // Cuerpo de cada caso
        for (int i = 0; i < e.casos().size(); i++) {
            emitir("label", "-", "-", etiquetasCasos.get(i));
            for (NodoSentencia s : e.casos().get(i).cuerpo()) generarSentencia(s);
            emitir("goto", "-", "-", L_fin);
        }

        // Default
        if (e.siempre() != null) {
            emitir("label", "-", "-", L_default);
            for (NodoSentencia s : e.siempre().cuerpo()) generarSentencia(s);
        }

        emitir("label", "-", "-", L_fin);
    }

    // CICLOS
    private void generarCicloPara(NodoSentencia.CicloPara c) {
        String L_inicio = etiquetas.nueva();
        String L_fin = etiquetas.nueva();

        // Inicialización
        emitir("=", generarExpresion(c.valorInicial()), "-", c.nombreVariable());

        // Etiqueta de inicio
        emitir("label", "-", "-", L_inicio);

        // Condición
        String cond = generarExpresion(c.condicion());
        emitir("if_false", cond, "-", L_fin);

        // Cuerpo
        for (NodoSentencia s : c.cuerpo()) generarSentencia(s);

        // Actualización
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
        String L_fin = etiquetas.nueva();

        emitir("label", "-", "-", L_inicio);
        String cond = generarExpresion(c.condicion());
        emitir("if_false", cond, "-", L_fin);

        for (NodoSentencia s : c.cuerpo()) generarSentencia(s);

        emitir("goto", "-", "-", L_inicio);
        emitir("label", "-", "-", L_fin);
    }

    private void generarCicloHacerMientras(NodoSentencia.CicloHacerMientras c) {
        String L_inicio = etiquetas.nueva();

        emitir("label", "-", "-", L_inicio);

        for (NodoSentencia s : c.cuerpo()) generarSentencia(s);

        String cond = generarExpresion(c.condicion());
        emitir("if_true", cond, "-", L_inicio);
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
        // 'leer()' sin asignación. La variante 'x = leer()' se maneja como expresión.
        emitir("read", "-", "-", "-");
    }

    // EXPRESIONES
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
                emitir(b.operador(), izq, der, t);
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
                // Emitir parámetros
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
        // Para asignaciones, el destino es una referencia.
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
    private void emitir(String op, String a1, String a2, String res) {
        cuarteta.add(new Cuarteta(op, a1, a2, res));
    }
}