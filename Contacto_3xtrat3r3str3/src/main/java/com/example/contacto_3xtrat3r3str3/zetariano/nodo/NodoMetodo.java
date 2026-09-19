package com.example.contacto_3xtrat3r3str3.zetariano.nodo;

import java.util.List;

 //public void saludar() { ... }
 //public int calcularAnioNacimiento(int anioActual) { ... }
 //tipoRetorno es null cuando el metodo es 'void'.
public record NodoMetodo(int linea, int columna, String nombre,
                         List<NodoParametro> parametros,
                         String tipoRetorno,
                         List<NodoSentencia> cuerpo) implements NodoAST {}
