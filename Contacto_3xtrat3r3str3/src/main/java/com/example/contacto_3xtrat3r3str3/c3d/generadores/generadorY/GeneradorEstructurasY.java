package com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorY;


import com.example.contacto_3xtrat3r3str3.c3d.Cuarteta;
import com.example.contacto_3xtrat3r3str3.y.ast.NodoAtributo;
import com.example.contacto_3xtrat3r3str3.y.ast.NodoEstructura;

import java.util.List;

/**
 * Genera cuartetas informativas para las estructuras de Y?.
 *
 * Las estructuras NO generan código ejecutable. Solo se emiten
 * como marcadores para que el GeneradorC pueda crear los structs de C.
 *
 * Formato:
 *   (struct, NombreEstructura, -, -)
 *   (campo, tipo nombre[tam], -, -)
 *   ...
 *   (end-struct, -, -, -)
 */
public class GeneradorEstructurasY {

    private final List<Cuarteta> cuartetas;

    public GeneradorEstructurasY(List<Cuarteta> cuartetas) {
        this.cuartetas = cuartetas;
    }

    /**
     * Genera las cuartetas de una estructura.
     */
    public void generar(NodoEstructura.Estructura estructura) {
        // Inicio de la estructura
        emitir("struct", estructura.nombre(), "-", "-");

        // Atributos
        for (NodoAtributo a : estructura.atributos()) {
            NodoAtributo.Atributo at = (NodoAtributo.Atributo) a;

            // Determinar el tipo: primitivo o estructura anidada
            String tipo = at.tipoPrimitivo() != null
                    ? at.tipoPrimitivo()
                    : at.tipoEstructura();

            // Determinar si es arreglo
            String tam = at.tamanoArreglo() > 0
                    ? "[" + at.tamanoArreglo() + "]"
                    : "";

            emitir("campo", tipo + " " + at.nombre() + tam, "-", "-");
        }

        // Fin de la estructura
        emitir("end-struct", "-", "-", "-");
    }

    private void emitir(String op, String a1, String a2, String res) {
        cuartetas.add(new Cuarteta(op, a1, a2, res));
    }
}
