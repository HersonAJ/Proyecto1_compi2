package com.example.contacto_3xtrat3r3str3.c3d_v2;

public class AccesoTemporal extends AccesoMemoria {

    private final int numero;

    public AccesoTemporal(int numero) {
        this.numero = numero;
    }

    public int getNumero() {
        return numero;
    }

    @Override
    public void aCodigoC(StringBuilder sb) {
        sb.append('t').append(numero);
    }
}