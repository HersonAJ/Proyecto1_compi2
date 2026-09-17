package com.example.contacto_3xtrat3r3str3.y.ast;

import com.example.contacto_3xtrat3r3str3.y.ast.NodoAST;

import java.util.List;

public sealed interface NodoExpr extends NodoAST permits
        NodoExpr.LiteralEntero,
        NodoExpr.LiteralFlotante,
        NodoExpr.LiteralCadena,
        NodoExpr.LiteralCaracter,
        NodoExpr.LiteralBool,
        NodoExpr.Identificador,
        NodoExpr.AccesoArray,
        NodoExpr.AccesoAtributo,
        NodoExpr.Binaria,
        NodoExpr.Unaria,
        NodoExpr.LlamadaFuncion {

    TipoNodoExpr tipoNodo();

    // ============================================================
    // LITERALES
    // ============================================================

    record LiteralEntero(int linea, int columna, int valor) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() {
            return TipoNodoExpr.LITERAL_ENTERO;
        }
    }

    record LiteralFlotante(int linea, int columna, double valor) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() {
            return TipoNodoExpr.LITERAL_FLOTANTE;
        }
    }

    record LiteralCadena(int linea, int columna, String valor) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() {
            return TipoNodoExpr.LITERAL_CADENA;
        }
    }

    record LiteralCaracter(int linea, int columna, char valor) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() {
            return TipoNodoExpr.LITERAL_CARACTER;
        }
    }

    record LiteralBool(int linea, int columna, boolean valor) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() {
            return TipoNodoExpr.LITERAL_BOOL;
        }
    }

    // ============================================================
    // ACCESOS
    // ============================================================

    /**
     * Uso de una variable simple: 'edad', 'contador', 'nombre'.
     */
    record Identificador(int linea, int columna, String nombre) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() {
            return TipoNodoExpr.IDENTIFICADOR;
        }
    }

    /**
     * Acceso a un arreglo: 'numeros[0]', 'matriz[i][j]'.
     * Se modela en cadena: AccesoArray(AccesoArray(matriz, i), j).
     */
    record AccesoArray(int linea, int columna, NodoExpr arreglo, NodoExpr indice) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() {
            return TipoNodoExpr.ACCESO_ARRAY;
        }
    }

    /**
     * Acceso a atributo de estructura u objeto: 'p1.promedio', 'alumno1.nombre'.
     */
    record AccesoAtributo(int linea, int columna, NodoExpr objeto, String atributo) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() {
            return TipoNodoExpr.ACCESO_ATRIBUTO;
        }
    }

    // ============================================================
    // OPERACIONES
    // ============================================================

    /**
     * Operación binaria: +, -, *, /, ==, !=, <, >, <=, >=, &&, ||.
     * Ejemplo: edad > 18  ->  Binaria(">", Identificador(edad), LiteralEntero(18))
     */
    record Binaria(int linea, int columna, String operador, NodoExpr izquierda, NodoExpr derecha) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() {
            return TipoNodoExpr.BINARIA;
        }
    }


    //Operación unaria: -x, !x, ++x, --x, x++, x--.
    record Unaria(int linea, int columna, String operador, NodoExpr operando, boolean prefijo) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() {
            return TipoNodoExpr.UNARIA;
        }
    }

    // ============================================================
    // LLAMADAS A FUNCION
    // ============================================================

    /**
     * Llamada a función: 'suma(a, b)', 'calcular()'.
     */
    record LlamadaFuncion(int linea, int columna, String nombre, List<NodoExpr> argumentos) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() {
            return TipoNodoExpr.LLAMADA_FUNCION;
        }
    }
}