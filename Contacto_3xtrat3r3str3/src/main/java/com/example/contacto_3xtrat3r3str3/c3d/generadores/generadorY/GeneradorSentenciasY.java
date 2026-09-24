package com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorY;

import com.example.contacto_3xtrat3r3str3.c3d.Cuarteta;
import com.example.contacto_3xtrat3r3str3.c3d.TablaEtiquetas;
import com.example.contacto_3xtrat3r3str3.c3d.TablaOffsets;
import com.example.contacto_3xtrat3r3str3.c3d.TablaTemporales;
import com.example.contacto_3xtrat3r3str3.y.ast.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Genera cuartetas para las sentencias de Y?.
 *
 * Maneja:
 *   - Declaraciones de variables, arreglos, matrices, estructuras.
 *   - Asignaciones.
 *   - Incremento/decremento.
 *   - Condicionales (si/sino/contrario).
 *   - Elegir (switch).
 *   - Ciclos (para, mientras, hacer-mientras).
 *   - Retorno, imprimir, leer, romper, continuar.
 */
public class GeneradorSentenciasY {

    private final List<Cuarteta> cuartetas;
    private final TablaTemporales temporales;
    private final TablaEtiquetas etiquetas;
    private final TablaOffsets offsets;
    private final GeneradorExpresionesY expresiones;

    // Pilas de contexto para romper/continuar
    private final Deque<String> pilaBreak = new ArrayDeque<>();
    private final Deque<String> pilaContinue = new ArrayDeque<>();

    public GeneradorSentenciasY(List<Cuarteta> cuartetas,
                                TablaTemporales temporales,
                                TablaEtiquetas etiquetas,
                                TablaOffsets offsets,
                                GeneradorExpresionesY expresiones) {
        this.cuartetas = cuartetas;
        this.temporales = temporales;
        this.etiquetas = etiquetas;
        this.offsets = offsets;
        this.expresiones = expresiones;
    }

