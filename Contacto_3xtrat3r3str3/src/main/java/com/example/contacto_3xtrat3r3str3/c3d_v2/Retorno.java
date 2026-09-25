package com.example.contacto_3xtrat3r3str3.c3d_v2;


/**
 * return;          (Retorno sin valor)
 * return valor;    (Retorno con valor)
 */
public class Retorno extends Cuarteta {

    private final AccesoMemoria valor; // puede ser null

    public Retorno(AccesoMemoria valor) {
        this.valor = valor;
    }

    public AccesoMemoria getValor() {
        return valor;
    }

    @Override
    public void aCodigoC(StringBuilder sb) {
        sb.append("    return");
        if (valor != null) {
            sb.append(' ');
            valor.aCodigoC(sb);
        }
        sb.append(";\n");
    }
}