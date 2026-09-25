package com.example.contacto_3xtrat3r3str3.c3d_v2;

/**
 * x = y
 */
public class AsignacionVariable extends Cuarteta {

    private final AccesoMemoria destino;
    private final AccesoMemoria valor;

    public AsignacionVariable(AccesoMemoria destino, AccesoMemoria valor) {
        this.destino = destino;
        this.valor = valor;
    }

    public AccesoMemoria getDestino() { return destino; }
    public AccesoMemoria getValor()   { return valor; }

    @Override
    public void aCodigoC(StringBuilder sb) {
        sb.append("    ");
        destino.aCodigoC(sb);
        sb.append(" = ");
        valor.aCodigoC(sb);
        sb.append(";\n");
    }
}