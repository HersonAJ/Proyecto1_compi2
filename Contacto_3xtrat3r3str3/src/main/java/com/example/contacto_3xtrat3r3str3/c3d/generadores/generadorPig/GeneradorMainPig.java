package com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorPig;

import com.example.contacto_3xtrat3r3str3.c3d.CuartetaV1;
import com.example.contacto_3xtrat3r3str3.c3d.TablaEtiquetas;
import com.example.contacto_3xtrat3r3str3.c3d.TablaOffsets;
import com.example.contacto_3xtrat3r3str3.c3d.TablaTemporales;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoSentencia;

import java.util.List;

/**
 * Genera cuartetas para el cuerpo del MAIOR> de PigLatin.
 *
 * El MAIOR es el punto de entrada del programa. Se genera como
 * un bloque principal con etiquetas.
 *
 * Formato:
 *   (main, -, -, -)              ← inicio del main
 *   ... variables globales ...
 *   ... cuerpo del MAIOR ...
 *   (end-main, -, -, -)          ← fin del main
 */
public class GeneradorMainPig {

    private final List<CuartetaV1> cuartetas;
    private final TablaTemporales temporales;
    private final TablaEtiquetas etiquetas;
    private final TablaOffsets offsets;
    private final GeneradorSentenciasPig sentencias;

    public GeneradorMainPig(List<CuartetaV1> cuartetas,
                            TablaTemporales temporales,
                            TablaEtiquetas etiquetas,
                            TablaOffsets offsets,
                            GeneradorSentenciasPig sentencias) {
        this.cuartetas = cuartetas;
        this.temporales = temporales;
        this.etiquetas = etiquetas;
        this.offsets = offsets;
        this.sentencias = sentencias;
    }

    public void generar(List<NodoSentencia> variablesGlobales, List<NodoSentencia> cuerpoMain) {
        emitir("main", "L_Pig_main", "-", "-");

        // Ajustar BP al inicio del frame
        emitir("=", "SP", "-", "BP");

        // Variables globales
        for (NodoSentencia s : variablesGlobales) {
            sentencias.generar(s);
        }

        // Cuerpo del MAIOR
        for (NodoSentencia s : cuerpoMain) {
            sentencias.generar(s);
        }

        // Fin del main
        emitir("end-main", "-", "-", "-");
    }

    private void emitir(String op, String a1, String a2, String res) {
        cuartetas.add(new CuartetaV1(op, a1, a2, res));
    }
}