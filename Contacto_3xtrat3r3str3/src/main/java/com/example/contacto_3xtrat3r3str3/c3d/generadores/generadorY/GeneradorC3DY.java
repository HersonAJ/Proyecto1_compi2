package com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorY;

import com.example.contacto_3xtrat3r3str3.c3d.CuartetaV1;
import com.example.contacto_3xtrat3r3str3.c3d.TablaEtiquetas;
import com.example.contacto_3xtrat3r3str3.c3d.TablaOffsets;
import com.example.contacto_3xtrat3r3str3.c3d.TablaTemporales;
import com.example.contacto_3xtrat3r3str3.y.ast.NodoEstructura;
import com.example.contacto_3xtrat3r3str3.y.ast.NodoFuncion;
import com.example.contacto_3xtrat3r3str3.y.ast.NodoPrograma;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquesta la generación de cuartetas para Y?.
 *
 * Coordina:
 *   - GeneradorEstructurasY (definiciones de estructuras).
 *   - GeneradorFuncionesY (cuerpos de funciones).
 *   - GeneradorSentenciasY (sentencias dentro de las funciones).
 *   - GeneradorExpresionesY (expresiones dentro de las sentencias).
 */
public class GeneradorC3DY {

    private final List<CuartetaV1> cuartetas = new ArrayList<>();
    private final TablaTemporales temporales = new TablaTemporales();
    private final TablaEtiquetas etiquetas = new TablaEtiquetas();
    private final TablaOffsets offsets = new TablaOffsets();

    private final GeneradorEstructurasY genEstructuras;
    private final GeneradorExpresionesY genExpresiones;
    private final GeneradorSentenciasY genSentencias;
    private final GeneradorFuncionesY genFunciones;

    public GeneradorC3DY() {
        // Inicializar los generadores especializados
        this.genEstructuras = new GeneradorEstructurasY(cuartetas);
        this.genExpresiones = new GeneradorExpresionesY(cuartetas, temporales, etiquetas, offsets);
        this.genSentencias = new GeneradorSentenciasY(cuartetas, temporales, etiquetas, offsets, genExpresiones);
        this.genFunciones = new GeneradorFuncionesY(cuartetas, temporales, etiquetas, offsets, genSentencias);
    }

    // PUNTO DE ENTRADA
    public List<CuartetaV1> generar(NodoPrograma.Programa programa) {
        cuartetas.clear();
        temporales.reiniciar();
        etiquetas.reiniciar();
        etiquetas.setPrefijo("Y_");   // ← AGREGAR
        offsets.reiniciar();

        // 1. Estructuras
        for (NodoEstructura e : programa.estructuras()) {
            genEstructuras.generar((NodoEstructura.Estructura) e);
        }

        // 2. Funciones
        for (NodoFuncion f : programa.funciones()) {
            offsets.reiniciar();
            genFunciones.generar((NodoFuncion.Funcion) f);
        }

        return cuartetas;
    }
}