    // GENERACION DE SENTENCIAS
    public void generar(NodoSentencia s) {
        if (s == null) return;

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
            case ROMPER -> generarRomper((NodoSentencia.Romper) s);
            case CONTINUAR -> generarContinuar((NodoSentencia.Continuar) s);
        }
    }

    // DECLARACIONES
    private void generarDeclaracionVariable(NodoSentencia.DeclaracionVariable d) {
        int offset = offsets.registrar(d.nombre());

        // Dirección de la variable
        String tDir = temporales.nuevo();
        emitir("+", "BP", String.valueOf(offset), tDir);

        // Valor inicial
        String valor;
        if (d.inicializacion() != null) {
            valor = expresiones.generarValor(d.inicializacion());
        } else {
            valor = "0"; // valor por defecto (evitar basura)
        }

        // Guardar en el stack
        emitir("=", valor, "-", "stack[" + tDir + "]");
    }

    private void generarDeclaracionArreglo(NodoSentencia.DeclaracionArreglo d) {
        int offsetBase = offsets.registrar(d.nombre());

        // Inicializar elementos si hay
        if (d.inicializacion() != null && !d.inicializacion().isEmpty()) {
            for (int i = 0; i < d.inicializacion().size(); i++) {
                String valor = expresiones.generarValor(d.inicializacion().get(i));

                // Dirección del elemento i: BP + offsetBase + i
                String tDir = temporales.nuevo();
                emitir("+", "BP", String.valueOf(offsetBase + i), tDir);

                emitir("=", valor, "-", "stack[" + tDir + "]");
            }
        } else {
            // Inicializar con 0
            for (int i = 0; i < d.tamano(); i++) {
                String tDir = temporales.nuevo();
                emitir("+", "BP", String.valueOf(offsetBase + i), tDir);
                emitir("=", "0", "-", "stack[" + tDir + "]");
            }
        }
    }

    private void generarDeclaracionMatriz(NodoSentencia.DeclaracionMatriz d) {
        int offsetBase = offsets.registrar(d.nombre());
        int total = d.filas() * d.columnas();

        // Inicializar todas las posiciones con 0
        for (int i = 0; i < total; i++) {
            String tDir = temporales.nuevo();
            emitir("+", "BP", String.valueOf(offsetBase + i), tDir);
            emitir("=", "0", "-", "stack[" + tDir + "]");
        }
    }

    private void generarDeclaracionEstructura(NodoSentencia.DeclaracionEstructura d) {
        int offsetBase = offsets.registrar(d.nombre());

        if (d.inicializacion() != null && !d.inicializacion().isEmpty()) {
            for (int i = 0; i < d.inicializacion().size(); i++) {
                String valor = expresiones.generarValor(d.inicializacion().get(i));

                String tDir = temporales.nuevo();
                emitir("+", "BP", String.valueOf(offsetBase + i), tDir);

                emitir("=", valor, "-", "stack[" + tDir + "]");
            }
        }
    }

    // ASIGNACIONES
    private void generarAsignacion(NodoSentencia.Asignacion a) {
        String valor = expresiones.generarValor(a.valor());
        asignarAReferencia(a.destino(), valor);
    }

    //Asigna un valor a una referencia (variable, arreglo, atributo)
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

                // Dirección base
                String tBase = temporales.nuevo();
                emitir("+", "BP", String.valueOf(offset), tBase);

                // Dirección del elemento
                String tDir = temporales.nuevo();
                emitir("+", tBase, indice, tDir);

                emitir("=", valor, "-", "stack[" + tDir + "]");
            }
            return;
        }

        if (destino instanceof NodoExpr.AccesoAtributo acc) {
            // Simplificación: asignar a "obj.atributo" como nombre compuesto
            // (en un modelo completo, se calcularía el offset del campo)
            String obj = expresiones.generarValor(acc.objeto());
            emitir("=", valor, "-", obj + "." + acc.atributo());
        }
    }

    private void generarIncrementoDecremento(NodoSentencia.IncrementoDecremento inc) {
        // ++x → x = x + 1
        // --x → x = x - 1
        String nombre = inc.nombre();
        int offset = offsets.obtener(nombre);
        if (offset < 0) return;

        // Leer el valor actual
        String tDir = temporales.nuevo();
        emitir("+", "BP", String.valueOf(offset), tDir);

        String tVal = temporales.nuevo();
        emitir("=", "stack[" + tDir + "]", "-", tVal);

        // Calcular nuevo valor
        String tResultado = temporales.nuevo();
        String op = "++".equals(inc.operador()) ? "+" : "-";
        emitir(op, tVal, "1", tResultado);

        // Guardar
        emitir("=", tResultado, "-", "stack[" + tDir + "]");
    }

    // CONDICIONAL
    private void generarCondicional(NodoSentencia.Condicional c) {
        String L_si = etiquetas.nueva();
        String L_siguiente = etiquetas.nueva();
        String L_fin = etiquetas.nueva();

        // Evaluar condición principal
        expresiones.generarCondicion(c.condicion(), L_si, L_siguiente);

        // Cuerpo del si
        emitir("label", "-", "-", L_si);
        for (NodoSentencia s : c.cuerpoSi()) generar(s);
        emitir("goto", "-", "-", L_fin);

        // Siguiente: sino o contrario
        emitir("label", "-", "-", L_siguiente);

        if (c.cuerpoSino() != null) {
            String L_sino = etiquetas.nueva();
            String L_siguiente2 = etiquetas.nueva();

            expresiones.generarCondicion(c.condicionSino(), L_sino, L_siguiente2);

            emitir("label", "-", "-", L_sino);
            for (NodoSentencia s : c.cuerpoSino()) generar(s);
            emitir("goto", "-", "-", L_fin);

            emitir("label", "-", "-", L_siguiente2);
        }

        if (c.cuerpoContrario() != null) {
            for (NodoSentencia s : c.cuerpoContrario()) generar(s);
        }

        emitir("label", "-", "-", L_fin);
    }

    // ELEGIR (switch)
    private void generarElegir(NodoSentencia.Elegir e) {
        String expr = expresiones.generarValor(e.expresion());
        String L_fin = etiquetas.nueva();

        // Etiquetas para cada caso
        java.util.List<String> etiquetasCasos = new java.util.ArrayList<>();
        for (int i = 0; i < e.casos().size(); i++) {
            etiquetasCasos.add(etiquetas.nueva());
        }
        String L_default = e.siempre() != null ? etiquetas.nueva() : L_fin;

        // Comparar con cada caso
        for (int i = 0; i < e.casos().size(); i++) {
            NodoSentencia.CasoElegir caso = e.casos().get(i);
            String valorCaso = expresiones.generarValor(caso.valor());
            String t = temporales.nuevo();
            emitir("==", expr, valorCaso, t);
            emitir("if", t, "-", etiquetasCasos.get(i));
        }
        emitir("goto", "-", "-", L_default);

        // Cuerpo de cada caso
        pilaBreak.push(L_fin);

        for (int i = 0; i < e.casos().size(); i++) {
            emitir("label", "-", "-", etiquetasCasos.get(i));
            for (NodoSentencia s : e.casos().get(i).cuerpo()) generar(s);
            emitir("goto", "-", "-", L_fin);
        }

        if (e.siempre() != null) {
            emitir("label", "-", "-", L_default);
            for (NodoSentencia s : e.siempre().cuerpo()) generar(s);
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

        // Inicialización
        int offsetVar = offsets.registrar(c.nombreVariable());
        String valorInicial = expresiones.generarValor(c.valorInicial());

        String tDir = temporales.nuevo();
        emitir("+", "BP", String.valueOf(offsetVar), tDir);
        emitir("=", valorInicial, "-", "stack[" + tDir + "]");

        // Inicio del ciclo
        emitir("label", "-", "-", L_inicio);

        // Condición
        expresiones.generarCondicion(c.condicion(), L_cuerpo, L_fin);
        emitir("label", "-", "-", L_cuerpo);

        // Cuerpo
        pilaBreak.push(L_fin);
        pilaContinue.push(L_actualizacion);

        for (NodoSentencia s : c.cuerpo()) generar(s);

        pilaBreak.pop();
        pilaContinue.pop();

        // Actualización
        emitir("label", "-", "-", L_actualizacion);

        String tDir2 = temporales.nuevo();
        emitir("+", "BP", String.valueOf(offsetVar), tDir2);

        String tVal = temporales.nuevo();
        emitir("=", "stack[" + tDir2 + "]", "-", tVal);

        String tResultado = temporales.nuevo();
        String op = "++".equals(c.operadorActualizacion()) ? "+" : "-";
        emitir(op, tVal, "1", tResultado);

        emitir("=", tResultado, "-", "stack[" + tDir2 + "]");

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
        emitir("print", valor, "-", "-");
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

    // HELPER
    private void emitir(String op, String a1, String a2, String res) {
        cuartetas.add(new Cuarteta(op, a1, a2, res));
    }
}