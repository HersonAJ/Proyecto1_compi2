package com.example.contacto_3xtrat3r3str3.c3d_v2.cuartetas.genericas;

/**
 * if (a op b) goto Lx;
 * Se usa para saltar a una etiqueta cuando la comparación es verdadera.
 * Se complementa con Salto para el caso falso.
 */
public class Condicional1 extends Cuarteta {

    private final AccesoMemoria izquierda;
    private final String operador;
    private final AccesoMemoria derecha;
    private final int etiqueta;

    public Condicional1(AccesoMemoria izquierda, String operador,
                        AccesoMemoria derecha, int etiqueta) {
        this.izquierda = izquierda;
        this.operador = operador;
        this.derecha = derecha;
        this.etiqueta = etiqueta;
    }

    public AccesoMemoria getIzquierda() { return izquierda; }
    public String getOperador()         { return operador; }
    public AccesoMemoria getDerecha()   { return derecha; }
    public int getEtiqueta()            { return etiqueta; }

    @Override
    public void aCodigoC(StringBuilder sb) {
        sb.append("    if (");
        izquierda.aCodigoC(sb);
        sb.append(' ').append(operador).append(' ');
        derecha.aCodigoC(sb);
        sb.append(") goto L").append(etiqueta).append(";\n");
    }
}