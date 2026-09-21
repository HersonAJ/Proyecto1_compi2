package com.example.contacto_3xtrat3r3str3.piglatin.nodo;

import java.util.List;

 // Nodo raíz del AST de PigLatin.
 // Contiene: importaciones, variables globales (opcional) y el cuerpo del MAIOR.
public record NodoPrograma(int linea, int columna,
                           List<NodoImportacion> importaciones,
                           List<NodoSentencia> variablesGlobales,
                           List<NodoSentencia> cuerpoMain) implements NodoAST {
}