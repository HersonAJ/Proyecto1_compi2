package com.example.contacto_3xtrat3r3str3.y.ast;

/**
 * Representa un atributo dentro de una estructura.
 *
 * Casos posibles:
 *   entero edad
 *   entero miArray[10]
 *   MiEstructura miEstructura     (estructura anidada)
 */
public sealed interface NodoAtributo extends NodoAST permits NodoAtributo.Atributo {

    TipoNodoEstructura tipoNodo();

    /**
     * @param tipoPrimitivo  nombre del tipo si es primitivo ('entero', 'cadena', ...),
     *                       o null si es estructura anidada.
     * @param tipoEstructura nombre de la estructura anidada, o null si es primitivo.
     * @param nombre         nombre del atributo.
     * @param tamanoArreglo  tamaño si es arreglo; -1 si no lo es.
     */
    record Atributo(int linea, int columna,
                    String tipoPrimitivo,
                    String tipoEstructura,
                    String nombre,
                    int tamanoArreglo) implements NodoAtributo {
        @Override
        public TipoNodoEstructura tipoNodo() {
            return TipoNodoEstructura.ATRIBUTO;
        }
    }
}