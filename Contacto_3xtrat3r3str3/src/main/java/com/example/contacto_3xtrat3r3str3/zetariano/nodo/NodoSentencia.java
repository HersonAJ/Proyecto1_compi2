package com.example.contacto_3xtrat3r3str3.zetariano.nodo;

import java.util.List;

public sealed interface NodoSentencia extends NodoAST permits
        NodoSentencia.DeclaracionVariable,
        NodoSentencia.Asignacion,
        NodoSentencia.ExpresionComoSentencia,
        NodoSentencia.Condicional,
        NodoSentencia.Switch,
        NodoSentencia.CasoSwitch,
        NodoSentencia.CasoDefault,
        NodoSentencia.CicloPara,
        NodoSentencia.CicloMientras,
        NodoSentencia.CicloHacerMientras,
        NodoSentencia.Retorno,
        NodoSentencia.Imprimir,
        NodoSentencia.Leer,
        NodoSentencia.Romper,
        NodoSentencia.Continuar {

    TipoNodoSentencia tipoNodo();

    // DECLARACION

    //'int edad = 25;', 'int[] calificaciones = new int[5];', 'int[][] matriz = new int[3][3];'.

    record DeclaracionVariable(int linea, int columna, String tipo, String nombre,
                               int dimensiones, NodoExpr inicializacion) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.DECLARACION_VARIABLE; }
    }

    // ASIGNACION

    // 'x = 5;', 'x += 3;', 'numeros[2] = numeros[0] * 3;'. 'operador' guarda '=', '+=', '-=' o '*='.
    record Asignacion(int linea, int columna, String operador, NodoExpr destino, NodoExpr valor) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.ASIGNACION; }
    }

    // EXPRESION COMO SENTENCIA

    //Cualquier expresion usada como instruccion suelta: 'p1.saludar();', 'a++;', 'calcular(x);
    record ExpresionComoSentencia(int linea, int columna, NodoExpr expresion) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.EXPRESION_COMO_SENTENCIA; }
    }

    // CONDICIONAL

    //if (cond) {...} else {...}
    record Condicional(int linea, int columna, NodoExpr condicion,
                       List<NodoSentencia> cuerpoSi,
                       List<NodoSentencia> cuerpoSino) implements NodoSentencia { // null si no hay 'else'
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.CONDICIONAL; }
    }

    // SWITCH
    record Switch(int linea, int columna, NodoExpr expresion,
                  List<CasoSwitch> casos, CasoDefault casoDefault) implements NodoSentencia { // casoDefault puede ser null
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.SWITCH; }
    }

    // 'case 1: ... break;' — 'break' es opcional
    record CasoSwitch(int linea, int columna, NodoExpr valor, List<NodoSentencia> cuerpo) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.CASO_SWITCH; }
    }

    record CasoDefault(int linea, int columna, List<NodoSentencia> cuerpo) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.CASO_DEFAULT; }
    }

    // CICLOS
    //for (init; cond; actualizacion) {...} Las 3 partes son opcionales ('for(;;)' es valido -> null en las 3)
    record CicloPara(int linea, int columna,
                     NodoSentencia inicializacion,
                     NodoExpr condicion,
                     NodoSentencia actualizacion,
                     List<NodoSentencia> cuerpo) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.CICLO_PARA; }
    }

    record CicloMientras(int linea, int columna, NodoExpr condicion, List<NodoSentencia> cuerpo) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.CICLO_MIENTRAS; }
    }

    record CicloHacerMientras(int linea, int columna, List<NodoSentencia> cuerpo, NodoExpr condicion) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.CICLO_HACER_MIENTRAS; }
    }

    // RETORNO
    // 'return valor;' o 'return;' (valor null para metodos void).
    record Retorno(int linea, int columna, NodoExpr valor) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.RETORNO; }
    }

    // 'println(expr)' o 'print(expr)'. 'saltoDeLinea' distingue cual de las dos fue.
    record Imprimir(int linea, int columna, boolean saltoDeLinea, NodoExpr expresion) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.IMPRIMIR; }
    }

    // 'readln();'
    record Leer(int linea, int columna) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.LEER; }
    }

    record Romper(int linea, int columna) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.ROMPER; }
    }

    record Continuar(int linea, int columna) implements NodoSentencia {
        @Override public TipoNodoSentencia tipoNodo() { return TipoNodoSentencia.CONTINUAR; }
    }
}