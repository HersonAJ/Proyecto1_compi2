package com.example.contacto_3xtrat3r3str3.piglatin.nodo;

import java.util.List;

public sealed interface NodoSentencia extends NodoAST permits
        NodoSentencia.DeclaracionVariable,
        NodoSentencia.DeclaracionArreglo,
        NodoSentencia.DeclaracionStruct,
        NodoSentencia.Asignacion,
        NodoSentencia.IncrementoDecremento,
        NodoSentencia.Condicional,
        NodoSentencia.CicloDum,
        NodoSentencia.CicloFacere,
        NodoSentencia.CicloPer,
        NodoSentencia.Lectura,
        NodoSentencia.Escritura,
        NodoSentencia.InterrupcionCiclo,
        NodoSentencia.LlamadaFuncionSentencia {

    TipoNodoSentencia tipoNodo();

    // DECLARACIONES
    record DeclaracionVariable(int linea, int columna, String tipo, String nombre,
                               NodoExpr inicializacion) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.DECLARACION_VARIABLE; }
    }

    record DeclaracionArreglo(int linea, int columna, String tipo, String nombre,
                              int tamano, List<NodoExpr> inicializacion) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.DECLARACION_ARREGLO; }
    }

    record DeclaracionStruct(int linea, int columna, String tipo, String nombre,
                             List<NodoExpr> inicializacion) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.DECLARACION_STRUCT; }
    }

    // ASIGNACIONES
    record Asignacion(int linea, int columna, NodoExpr destino, NodoExpr valor) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.ASIGNACION; }
    }

    record IncrementoDecremento(int linea, int columna, String operador,
                                NodoExpr operando, boolean prefijo) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.INCREMENTO_DECREMENTO; }
    }

    // CONDICIONAL
    record Condicional(int linea, int columna, NodoExpr condicion,
                       List<NodoSentencia> cuerpoSi,
                       List<RamaAliter> ramasAliter,
                       List<NodoSentencia> cuerpoAliter) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.CONDICIONAL; }
    }

    record RamaAliter(int linea, int columna, NodoExpr condicion, List<NodoSentencia> cuerpo) {
    }

    // CICLOS
    record CicloDum(int linea, int columna, NodoExpr condicion,
                    List<NodoSentencia> cuerpo) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.CICLO_DUM; }
    }

    record CicloFacere(int linea, int columna, List<NodoSentencia> cuerpo,
                       NodoExpr condicion) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.CICLO_FACERE; }
    }

    record CicloPer(int linea, int columna, NodoSentencia inicializacion,
                    NodoExpr condicion, NodoSentencia actualizacion,
                    List<NodoSentencia> cuerpo) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.CICLO_PER; }
    }

    // LECTURA / ESCRITURA
    record Lectura(int linea, int columna, String variable) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.LECTURA; }
    }

    record Escritura(int linea, int columna, List<NodoExpr> valores) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.ESCRITURA; }
    }

    // INTERRUPCION DE CICLO
    record InterrupcionCiclo(int linea, int columna, String tipo) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.INTERRUPCION_CICLO; }
    }

    // LLAMADA A FUNCION COMO SENTENCIA
    record LlamadaFuncionSentencia(int linea, int columna, NodoExpr.LlamadaFuncion llamada) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.LLAMADA_FUNCION_SENTENCIA; }
    }
}