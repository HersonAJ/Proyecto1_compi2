package com.example.contacto_3xtrat3r3str3.zetariano.nodo;

import java.util.List;

public record NodoClase(
        int linea, int columna, String nombre,
        List<NodoAtributoZ> atributos,
        List<NodoConstructor> constructores,
        List<NodoMetodo> metodos) implements NodoAST { }
