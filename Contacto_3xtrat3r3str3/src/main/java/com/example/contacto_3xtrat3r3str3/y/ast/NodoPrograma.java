package com.example.contacto_3xtrat3r3str3.y.ast;

import com.example.contacto_3xtrat3r3str3.c3d_v2.c.FuncionC;
import com.example.contacto_3xtrat3r3str3.y.semantica.TablaSimbolos;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodo raíz del AST. Contiene todas las estructuras y funciones
 * definidas en un archivo .y.
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

        /**
         * Recorre todas las funciones del programa y las traduce a FuncionC.
         * El resultado se pasa directamente al GeneradorC.
         */
        public List<FuncionC> aFuncionesC(TablaSimbolos tabla) {
            List<FuncionC> resultado = new ArrayList<>();
            for (NodoFuncion f : funciones) {
                if (f instanceof NodoFuncion.Funcion funcion) {
                    resultado.add(funcion.aFuncionC(tabla));
                }
            }
            return resultado;
        }
    }
}