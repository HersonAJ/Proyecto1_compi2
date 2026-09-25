package com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorZ;

import com.example.contacto_3xtrat3r3str3.c3d.CuartetaV1;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoAtributoZ;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoClase;

import java.util.List;

/**
 * Genera cuartetas informativas para la clase de Zetariano.
 *
 * Formato:
 *   (class, NombreClase, -, -)
 *   (campo, tipo nombre, -, -)
 *   ...
 *   (end-class, -, -, -)
 */
public class GeneradorClaseZ {

    private final List<CuartetaV1> cuartetas;

    public GeneradorClaseZ(List<CuartetaV1> cuartetas) {
        this.cuartetas = cuartetas;
    }

    public void generar(NodoClase clase) {
        emitir("class", clase.nombre(), "-", "-");

        for (NodoAtributoZ a : clase.atributos()) {
            emitir("campo", a.tipo() + " " + a.nombre(), "-", "-");
        }

        emitir("end-class", "-", "-", "-");
    }

    private void emitir(String op, String a1, String a2, String res) {
        cuartetas.add(new CuartetaV1(op, a1, a2, res));
    }
}
