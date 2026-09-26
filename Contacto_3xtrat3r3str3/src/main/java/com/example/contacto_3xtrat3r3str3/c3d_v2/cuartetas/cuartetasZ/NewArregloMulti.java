package com.example.contacto_3xtrat3r3str3.c3d_v2.cuartetas.cuartetasZ;

import com.example.contacto_3xtrat3r3str3.c3d_v2.cuartetas.genericas.AccesoMemoria;
import com.example.contacto_3xtrat3r3str3.c3d_v2.cuartetas.genericas.Cuarteta;

import java.util.List;

/**
 * Reserva de un arreglo multidimensional al estilo Java:
 *   int** m = new int[a][b];
 *
 * Se traduce a:
 *   int** m = malloc(a * sizeof(int*));
 *   for (int i = 0; i < a; i++) {
 *       m[i] = malloc(b * sizeof(int));
 *   }
 *
 * Para 3+ dimensiones, se anidan más bucles.
 * Por ahora soportamos 2D.
 */
public class NewArregloMulti extends Cuarteta {

    private final AccesoMemoria destino;      // el temporal que recibe el arreglo
    private final String tipoElementoC;       // "int", "double", "struct Persona"
    private final List<AccesoMemoria> tamanos;

    public NewArregloMulti(AccesoMemoria destino,
                            String tipoElementoC,
                            List<AccesoMemoria> tamanos) {
        this.destino = destino;
        this.tipoElementoC = tipoElementoC;
        this.tamanos = tamanos;
    }

    @Override
    public void aCodigoC(StringBuilder sb) {
        int n = tamanos.size();
        if (n < 2) {
            throw new IllegalStateException(
                    "NewArregloMulti1 requiere al menos 2 dimensiones");
        }

        // Para 2D:
        //   destino = malloc(tam0 * sizeof(tipoElemento*));
        //   for (int i = 0; i < tam0; i++) {
        //       destino[i] = malloc(tam1 * sizeof(tipoElemento));
        //   }
        //
        // Para 3D+ anidaríamos bucles, pero por ahora solo 2D.

        String tipoPuntero = tipoElementoC + "*";

        sb.append("    ");
        destino.aCodigoC(sb);
        sb.append(" = malloc(");
        tamanos.get(0).aCodigoC(sb);
        sb.append(" * sizeof(").append(tipoPuntero).append("));\n");

        sb.append("    for (int i = 0; i < ");
        tamanos.get(0).aCodigoC(sb);
        sb.append("; i++) {\n");

        sb.append("        ");
        destino.aCodigoC(sb);
        sb.append("[i] = malloc(");
        tamanos.get(1).aCodigoC(sb);
        sb.append(" * sizeof(").append(tipoElementoC).append("));\n");

        sb.append("    }\n");
    }
}