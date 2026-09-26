package com.example.contacto_3xtrat3r3str3.c3d_v2.cuartetas.cuartetasPig;

import com.example.contacto_3xtrat3r3str3.c3d_v2.cuartetas.genericas.AccesoMemoria;

/**
 * Literal del lenguaje PigLatin.
 * Tipos: "numerus", "decimalis", "littera", "textum", "bool".
 */
public class LiteralPig extends AccesoMemoria {

    private final Object valor;
    private final String tipo;

    public LiteralPig(Object valor, String tipo) {
        this.valor = valor;
        this.tipo = tipo;
    }

    public Object getValor() { return valor; }

    @Override
    public String getTipo() { return tipo; }

    @Override
    public void aCodigoC(StringBuilder sb) {
        switch (tipo) {
            case "textum"  -> sb.append('"').append(valor).append('"');
            case "littera" -> sb.append('\'').append(valor).append('\'');
            case "bool"    -> sb.append(((Boolean) valor) ? 1 : 0);
            default        -> sb.append(valor);   // numerus, decimalis
        }
    }
}