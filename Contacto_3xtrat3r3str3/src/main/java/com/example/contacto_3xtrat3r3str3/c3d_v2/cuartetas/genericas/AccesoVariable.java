package com.example.contacto_3xtrat3r3str3.c3d_v2.cuartetas.genericas;


/**
 * Uso de una variable simple. El tipo es informativo; el nombre va tal cual a C.
 */
public class AccesoVariable extends AccesoMemoria {

    private final String nombre;
    private final String tipo;

    public AccesoVariable(String nombre, String tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    @Override
    public void aCodigoC(StringBuilder sb) {
        sb.append(nombre);
    }
}