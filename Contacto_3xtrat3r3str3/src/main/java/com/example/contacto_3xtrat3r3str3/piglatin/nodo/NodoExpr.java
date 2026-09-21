package com.example.contacto_3xtrat3r3str3.piglatin.nodo;

import java.util.List;

public sealed interface NodoExpr extends NodoAST permits
        NodoExpr.LiteralEntero,
        NodoExpr.LiteralDecimal,
        NodoExpr.LiteralTexto,
        NodoExpr.LiteralCaracter,
        NodoExpr.LiteralBool,
        NodoExpr.ListaLiteral,
        NodoExpr.Identificador,
        NodoExpr.AccesoArray,
        NodoExpr.AccesoAtributo,
        NodoExpr.Binaria,
        NodoExpr.IncrementoDecremento,
        NodoExpr.LlamadaFuncion,
        NodoExpr.LlamadaMetodo,
        NodoExpr.InstanciaObjeto {

    TipoNodoExpr tipoNodo();

    // LITERALES
    record LiteralEntero(int linea, int columna, int valor) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_ENTERO; }
    }

    record LiteralDecimal(int linea, int columna, double valor) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_DECIMAL; }
    }

    record LiteralTexto(int linea, int columna, String valor) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_TEXTO; }
    }

    record LiteralCaracter(int linea, int columna, char valor) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_CARACTER; }
    }

    record LiteralBool(int linea, int columna, boolean valor) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_BOOL; }
    }

    // para '{1, 2, 3}' dentro de expresiones
    record ListaLiteral(int linea, int columna, List<NodoExpr> elementos) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LISTA_LITERAL; }
    }

    // ACCESOS
    record Identificador(int linea, int columna, String nombre) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.IDENTIFICADOR; }
    }

    record AccesoArray(int linea, int columna, NodoExpr arreglo, NodoExpr indice) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.ACCESO_ARRAY; }
    }

    record AccesoAtributo(int linea, int columna, NodoExpr objeto, String atributo) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.ACCESO_ATRIBUTO; }
    }

    // OPERACIONES
    record Binaria(int linea, int columna, String operador, NodoExpr izquierda, NodoExpr derecha) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.BINARIA; }
    }

    record IncrementoDecremento(int linea, int columna, String operador,
                                NodoExpr operando, boolean prefijo) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.INCREMENTO_DECREMENTO; }
    }

    // LLAMADAS Y OBJETOS
    record LlamadaFuncion(int linea, int columna, String nombre, List<NodoExpr> argumentos) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LLAMADA_FUNCION; }
    }

    record LlamadaMetodo(int linea, int columna, NodoExpr objeto,
                         String nombre, List<NodoExpr> argumentos) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LLAMADA_METODO; }
    }

    record InstanciaObjeto(int linea, int columna, String tipoClase, List<NodoExpr> argumentos) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.INSTANCIA_OBJETO; }
    }
}