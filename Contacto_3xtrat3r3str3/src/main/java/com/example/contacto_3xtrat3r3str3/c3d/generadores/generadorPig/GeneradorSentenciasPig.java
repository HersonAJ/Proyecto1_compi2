package com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorPig;

import com.example.contacto_3xtrat3r3str3.c3d.CuartetaV1;
import com.example.contacto_3xtrat3r3str3.c3d.TablaEtiquetas;
import com.example.contacto_3xtrat3r3str3.c3d.TablaOffsets;
import com.example.contacto_3xtrat3r3str3.c3d.TablaTemporales;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoExpr;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoSentencia;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

// Genera cuartetas para las sentencias de PigLatin
public class GeneradorSentenciasPig {

    private final List<CuartetaV1> cuartetas;
    private final TablaTemporales temporales;
    private final TablaEtiquetas etiquetas;
    private final TablaOffsets offsets;
    private final GeneradorExpresionesPig expresiones;

    private final Deque<String> pilaBreak = new ArrayDeque<>();
    private final Deque<String> pilaContinue = new ArrayDeque<>();

    public GeneradorSentenciasPig(List<CuartetaV1> cuartetas,
                                  TablaTemporales temporales,
                                  TablaEtiquetas etiquetas,
                                  TablaOffsets offsets,
                                  GeneradorExpresionesPig expresiones) {
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
        int offset = offsets.registrar(d.nombre());
        String tDir = temporales.nuevo();
        emitir("+", "BP", String.valueOf(offset), tDir);

        String valor = "0";
        if (d.inicializacion() != null) {
            valor = expresiones.generarValor(d.inicializacion());
        }
        emitir("=", valor, "-", "stack[" + tDir + "]");
    }

    private void generarDeclaracionArreglo(NodoSentencia.DeclaracionArreglo d) {
        int offsetBase = offsets.registrar(d.nombre());

        if (d.inicializacion() != null && !d.inicializacion().isEmpty()) {
            for (int i = 0; i < d.inicializacion().size(); i++) {
                String valor = expresiones.generarValor(d.inicializacion().get(i));
                String tDir = temporales.nuevo();
                emitir("+", "BP", String.valueOf(offsetBase + i), tDir);
                emitir("=", valor, "-", "stack[" + tDir + "]");
            }
        } else {
            for (int i = 0; i < d.tamano(); i++) {
                String tDir = temporales.nuevo();
                emitir("+", "BP", String.valueOf(offsetBase + i), tDir);
                emitir("=", "0", "-", "stack[" + tDir + "]");
            }
        }
    }

    private void generarDeclaracionStruct(NodoSentencia.DeclaracionStruct d) {
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

    private void generarIncrementoDecremento(NodoSentencia.IncrementoDecremento inc) {
        String operando = expresiones.generarReferencia(inc.operando());
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

        expresiones.generarCondicion(c.condicion(), L_si, L_siguiente);

        emitir("label", "-", "-", L_si);
        for (NodoSentencia s : c.cuerpoSi()) generar(s);
        emitir("goto", "-", "-", L_fin);

        emitir("label", "-", "-", L_siguiente);

        // Ramas aliter con condición
        for (NodoSentencia.RamaAliter r : c.ramasAliter()) {
            String L_aliterSi = etiquetas.nueva();
            String L_aliterSig = etiquetas.nueva();
            expresiones.generarCondicion(r.condicion(), L_aliterSi, L_aliterSig);

            emitir("label", "-", "-", L_aliterSi);
            for (NodoSentencia s : r.cuerpo()) generar(s);
            emitir("goto", "-", "-", L_fin);

            emitir("label", "-", "-", L_aliterSig);
        }

        // Rama aliter final
        if (c.cuerpoAliter() != null) {
            for (NodoSentencia s : c.cuerpoAliter()) generar(s);
        }

        emitir("label", "-", "-", L_fin);
    }

    // CICLOS
    private void generarCicloDum(NodoSentencia.CicloDum c) {
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

    private void generarCicloFacere(NodoSentencia.CicloFacere c) {
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

    private void generarCicloPer(NodoSentencia.CicloPer c) {
        String L_inicio = etiquetas.nueva();
        String L_cuerpo = etiquetas.nueva();
        String L_actualizacion = etiquetas.nueva();
        String L_fin = etiquetas.nueva();

        if (c.inicializacion() != null) generar(c.inicializacion());

        emitir("label", "-", "-", L_inicio);
        expresiones.generarCondicion(c.condicion(), L_cuerpo, L_fin);
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
            String valor = expresiones.generarValor(v);
            emitir("print", valor, "-", "-");
        }
    }

    private void generarInterrupcion(NodoSentencia.InterrupcionCiclo i) {
        if ("perge".equals(i.tipo())) {
            if (!pilaContinue.isEmpty()) {
                emitir("goto", "-", "-", pilaContinue.peek());
            }
        } else {
            if (!pilaBreak.isEmpty()) {
                emitir("goto", "-", "-", pilaBreak.peek());
            }
        }
    }

    // LLAMADAS COMO SENTENCIA
    private void generarLlamadaFuncionSentencia(NodoSentencia.LlamadaFuncionSentencia l) {
        expresiones.generarValor(l.llamada());
    }

    private void generarLlamadaMetodoSentencia(NodoSentencia.LlamadaMetodoSentencia l) {
        expresiones.generarValor(l.llamada());
    }

    private void emitir(String op, String a1, String a2, String res) {
        cuartetas.add(new CuartetaV1(op, a1, a2, res));
    }
}