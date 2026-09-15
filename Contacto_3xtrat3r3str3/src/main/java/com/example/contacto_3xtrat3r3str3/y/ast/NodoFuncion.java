package com.example.contacto_3xtrat3r3str3.y.ast;

import java.util.List;

/**
 * Representa la definición de una función:
 *
 *   definir suma(entero a, entero b) -> entero:
 *       retornar a + b
 *
 * Si 'tipoRetorno' es null, la función no retorna valor.
 */
public sealed interface NodoFuncion extends NodoAST permits NodoFuncion.Funcion {

    TipoNodoEstructura tipoNodo();

    record Funcion(int linea, int columna,
                   String nombre,
                   List<NodoParametro> parametros,
                   String tipoRetorno,                     // null si no retorna
                   List<NodoSentencia> cuerpo) implements NodoFuncion {
        @Override
        public TipoNodoEstructura tipoNodo() {
            return TipoNodoEstructura.FUNCION;
        }
    }
}
