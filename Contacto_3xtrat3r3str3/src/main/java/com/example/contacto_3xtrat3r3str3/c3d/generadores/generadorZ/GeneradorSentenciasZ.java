package com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorZ;

import com.example.contacto_3xtrat3r3str3.c3d.Cuarteta;
import com.example.contacto_3xtrat3r3str3.c3d.TablaEtiquetas;
import com.example.contacto_3xtrat3r3str3.c3d.TablaOffsets;
import com.example.contacto_3xtrat3r3str3.c3d.TablaTemporales;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoExpr;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoSentencia;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Genera cuartetas para las sentencias de Zetariano.
 */
public class GeneradorSentenciasZ {

    private final List<Cuarteta> cuartetas;
    private final TablaTemporales temporales;
    private final TablaEtiquetas etiquetas;
    private final TablaOffsets offsets;
    private final GeneradorExpresionesZ expresiones;

    private final Deque<String> pilaBreak = new ArrayDeque<>();
    private final Deque<String> pilaContinue = new ArrayDeque<>();

    public GeneradorSentenciasZ(List<Cuarteta> cuartetas,
                                TablaTemporales temporales,
                                TablaEtiquetas etiquetas,
                                TablaOffsets offsets,
                                GeneradorExpresionesZ expresiones) {
        this.cuartetas = cuartetas;
        this.temporales = temporales;
        this.etiquetas = etiquetas;
        this.offsets = offsets;
        this.expresiones = expresiones;
    }

