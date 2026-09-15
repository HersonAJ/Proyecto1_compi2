package com.example.contacto_3xtrat3r3str3.y.ast;

import java.util.List;

/**
 * Nodo raíz del AST. Contiene todas las estructuras y funciones
 * definidas en un archivo .y.
 *
 * Recordatorio: en Y? NO se permiten variables globales.
 */
public sealed interface NodoPrograma extends NodoAST permits NodoPrograma.Programa {

    TipoNodoEstructura tipoNodo();

    record Programa(int linea, int columna,
                    List<NodoEstructura> estructuras,
                    List<NodoFuncion> funciones) implements NodoPrograma {
        @Override
        public TipoNodoEstructura tipoNodo() {
            return TipoNodoEstructura.PROGRAMA;
        }
    }
}
