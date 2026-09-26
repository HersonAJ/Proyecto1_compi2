package com.example.contacto_3xtrat3r3str3.c3d_v2.cuartetas.genericas;

/**
 * Literal de cualquier tipo primitivo.
 * - Entero: 10
 * - Flotante: 3.14
 * - Caracter: 'a'
 * - Cadena: "hola"
 * - Bool: 1 / 0
 */
public class Literal extends AccesoMemoria {

    private final Object valor;
    private final String tipo; // "entero", "flotante", "caracter", "cadena", "bool"

    public Literal(Object valor, String tipo) {
        this.valor = valor;
        this.tipo = tipo;
    }

    public Object getValor() {
        return valor;
    }

    public String getTipo() {
        return tipo;
    }

    @Override
    public void aCodigoC(StringBuilder sb) {
        switch (tipo) {
            case "cadena" -> sb.append('"').append(valor).append('"');
            case "caracter" -> sb.append('\'').append(valor).append('\'');
            case "bool" -> sb.append(((Boolean) valor) ? 1 : 0);
            default -> sb.append(valor);
        }
    }
}
