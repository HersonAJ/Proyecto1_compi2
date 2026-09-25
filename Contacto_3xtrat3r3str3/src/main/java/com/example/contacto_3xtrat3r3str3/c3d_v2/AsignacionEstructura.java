package com.example.contacto_3xtrat3r3str3.c3d_v2;

/**
 * Copia completa de un struct: p3 = p1
 * En C es un simple '=', pero semánticamente es una copia campo a campo.
 */
public class AsignacionEstructura extends Cuarteta {

    private final AccesoMemoria destino;
    private final AccesoMemoria fuente;

    public AsignacionEstructura(AccesoMemoria destino, AccesoMemoria fuente) {
        this.destino = destino;
        this.fuente = fuente;
    }

    public AccesoMemoria getDestino() { return destino; }
    public AccesoMemoria getFuente()  { return fuente; }

    @Override
    public void aCodigoC(StringBuilder sb) {
        sb.append("    ");
        destino.aCodigoC(sb);
        sb.append(" = ");
        fuente.aCodigoC(sb);
        sb.append(";\n");
    }
}