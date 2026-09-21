package com.example.contacto_3xtrat3r3str3.piglatin.semantica;

//declara variables y arreglos en la tabla de simbolos
//detecta: declaracion duplicada variable
//declaracion duplicada arreglo
//declaracion duplicada estructura
//declaracion duplicada objeto

import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoExpr;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoSentencia;
import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;

import java.util.List;

public class ValidadorDeclaracionesPig {

    private final TablaSimbolosPig tabla;
    private final List<ErrorSemantico> errores;

    public ValidadorDeclaracionesPig(TablaSimbolosPig tabla, List<ErrorSemantico> errores) {
        this.tabla = tabla;
        this.errores = errores;
    }

    //variables
    public void declararVariable(NodoSentencia.DeclaracionVariable d) {
        boolean ok = tabla.declararVariable(d.nombre(), d.tipo(), 0, null, false, false, d.tipo());
        if (!ok) {
            reportarDuplicada(d.linea(), d.columna(), d.nombre());
        }
    }

    //arreglos
    public void declararArreglo(NodoSentencia.DeclaracionArreglo d) {
        boolean esEstructura = tabla.buscarEstructura(d.tipo()).isPresent();
        boolean esObjeto = tabla.buscarClase(d.tipo()).isPresent();

        boolean ok = tabla.declararVariable(
                d.nombre(), d.tipo(), 1, d.tamano(),
                esEstructura, esObjeto, d.tipo()
        );

        if (!ok) {
            reportarDuplicada(d.linea(), d.columna(), d.nombre());
        }
    }

    //estructura
    public void declararStruct(NodoSentencia.DeclaracionStruct d) {
        boolean ok = tabla.declararVariable(d.nombre(), d.tipo(), 0, null, true, false, d.tipo());
        if (!ok) {
            reportarDuplicada(d.linea(), d.columna(), d.nombre());
        }
    }

    //variable con novus (objeto)
    public void declararObjeto(NodoSentencia.DeclaracionVariable d) {
        String tipo = d.tipo();

        // Si el tipo es null, intentar extraerlo del inicializador.
        if (tipo == null && d.inicializacion() instanceof NodoExpr.InstanciaObjeto inst) {
            tipo = inst.tipoClase();
        }

        boolean ok = tabla.declararVariable(
                d.nombre(), tipo, 0, null, false, true, tipo
        );

        if (!ok) {
            reportarDuplicada(d.linea(), d.columna(), d.nombre());
        }
    }

    private void reportarDuplicada(int linea, int columna, String nombre) {
        errores.add(new ErrorSemantico(linea, columna, "Declaracion duplicada",
                "'" + nombre + "' ya fue declarado en este ambito"));
    }
}
