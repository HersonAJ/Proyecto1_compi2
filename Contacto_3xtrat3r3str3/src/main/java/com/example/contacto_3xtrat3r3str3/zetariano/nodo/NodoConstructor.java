package com.example.contacto_3xtrat3r3str3.zetariano.nodo;

import java.util.List;

public record NodoConstructor(int linea, int columna, String nombre,
                              List<NodoParametroZ> parametros,
                              List<NodoSentencia> cuerpo) implements NodoAST {}
