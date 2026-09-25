package com.example.contacto_3xtrat3r3str3.c3d_v2;


/**
 * goto L_romper;
 * La etiqueta concreta la decide el AST a partir de la pila de ciclos.
 */
public class Romper1 extends Cuarteta {

    private final int etiqueta;

    public Romper1(int etiqueta) {
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