    public void generar(NodoSentencia s) {
        if (s == null) return;

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
            case ROMPER -> generarRomper((NodoSentencia.Romper) s);
            case CONTINUAR -> generarContinuar((NodoSentencia.Continuar) s);
            case CASO_SWITCH, CASO_DEFAULT -> { /* se manejan desde generarSwitch */ }
        }
    }

    // DECLARACIONES
    private void generarDeclaracionVariable(NodoSentencia.DeclaracionVariable d) {
        int offset = offsets.registrar(d.nombre());

        String tDir = temporales.nuevo();
        emitir("+", "BP", String.valueOf(offset), tDir);

        String valor = "0";
        if (d.inicializacion() != null) {
            if (d.inicializacion() instanceof NodoExpr.ListaLiteral lista) {
                // Inicializar cada elemento del arreglo
                for (int i = 0; i < lista.elementos().size(); i++) {
                    String v = expresiones.generarValor(lista.elementos().get(i));
                    String tDirElem = temporales.nuevo();
                    emitir("+", "BP", String.valueOf(offset + i), tDirElem);
                    emitir("=", v, "-", "stack[" + tDirElem + "]");
                }
                return;
            }
            valor = expresiones.generarValor(d.inicializacion());
        }

        emitir("=", valor, "-", "stack[" + tDir + "]");
    }

    // ASIGNACIONES
    private void generarAsignacion(NodoSentencia.Asignacion a) {
        String operador = a.operador();
        String destino = expresiones.generarReferencia(a.destino());

        if ("=".equals(operador)) {
            String valor = expresiones.generarValor(a.valor());
            asignarAReferencia(a.destino(), valor);
            return;
        }

        // Asignación compuesta: +=, -=, *=
        String opBinario = switch (operador) {
            case "+=" -> "+";
            case "-=" -> "-";
            case "*=" -> "*";
            default -> "+";
        };

        String valor = expresiones.generarValor(a.valor());
        String t = temporales.nuevo();
        emitir(opBinario, destino, valor, t);
        asignarAReferencia(a.destino(), t);
    }

    private void asignarAReferencia(NodoExpr destino, String valor) {
        if (destino instanceof NodoExpr.Identificador id) {
            int offset = offsets.obtener(id.nombre());
            if (offset < 0) return;
            String tDir = temporales.nuevo();
            emitir("+", "BP", String.valueOf(offset), tDir);
            emitir("=", valor, "-", "stack[" + tDir + "]");
            return;
        }

        if (destino instanceof NodoExpr.AccesoArray acc) {
            if (acc.arreglo() instanceof NodoExpr.Identificador id) {
                int offset = offsets.obtener(id.nombre());
                if (offset < 0) return;
                String indice = expresiones.generarValor(acc.indice());
                String tBase = temporales.nuevo();
                emitir("+", "BP", String.valueOf(offset), tBase);
                String tDir = temporales.nuevo();
                emitir("+", tBase, indice, tDir);
                emitir("=", valor, "-", "stack[" + tDir + "]");
            }
            return;
        }

        if (destino instanceof NodoExpr.AccesoAtributo acc) {
            String obj = expresiones.generarValor(acc.objeto());
            emitir("=", valor, "-", obj + "." + acc.atributo());
        }
    }

    // EXPRESION COMO SENTENCIA
    private void generarExpresionComoSentencia(NodoSentencia.ExpresionComoSentencia e) {
        expresiones.generarValor(e.expresion());
    }

    // CONDICIONAL
    private void generarCondicional(NodoSentencia.Condicional c) {
        String L_si = etiquetas.nueva();
        String L_siguiente = etiquetas.nueva();
        String L_fin = etiquetas.nueva();

        expresiones.generarCondicion(c.condicion(), L_si, L_siguiente);

        emitir("label", "-", "-", L_si);
        for (NodoSentencia s : c.cuerpoSi()) generar(s);
        emitir("goto", "-", "-", L_fin);

        emitir("label", "-", "-", L_siguiente);

        if (c.cuerpoSino() != null) {
            for (NodoSentencia s : c.cuerpoSino()) generar(s);
        }

        emitir("label", "-", "-", L_fin);
    }


    // SWITCH
    private void generarSwitch(NodoSentencia.Switch sw) {
        String expr = expresiones.generarValor(sw.expresion());
        String L_fin = etiquetas.nueva();

        java.util.List<String> etiquetasCasos = new java.util.ArrayList<>();
        for (int i = 0; i < sw.casos().size(); i++) {
            etiquetasCasos.add(etiquetas.nueva());
        }
        String L_default = sw.casoDefault() != null ? etiquetas.nueva() : L_fin;

        for (int i = 0; i < sw.casos().size(); i++) {
            NodoSentencia.CasoSwitch caso = sw.casos().get(i);
            String valorCaso = expresiones.generarValor(caso.valor());
            String t = temporales.nuevo();
            emitir("==", expr, valorCaso, t);
            emitir("if", t, "-", etiquetasCasos.get(i));
        }
        emitir("goto", "-", "-", L_default);

        pilaBreak.push(L_fin);

        for (int i = 0; i < sw.casos().size(); i++) {
            emitir("label", "-", "-", etiquetasCasos.get(i));
            for (NodoSentencia s : sw.casos().get(i).cuerpo()) generar(s);
            emitir("goto", "-", "-", L_fin);
        }

        if (sw.casoDefault() != null) {
            emitir("label", "-", "-", L_default);
            for (NodoSentencia s : sw.casoDefault().cuerpo()) generar(s);
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

        if (c.inicializacion() != null) generar(c.inicializacion());

        emitir("label", "-", "-", L_inicio);

        if (c.condicion() != null) {
            expresiones.generarCondicion(c.condicion(), L_cuerpo, L_fin);
        }
        emitir("label", "-", "-", L_cuerpo);

        pilaBreak.push(L_fin);
        pilaContinue.push(L_actualizacion);

        for (NodoSentencia s : c.cuerpo()) generar(s);

        pilaBreak.pop();
        pilaContinue.pop();

        emitir("label", "-", "-", L_actualizacion);
        if (c.actualizacion() != null) generar(c.actualizacion());

        emitir("goto", "-", "-", L_inicio);
        emitir("label", "-", "-", L_fin);
    }

    private void generarCicloMientras(NodoSentencia.CicloMientras c) {
        String L_inicio = etiquetas.nueva();
        String L_cuerpo = etiquetas.nueva();
        String L_fin = etiquetas.nueva();

        emitir("label", "-", "-", L_inicio);
        expresiones.generarCondicion(c.condicion(), L_cuerpo, L_fin);
        emitir("label", "-", "-", L_cuerpo);

        pilaBreak.push(L_fin);
        pilaContinue.push(L_inicio);

        for (NodoSentencia s : c.cuerpo()) generar(s);

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

        for (NodoSentencia s : c.cuerpo()) generar(s);

        pilaBreak.pop();
        pilaContinue.pop();

        emitir("label", "-", "-", L_condicion);
        expresiones.generarCondicion(c.condicion(), L_inicio, L_fin);
        emitir("label", "-", "-", L_fin);
    }

    // RETORNO / IMPRIMIR / LEER
    private void generarRetorno(NodoSentencia.Retorno r) {
        if (r.valor() != null) {
            String valor = expresiones.generarValor(r.valor());
            emitir("return", valor, "-", "-");
        } else {
            emitir("return", "-", "-", "-");
        }
    }

    private void generarImprimir(NodoSentencia.Imprimir i) {
        String valor = expresiones.generarValor(i.expresion());
        emitir(i.saltoDeLinea() ? "println" : "print", valor, "-", "-");
    }

    private void generarLeer(NodoSentencia.Leer l) {
        emitir("read", "-", "-", "-");
    }

    // ROMPER / CONTINUAR
    private void generarRomper(NodoSentencia.Romper r) {
        if (!pilaBreak.isEmpty()) {
            emitir("goto", "-", "-", pilaBreak.peek());
        }
    }

    private void generarContinuar(NodoSentencia.Continuar c) {
        if (!pilaContinue.isEmpty()) {
            emitir("goto", "-", "-", pilaContinue.peek());
        }
    }

    private void emitir(String op, String a1, String a2, String res) {
        cuartetas.add(new Cuarteta(op, a1, a2, res));
    }
}