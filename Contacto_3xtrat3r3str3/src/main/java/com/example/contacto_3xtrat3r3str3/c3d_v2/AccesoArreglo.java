package com.example.contacto_3xtrat3r3str3.c3d_v2;

/**
 * Acceso a un arreglo ya aplanado:
 *   a[i]        ->  a[i]
 *   m[i][j]     ->  m[i * COLS + j]
 *
 * Para simplificar, el AST se encarga de aplanar matrices antes de crear
 * este acceso. Aquí solo tratamos arreglos lineales.
 */
public class AccesoArreglo extends AccesoMemoria {

    private final AccesoMemoria base;
    private final AccesoMemoria indice;

    public AccesoArreglo(AccesoMemoria base, AccesoMemoria indice) {
        this.base = base;
        this.indice = indice;
    }

    public AccesoMemoria getBase() {
        return base;
    }

    public AccesoMemoria getIndice() {
        return indice;
    }

    @Override
    public void aCodigoC(StringBuilder sb) {
        base.aCodigoC(sb);
        sb.append('[');
        indice.aCodigoC(sb);
        sb.append(']');
    }
}