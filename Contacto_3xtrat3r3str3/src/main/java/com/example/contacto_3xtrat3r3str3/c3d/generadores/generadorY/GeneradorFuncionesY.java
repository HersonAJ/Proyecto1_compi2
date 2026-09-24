package com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorY;

import com.example.contacto_3xtrat3r3str3.c3d.Cuarteta;
import com.example.contacto_3xtrat3r3str3.c3d.TablaEtiquetas;
import com.example.contacto_3xtrat3r3str3.c3d.TablaOffsets;
import com.example.contacto_3xtrat3r3str3.c3d.TablaTemporales;
import com.example.contacto_3xtrat3r3str3.y.ast.*;
import java.util.List;

/**
 * Genera cuartetas para las funciones de Y?.
 *
 * Modelo del video: las funciones son BLOQUES con etiquetas y saltos.
 * El BP y SP se manejan manualmente.
 *
 * Formato:
 *   (func, nombre, numParams, -)       ← inicio de la función
 *   ... cuerpo ...
 *   (return, valor, -, -)              ← retorno
 *   (end-func, nombre, -, -)           ← fin de la función
 */
public class GeneradorFuncionesY {

    private final List<Cuarteta> cuartetas;
    private final TablaTemporales temporales;
    private final TablaEtiquetas etiquetas;
    private final TablaOffsets offsets;
    private final GeneradorSentenciasY sentencias;

    public GeneradorFuncionesY(List<Cuarteta> cuartetas,
                               TablaTemporales temporales,
                               TablaEtiquetas etiquetas,
                               TablaOffsets offsets,
                               GeneradorSentenciasY sentencias) {
        this.cuartetas = cuartetas;
        this.temporales = temporales;
        this.etiquetas = etiquetas;
        this.offsets = offsets;
        this.sentencias = sentencias;
    }

    // GENERACION DE UNA FUNCION
    public void generar(NodoFuncion.Funcion funcion) {
        String nombre = funcion.nombre();
        List<NodoParametro> parametros = funcion.parametros();

        // Etiquetas de la función
        String L_funcion = "L_" + nombre;
        String L_retorno = "L_" + nombre + "_retorno";

        // Encabezado
        emitir("func", L_funcion, String.valueOf(parametros.size()), nombre);

        // Ajustar BP al inicio del frame
        emitir("=", "SP", "-", "BP");

        // Registrar parámetros en la tabla de offsets
        for (NodoParametro p : parametros) {
            NodoParametro.Parametro param = (NodoParametro.Parametro) p;
            offsets.registrar(param.nombre());
        }

        // Cuerpo
        for (NodoSentencia s : funcion.cuerpo()) {
            sentencias.generar(s);
        }

        // Retorno implícito si es void
        if (funcion.tipoRetorno() == null) {
            emitir("goto", "-", "-", L_retorno);
        }

        // Etiqueta de retorno
        emitir("label", "-", "-", L_retorno);

        // Restaurar SP al BP
        emitir("=", "BP", "-", "SP");

        // Fin de la función
        emitir("end-func", L_funcion, "-", "-");
    }

    // HELPER
    private void emitir(String op, String a1, String a2, String res) {
        cuartetas.add(new Cuarteta(op, a1, a2, res));
    }
}