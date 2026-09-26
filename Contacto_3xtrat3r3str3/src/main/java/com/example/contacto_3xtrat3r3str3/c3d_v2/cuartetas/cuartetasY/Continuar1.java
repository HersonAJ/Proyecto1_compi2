package com.example.contacto_3xtrat3r3str3.c3d_v2.cuartetas.cuartetasY;

import com.example.contacto_3xtrat3r3str3.c3d_v2.cuartetas.genericas.Cuarteta;

/**
 * goto L_continuar;
 */
public class Continuar1 extends Cuarteta {

    private final int etiqueta;

    public Continuar1(int etiqueta) {
        this.etiqueta = etiqueta;
    }

    public int getEtiqueta() {
        return etiqueta;
    }

    @Override
    public void aCodigoC(StringBuilder sb) {
        sb.append("    goto L").append(etiqueta).append(";\n");
    }
}
