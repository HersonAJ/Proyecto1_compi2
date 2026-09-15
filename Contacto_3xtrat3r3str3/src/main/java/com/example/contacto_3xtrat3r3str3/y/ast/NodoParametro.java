package com.example.contacto_3xtrat3r3str3.y.ast;

/**
 * Representa un parámetro de una función.
 *
 * Casos posibles según el PDF:
 *   entero miEntero
 *   [] entero miArray           (arreglo, por referencia)
 *   {} MiEstructura miStruct    (estructura, por referencia)
 */
public sealed interface NodoParametro extends NodoAST permits NodoParametro.Parametro {

    TipoNodoEstructura tipoNodo();

    /**
     * @param tipoPrimitivo  tipo base ('entero', 'cadena', ...) o null si es estructura.
     * @param tipoEstructura nombre de la estructura o null si es primitivo.
     * @param nombre         nombre del parámetro.
     * @param esArreglo      true si se declara como '[] tipo nombre'.
     * @param esEstructura   true si se declara como '{} Estructura nombre'.
     */
    record Parametro(int linea, int columna,
                     String tipoPrimitivo,
                     String tipoEstructura,
                     String nombre,
                     boolean esArreglo,
                     boolean esEstructura) implements NodoParametro {
        @Override
        public TipoNodoEstructura tipoNodo() {
            return TipoNodoEstructura.PARAMETRO;
        }
    }
}