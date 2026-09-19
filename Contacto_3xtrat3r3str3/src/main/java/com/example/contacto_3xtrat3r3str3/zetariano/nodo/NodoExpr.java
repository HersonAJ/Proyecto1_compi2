package com.example.contacto_3xtrat3r3str3.zetariano.nodo;

import java.util.List;

public sealed interface NodoExpr extends NodoAST permits
        NodoExpr.LiteralEntero,
        NodoExpr.LiteralDecimal,
        NodoExpr.LiteralCadena,
        NodoExpr.LiteralCaracter,
        NodoExpr.LiteralBool,
        NodoExpr.LiteralNulo,
        NodoExpr.ListaLiteral,
        NodoExpr.Identificador,
        NodoExpr.AccesoArray,
        NodoExpr.AccesoAtributo,
        NodoExpr.Binaria,
        NodoExpr.Unaria,
        NodoExpr.IncrementoDecremento,
        NodoExpr.Ternaria,
        NodoExpr.LlamadaFuncion,
        NodoExpr.LlamadaMetodo,
        NodoExpr.InstanciaObjeto,
        NodoExpr.ArregloNuevo {

    TipoNodoExpr tipoNodo();

    // LITERALES
    record LiteralEntero(int linea, int columna, int valor) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_ENTERO; }
    }

    record LiteralDecimal(int linea, int columna, double valor) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_DECIMAL; }
    }

    record LiteralCadena(int linea, int columna, String valor) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_CADENA; }
    }

    record LiteralCaracter(int linea, int columna, char valor) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_CARACTER; }
    }

    record LiteralBool(int linea, int columna, boolean valor) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_BOOL; }
    }

    record LiteralNulo(int linea, int columna) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_NULO; }
    }

    //El inicializador '{10, 20, 30}' de un arreglo. Solo aparece como parte de una DeclaracionVariable, nunca suelto en medio de una expresion normal
    record ListaLiteral(int linea, int columna, List<NodoExpr> elementos) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LISTA_LITERAL; }
    }

    // ACCESOS
    record Identificador(int linea, int columna, String nombre) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.IDENTIFICADOR; }
    }

    // 'numeros[0]', 'matriz[i][j]'
    record AccesoArray(int linea, int columna, NodoExpr arreglo, NodoExpr indice) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.ACCESO_ARRAY; }
    }

    // 'p1.edad'
    record AccesoAtributo(int linea, int columna, NodoExpr objeto, String atributo) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.ACCESO_ATRIBUTO; }
    }

    // OPERACIONES
    // +, -, *, /, %, ==, !=, <, >, <=, >=, &&, ||
    record Binaria(int linea, int columna, String operador, NodoExpr izquierda, NodoExpr derecha) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.BINARIA; }
    }

    // -x, !x (unario puro, no modifica la variable)
    record Unaria(int linea, int columna, String operador, NodoExpr operando) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.UNARIA; }
    }

    //++x, --x, x++, x--
    record IncrementoDecremento(int linea, int columna, String operador,
                                NodoExpr operando, boolean prefijo) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.INCREMENTO_DECREMENTO; }
    }

    // condicion ? siVerdadero : siFalso
    record Ternaria(int linea, int columna, NodoExpr condicion,
                    NodoExpr siVerdadero, NodoExpr siFalso) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.TERNARIA; }
    }

    // LLAMADAS Y CREACION DE OBJETOS
    // Llamada sin objeto explicito: 'calcular(x)'
    record LlamadaFuncion(int linea, int columna, String nombre, List<NodoExpr> argumentos) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LLAMADA_FUNCION; }
    }

    //'p1.saludar()', 'p1.calcularAnioNacimiento(2026)'
    record LlamadaMetodo(int linea, int columna, NodoExpr objeto,
                         String nombre, List<NodoExpr> argumentos) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LLAMADA_METODO; }
    }

    //'new Persona("Carlos", 25)'
    record InstanciaObjeto(int linea, int columna, String tipoClase, List<NodoExpr> argumentos) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.INSTANCIA_OBJETO; }
    }

    record ArregloNuevo(int linea, int columna, String tipoBase, List<NodoExpr> dimensiones) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.ARREGLO_NUEVO; }
    }
}