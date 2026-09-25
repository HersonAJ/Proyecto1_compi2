package com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorPig;

import com.example.contacto_3xtrat3r3str3.c3d.Cuarteta;
import com.example.contacto_3xtrat3r3str3.c3d.TablaEtiquetas;
import com.example.contacto_3xtrat3r3str3.c3d.TablaOffsets;
import com.example.contacto_3xtrat3r3str3.c3d.TablaTemporales;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoPrograma;

import java.util.ArrayList;
import java.util.List;

//Orquesta la generación de cuartetas para PigLatin
public class GeneradorC3DPig {

    private final List<Cuarteta> cuartetas = new ArrayList<>();
    private final TablaTemporales temporales = new TablaTemporales();
    private final TablaEtiquetas etiquetas = new TablaEtiquetas();
    private final TablaOffsets offsets = new TablaOffsets();

    private final GeneradorExpresionesPig genExpresiones;
    private final GeneradorSentenciasPig genSentencias;
    private final GeneradorMainPig genMain;

    public GeneradorC3DPig() {
        this.genExpresiones = new GeneradorExpresionesPig(cuartetas, temporales, etiquetas, offsets);
        this.genSentencias = new GeneradorSentenciasPig(cuartetas, temporales, etiquetas, offsets, genExpresiones);
        this.genMain = new GeneradorMainPig(cuartetas, temporales, etiquetas, offsets, genSentencias);
    }

    public List<Cuarteta> generar(NodoPrograma programa) {
        cuartetas.clear();
        temporales.reiniciar();
        etiquetas.reiniciar();
        etiquetas.setPrefijo("Pig_");   // ← AGREGAR
        offsets.reiniciar();

        genMain.generar(programa.variablesGlobales(), programa.cuerpoMain());

        return cuartetas;
    }
}