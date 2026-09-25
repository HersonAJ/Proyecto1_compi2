package com.example.contacto_3xtrat3r3str3.c3d_v2;

/**
 * Operando de una cuádrupla: literal, temporal, variable, acceso a arreglo,
 * acceso a atributo o etiqueta. Cada subclase sabe escribirse en C y
 * declarar su tipo en el lenguaje Y ("entero", "flotante", "caracter",
 * "cadena", "bool", o el nombre de una estructura).
 */
public abstract class AccesoMemoria implements TransformableACodigo {

    /**
     * Tipo del lenguaje Y que representa este operando.
     * Ej: "entero", "flotante", "caracter", "cadena", "bool", "Persona".
     */
    public abstract String getTipo();
}
