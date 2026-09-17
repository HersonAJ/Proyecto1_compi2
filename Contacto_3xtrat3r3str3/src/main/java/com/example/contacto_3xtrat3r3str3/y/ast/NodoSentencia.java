package com.example.contacto_3xtrat3r3str3.y.ast;

import java.util.List;

public sealed interface NodoSentencia extends NodoAST permits
        NodoSentencia.DeclaracionVariable,
        NodoSentencia.DeclaracionArreglo,
        NodoSentencia.DeclaracionMatriz,
        NodoSentencia.DeclaracionEstructura,
        NodoSentencia.Asignacion,
        NodoSentencia.IncrementoDecremento,
        NodoSentencia.Condicional,
        NodoSentencia.Elegir,
        NodoSentencia.CasoElegir,
        NodoSentencia.SiempreElegir,
        NodoSentencia.CicloPara,
        NodoSentencia.CicloMientras,
        NodoSentencia.CicloHacerMientras,
        NodoSentencia.Retorno,
        NodoSentencia.Imprimir,
        NodoSentencia.Leer,
        NodoSentencia.Romper,
        NodoSentencia.Continuar {
    TipoNodoSentencia tipoNodo();

    // ============================================================
    // DECLARACIONES
    // ============================================================

    /**
     * Declaración de variable simple: 'entero edad = 25'.
     * La inicialización es opcional ('entero edad').
     */
    record DeclaracionVariable(int linea, int columna, String tipo, String nombre,
                               NodoExpr inicializacion) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.DECLARACION_VARIABLE;
        }
    }

    /**
     * Declaración de arreglo: 'entero numeros[5] = {10, 20, 30, 40, 50}'.
     * La lista de inicialización es opcional.
     */
    record DeclaracionArreglo(int linea, int columna, String tipo, String nombre,
                              int tamano, List<NodoExpr> inicializacion) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.DECLARACION_ARREGLO;
        }
    }

    //Declaración de matriz: 'entero matriz[3][3]'.
    record DeclaracionMatriz(int linea, int columna, String tipo, String nombre,
                             int filas, int columnas) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.DECLARACION_MATRIZ;
        }
    }

    /**
     * Declaración de variable de tipo estructura: 'Persona alumno1'.
     * La inicialización con literal de estructura es opcional.
     */
    record DeclaracionEstructura(int linea, int columna, String tipoEstructura, String nombre,
                                 List<NodoExpr> inicializacion) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.DECLARACION_ESTRUCTURA;
        }
    }

    // ============================================================
    // ASIGNACION E INCREMENTO/DECREMENTO
    // ============================================================

    /**
     * Asignación: 'x = 10', 'numeros[0] = 5', 'p1.nombre = "Ana"'.
     */
    record Asignacion(int linea, int columna, NodoExpr destino, NodoExpr valor) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.ASIGNACION;
        }
    }

    /**
     * Incremento/decremento como sentencia: 'contador++', 'i--'.
     */
    record IncrementoDecremento(int linea, int columna, String operador,
                                String nombre) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.INCREMENTO_DECREMENTO;
        }
    }

    // ============================================================
    // CONDICIONAL
    // ============================================================

    /**
     * Condicional:
     *   si (cond) entonces
     *       ...
     *   sino (cond) entonces
     *       ...
     *   contrario
     *       ...
     *
     * 'sino' y 'contrario' son opcionales.
     */
    record Condicional(int linea, int columna,
                       NodoExpr condicion,
                       List<NodoSentencia> cuerpoSi,
                       NodoExpr condicionSino,           // puede ser null
                       List<NodoSentencia> cuerpoSino,   // puede ser null
                       List<NodoSentencia> cuerpoContrario) implements NodoSentencia { // puede ser null
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.CONDICIONAL;
        }
    }

    // ============================================================
    // ELEGIR
    // ============================================================

    /**
     * Estructura 'elegir(opcion):' con sus casos y 'siempre'.
     */
    record Elegir(int linea, int columna, NodoExpr expresion,
                  List<CasoElegir> casos, SiempreElegir siempre) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.ELEGIR;
        }
    }

    /**
     * Un caso dentro de 'elegir': 'caso 1: ... romper'.
     */
    record CasoElegir(int linea, int columna, NodoExpr valor,
                      List<NodoSentencia> cuerpo) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.ELEGIR;
        }
    }

    /**
     * Bloque 'siempre:' (default) dentro de 'elegir'.
     */
    record SiempreElegir(int linea, int columna,
                         List<NodoSentencia> cuerpo) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.ELEGIR;
        }
    }

    // ============================================================
    // CICLOS
    // ============================================================

    /**
     * Ciclo 'para(entero i = 0; i < 10; i++):'.
     */
    record CicloPara(int linea, int columna,
                     String tipoInicializacion, String nombreVariable,
                     NodoExpr valorInicial, NodoExpr condicion,
                     String operadorActualizacion,
                     List<NodoSentencia> cuerpo) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.CICLO_PARA;
        }
    }

    /**
     * Ciclo 'mientras(cond) hacer'.
     */
    record CicloMientras(int linea, int columna,
                         NodoExpr condicion,
                         List<NodoSentencia> cuerpo) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.CICLO_MIENTRAS;
        }
    }

    /**
     * Ciclo 'hacer: ... mientras(cond)'.
     */
    record CicloHacerMientras(int linea, int columna,
                              List<NodoSentencia> cuerpo,
                              NodoExpr condicion) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.CICLO_HACER_MIENTRAS;
        }
    }

    // ============================================================
    // RETORNO
    // ============================================================

    /**
     * 'retornar 160' o 'retornar'.
     */
    record Retorno(int linea, int columna, NodoExpr valor) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.RETORNO;
        }
    }

    // ============================================================
    // FUNCIONES ESPECIALES
    // ============================================================

    /**
     * 'imprimir(expresion)'.
     */
    record Imprimir(int linea, int columna, NodoExpr expresion) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.IMPRIMIR;
        }
    }

    /**
     * 'leer()' sin asignación a variable.
     * (La variante 'cadena x = leer()' se modela como DeclaracionVariable
     *  con inicializacion = LlamadaFuncion("leer").)
     */
    record Leer(int linea, int columna) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.LEER;
        }
    }

    // ============================================================
    // CONTROL DE CICLOS
    // ============================================================

    record Romper(int linea, int columna) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.ROMPER;
        }
    }

    record Continuar(int linea, int columna) implements NodoSentencia {
        @Override
        public TipoNodoSentencia tipoNodo() {
            return TipoNodoSentencia.CONTINUAR;
        }
    }
}
