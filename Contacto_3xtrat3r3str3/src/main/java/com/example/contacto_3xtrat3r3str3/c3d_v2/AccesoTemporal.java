package com.example.contacto_3xtrat3r3str3.c3d_v2;

public class AccesoTemporal extends AccesoMemoria {

    private final int numero;
    private final String tipo;

    public AccesoTemporal(int numero, String tipo) {
        this.numero = numero;
        this.tipo = tipo;
    }

    public int getNumero() { return numero; }

    @Override
    public String getTipo() { return tipo; }

    @Override
    public void aCodigoC(StringBuilder sb) {
        sb.append('t').append(numero);
    }
}