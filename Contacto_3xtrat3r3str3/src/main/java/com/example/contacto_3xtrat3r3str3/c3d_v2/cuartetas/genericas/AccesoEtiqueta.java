package com.example.contacto_3xtrat3r3str3.c3d_v2.cuartetas.genericas;


/**
 * Operando especial que representa una etiqueta.
 * Solo lo usan cuádruplas de control (salto, condicional).
 */
public class AccesoEtiqueta extends AccesoMemoria {

    private final int numero;

    public AccesoEtiqueta(int numero) {
        this.numero = numero;
    }

    public int getNumero() {
        return numero;
    }

    @Override
    public void aCodigoC(StringBuilder sb) {
        sb.append('L').append(numero);
    }

    @Override
    public String getTipo() { return "etiqueta"; }
}
