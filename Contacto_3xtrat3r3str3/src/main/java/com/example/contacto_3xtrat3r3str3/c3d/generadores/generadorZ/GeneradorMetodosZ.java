package com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorZ;

import com.example.contacto_3xtrat3r3str3.c3d.Cuarteta;
import com.example.contacto_3xtrat3r3str3.c3d.TablaEtiquetas;
import com.example.contacto_3xtrat3r3str3.c3d.TablaOffsets;
import com.example.contacto_3xtrat3r3str3.c3d.TablaTemporales;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoConstructor;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoMetodo;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoParametroZ;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoSentencia;

import java.util.List;

/**
 * Genera cuartetas para los constructores y métodos de Zetariano.
 *
 * Los constructores y métodos son bloques con etiquetas y saltos.
 * Los atributos de la clase se acceden con 'this.atributo'.
 */
public class GeneradorMetodosZ {

    private final List<Cuarteta> cuartetas;
    private final TablaTemporales temporales;
    private final TablaEtiquetas etiquetas;
    private final TablaOffsets offsets;
    private final GeneradorSentenciasZ sentencias;

    private String nombreClase;

    public GeneradorMetodosZ(List<Cuarteta> cuartetas,
                             TablaTemporales temporales,
                             TablaEtiquetas etiquetas,
                             TablaOffsets offsets,
                             GeneradorSentenciasZ sentencias) {
        this.cuartetas = cuartetas;
        this.temporales = temporales;
        this.etiquetas = etiquetas;
        this.offsets = offsets;
        this.sentencias = sentencias;
    }

    public void setNombreClase(String nombre) {
        this.nombreClase = nombre;
    }

    // CONSTRUCTORES
    public void generarConstructor(NodoConstructor c) {
        String etiqueta = "L_" + nombreClase + "_constructor";

        // Encabezado
        emitir("constructor", etiqueta, String.valueOf(c.parametros().size()), c.nombre());

        // Ajustar BP al inicio del frame
        emitir("=", "SP", "-", "BP");

        // Registrar parámetros en la tabla de offsets
        for (NodoParametroZ p : c.parametros()) {
            offsets.registrar(p.nombre());
        }

        // Cuerpo
        for (NodoSentencia s : c.cuerpo()) {
            sentencias.generar(s);
        }

        // Retorno implícito
        emitir("goto", "-", "-", etiqueta + "_retorno");

        emitir("label", "-", "-", etiqueta + "_retorno");
        emitir("=", "BP", "-", "SP");
        emitir("end-constructor", etiqueta, "-", "-");
    }

    // METODOS
    public void generarMetodo(NodoMetodo m) {
        String etiqueta = "L_" + nombreClase + "_" + m.nombre();

        // Encabezado
        emitir("method", etiqueta, String.valueOf(m.parametros().size()), m.nombre());

        // Ajustar BP
        emitir("=", "SP", "-", "BP");

        // Registrar parámetros
        for (NodoParametroZ p : m.parametros()) {
            offsets.registrar(p.nombre());
        }

        // Cuerpo
        for (NodoSentencia s : m.cuerpo()) {
            sentencias.generar(s);
        }

        // Retorno implícito si es void
        if (m.tipoRetorno() == null) {
            emitir("goto", "-", "-", etiqueta + "_retorno");
        }

        emitir("label", "-", "-", etiqueta + "_retorno");
        emitir("=", "BP", "-", "SP");
        emitir("end-method", etiqueta, "-", "-");
    }

    private void emitir(String op, String a1, String a2, String res) {
        cuartetas.add(new Cuarteta(op, a1, a2, res));
    }
}