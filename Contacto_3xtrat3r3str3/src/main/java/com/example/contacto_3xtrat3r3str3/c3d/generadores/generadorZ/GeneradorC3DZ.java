package com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorZ;

import com.example.contacto_3xtrat3r3str3.c3d.Cuarteta;
import com.example.contacto_3xtrat3r3str3.c3d.TablaEtiquetas;
import com.example.contacto_3xtrat3r3str3.c3d.TablaOffsets;
import com.example.contacto_3xtrat3r3str3.c3d.TablaTemporales;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoClase;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoConstructor;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoMetodo;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoPrograma;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquesta la generación de cuartetas para Zetariano.
 */
public class GeneradorC3DZ {

    private final List<Cuarteta> cuartetas = new ArrayList<>();
    private final TablaTemporales temporales = new TablaTemporales();
    private final TablaEtiquetas etiquetas = new TablaEtiquetas();
    private final TablaOffsets offsets = new TablaOffsets();

    private final GeneradorClaseZ genClase;
    private final GeneradorExpresionesZ genExpresiones;
    private final GeneradorSentenciasZ genSentencias;
    private GeneradorMetodosZ genMetodos;

    public GeneradorC3DZ() {
        this.genClase = new GeneradorClaseZ(cuartetas);
        this.genExpresiones = new GeneradorExpresionesZ(cuartetas, temporales, etiquetas, offsets);
        this.genSentencias = new GeneradorSentenciasZ(cuartetas, temporales, etiquetas, offsets, genExpresiones);
    }

    public List<Cuarteta> generar(NodoPrograma programa) {
        cuartetas.clear();
        temporales.reiniciar();
        etiquetas.reiniciar();
        offsets.reiniciar();

        NodoClase clase = programa.clase();

        genMetodos = new GeneradorMetodosZ(
                cuartetas, temporales, etiquetas, offsets, genSentencias, clase.atributos());
        genMetodos.setNombreClase(clase.nombre());

        // 1. Definición de la clase
        genClase.generar(clase);

        // 2. Constructores
        for (NodoConstructor c : clase.constructores()) {
            offsets.reiniciar();
            genMetodos.generarConstructor(c);
        }

        // 3. Métodos
        for (NodoMetodo m : clase.metodos()) {
            offsets.reiniciar();
            genMetodos.generarMetodo(m);
        }

        return cuartetas;
    }
}