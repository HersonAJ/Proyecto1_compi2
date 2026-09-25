package com.example.contacto_3xtrat3r3str3.y.ast;

import com.example.contacto_3xtrat3r3str3.c3d_v2.*;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.ContextoTraduccion;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.TipoC;

import com.example.contacto_3xtrat3r3str3.c3d_v2.AccesoTemporal;
import com.example.contacto_3xtrat3r3str3.c3d_v2.PromocionTipos;
import com.example.contacto_3xtrat3r3str3.c3d_v2.OperacionBinaria;
import com.example.contacto_3xtrat3r3str3.c3d_v2.ConversionTipo;

import java.util.List;

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
        NodoExpr.LlamadaFuncion,
        NodoExpr.Leer {

    TipoNodoExpr tipoNodo();

    AccesoMemoria aCodigoIntermedio(ContextoTraduccion ctx);

    // LITERALES
    record LiteralEntero(int linea, int columna, int valor) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_ENTERO; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccion ctx) {
            return new Literal(valor, "entero");
        }
    }

    record LiteralFlotante(int linea, int columna, double valor) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_FLOTANTE; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccion ctx) {
            return new Literal(valor, "flotante");
        }
    }

    record LiteralCadena(int linea, int columna, String valor) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_CADENA; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccion ctx) {
            return new Literal(valor, "cadena");
        }
    }

    record LiteralCaracter(int linea, int columna, char valor) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_CARACTER; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccion ctx) {
            return new Literal(valor, "caracter");
        }
    }

    record LiteralBool(int linea, int columna, boolean valor) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_BOOL; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccion ctx) {
            return new Literal(valor, "bool");
        }
    }

    // ACCESOS
    record Identificador(int linea, int columna, String nombre) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() { return TipoNodoExpr.IDENTIFICADOR; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccion ctx) {
            var simboloOpt = ctx.getTabla().buscarVariable(nombre);
            if (simboloOpt.isEmpty()) {
                throw new IllegalStateException(
                        "Variable no declarada: '" + nombre + "' (línea " + linea + ")");
            }
            var simbolo = simboloOpt.get();
            String tipoC = TipoC.aTipoC(simbolo);
            return new AccesoVariable(nombre, tipoC);
        }
    }

    record AccesoArray(int linea, int columna, NodoExpr arreglo, NodoExpr indice) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() { return TipoNodoExpr.ACCESO_ARRAY; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccion ctx) {
            GestorCodigoIntermedio g = ctx.getGestor();

            // Caso 2D: el arreglo interno es también un AccesoArray
            if (arreglo instanceof AccesoArray interno) {
                return emitirAcceso2D(ctx, interno, indice);
            }

            // Caso 1D
            return emitirAcceso1D(ctx, arreglo, indice);
        }

        private AccesoMemoria emitirAcceso1D(ContextoTraduccion ctx,
                                             NodoExpr baseExpr,
                                             NodoExpr indiceExpr) {
            GestorCodigoIntermedio g = ctx.getGestor();

            AccesoMemoria base = baseExpr.aCodigoIntermedio(ctx);
            AccesoMemoria indiceAcc = indiceExpr.aCodigoIntermedio(ctx);
            String tipoElemento = obtenerTipoElemento(ctx, baseExpr);

            return new AccesoArreglo(base, indiceAcc, tipoElemento);
        }

        private AccesoMemoria emitirAcceso2D(ContextoTraduccion ctx,
                                             AccesoArray interno,
                                             NodoExpr indiceExterno) {
            GestorCodigoIntermedio g = ctx.getGestor();

            // Base real: el "arreglo" del interno (ej: Identificador("matriz")).
            AccesoMemoria base = interno.arreglo().aCodigoIntermedio(ctx);

            // Calcular i * cols + j
            AccesoMemoria i = interno.indice().aCodigoIntermedio(ctx);
            AccesoMemoria j = indiceExterno.aCodigoIntermedio(ctx);

            int cols = obtenerColumnas(ctx, interno.arreglo());

            // t1 = i * cols
            int idProd = g.getContador().siguienteTemporal("entero");
            AccesoTemporal tProd = new AccesoTemporal(idProd, "entero");
            g.emitir(new OperacionBinaria(tProd, i, "*", new Literal(cols, "entero")));

            // t2 = t1 + j
            int idSum = g.getContador().siguienteTemporal("entero");
            AccesoTemporal tSum = new AccesoTemporal(idSum, "entero");
            g.emitir(new OperacionBinaria(tSum, tProd, "+", j));

            String tipoElemento = obtenerTipoElemento(ctx, interno.arreglo());
            return new AccesoArreglo(base, tSum, tipoElemento);
        }

        private String obtenerTipoElemento(ContextoTraduccion ctx, NodoExpr baseExpr) {
            if (baseExpr instanceof Identificador id) {
                return ctx.getTabla().buscarVariable(id.nombre())
                        .map(s -> s.tipo())
                        .orElse("entero");
            }
            return "entero"; // fallback defensivo
        }

        private int obtenerColumnas(ContextoTraduccion ctx, NodoExpr baseExpr) {
            if (baseExpr instanceof Identificador id) {
                return ctx.getTabla().buscarVariable(id.nombre())
                        .filter(s -> s.tamanos().size() >= 2)
                        .map(s -> s.tamanos().get(1))   // columnas = segunda dimensión
                        .orElse(0);
            }
            return 0;
        }
    }

    record AccesoAtributo(int linea, int columna, NodoExpr objeto, String atributo) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() { return TipoNodoExpr.ACCESO_ATRIBUTO; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccion ctx) {
            // TODO: implementar en el paso correspondiente
            throw new UnsupportedOperationException("AccesoAtributo pendiente");
        }
    }

    // OPERACIONES
    record Binaria(int linea, int columna, String operador, NodoExpr izquierda, NodoExpr derecha) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() { return TipoNodoExpr.BINARIA; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccion ctx) {
            GestorCodigoIntermedio g = ctx.getGestor();

            // 1. Evaluar ambos lados.
            AccesoMemoria izq = izquierda.aCodigoIntermedio(ctx);
            AccesoMemoria der = derecha.aCodigoIntermedio(ctx);

            // 2. Caso especial: == o != con cadenas -> strcmp.
            boolean hayCadena = "cadena".equals(izq.getTipo()) || "cadena".equals(der.getTipo());
            boolean esComparacion = "==".equals(operador) || "!=".equals(operador);
            if (hayCadena && esComparacion) {
                // t0 = strcmp(izq, der)
                int idCmp = g.getContador().siguienteTemporal("entero");
                AccesoTemporal tCmp = new AccesoTemporal(idCmp, "entero");
                g.emitir(new OperacionBinaria(tCmp, izq, "strcmp", der));

                // t1 = (t0 == 0)  o  (t0 != 0)
                int idRes = g.getContador().siguienteTemporal("entero");
                AccesoTemporal tRes = new AccesoTemporal(idRes, "entero");
                g.emitir(new OperacionBinaria(tRes, tCmp, operador, new Literal(0, "entero")));
                return tRes;
            }

            // 3. Promoción de tipos normal.
            PromocionTipos.Resultado prom = PromocionTipos.promover(izq.getTipo(), der.getTipo());

            // 4. Conversiones explícitas si hacen falta.
            if (prom.conversionIzq() != null) {
                int idConv = g.getContador().siguienteTemporal(prom.conversionIzq());
                AccesoTemporal tConv = new AccesoTemporal(idConv, prom.conversionIzq());
                g.emitir(new ConversionTipo(tConv, TipoC.primitivoAC(prom.conversionIzq()), izq));
                izq = tConv;
            }
            if (prom.conversionDer() != null) {
                int idConv = g.getContador().siguienteTemporal(prom.conversionDer());
                AccesoTemporal tConv = new AccesoTemporal(idConv, prom.conversionDer());
                g.emitir(new ConversionTipo(tConv, TipoC.primitivoAC(prom.conversionDer()), der));
                der = tConv;
            }

            // 5. Crear temporal del tipo resultado y emitir la operación.
            int idT = g.getContador().siguienteTemporal(prom.tipoResultado());
            AccesoTemporal t = new AccesoTemporal(idT, prom.tipoResultado());
            g.emitir(new OperacionBinaria(t, izq, operador, der));
            return t;
        }
    }

    record Unaria(int linea, int columna, String operador, NodoExpr operando, boolean prefijo) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() { return TipoNodoExpr.UNARIA; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccion ctx) {
            GestorCodigoIntermedio g = ctx.getGestor();

            // 1. Evaluar el operando.
            AccesoMemoria op = operando.aCodigoIntermedio(ctx);

            // 2. Casos "-" y "!" (negación aritmética y lógica).
            if ("-".equals(operador) || "!".equals(operador)) {
                int idT = g.getContador().siguienteTemporal(op.getTipo());
                AccesoTemporal t = new AccesoTemporal(idT, op.getTipo());
                g.emitir(new OperacionUnaria(t, operador, op));
                return t;
            }

            // 3. Casos "++" y "--".
            if ("++".equals(operador) || "--".equals(operador)) {
                if (!(op instanceof AccesoVariable av)) {
                    // El semántico ya debería haberlo impedido.
                    throw new IllegalStateException(
                            "Operador '" + operador + "' solo válido sobre variables (línea " + linea + ")");
                }

                // Emitimos x = x ± 1 como OperacionBinaria con literal 1.
                String opBinario = "++".equals(operador) ? "+" : "-";
                AccesoMemoria uno = new Literal(1, "entero");
                g.emitir(new OperacionBinaria(av, av, opBinario, uno));

                // Devolvemos el propio acceso: el valor actualizado está en 'x'.
                return av;
            }

            // 4. Operador no soportado (no debería pasar).
            throw new IllegalStateException(
                    "Operador unario desconocido: '" + operador + "' (línea " + linea + ")");
        }
    }

    // LLAMADAS A FUNCION
    record LlamadaFuncion(int linea, int columna, String nombre, List<NodoExpr> argumentos) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LLAMADA_FUNCION; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccion ctx) {
            GestorCodigoIntermedio g = ctx.getGestor();

            // 1. Evaluar argumentos (en orden).
            List<AccesoMemoria> args = new java.util.ArrayList<>();
            for (NodoExpr arg : argumentos) {
                args.add(arg.aCodigoIntermedio(ctx));
            }

            // 2. Buscar la función en la tabla de símbolos.
            var funOpt = ctx.getTabla().buscarFuncion(nombre);
            if (funOpt.isEmpty()) {
                throw new IllegalStateException(
                        "Función no declarada: '" + nombre + "' (línea " + linea + ")");
            }
            var funcion = funOpt.get();

            // 3. Función sin retorno no puede usarse como expresión.
            if (funcion.tipoRetorno() == null) {
                throw new IllegalStateException(
                        "La función '" + nombre + "' no retorna valor y no puede usarse como expresión "
                                + "(línea " + linea + ")");
            }

            // 4. Crear temporal del tipo de retorno y emitir la llamada.
            int idT = g.getContador().siguienteTemporal(funcion.tipoRetorno());
            AccesoTemporal t = new AccesoTemporal(idT, funcion.tipoRetorno());
            g.emitir(new Llamada(t, nombre, args));
            return t;
        }
    }
    // LEER COMO EXPRESION
    record Leer(int linea, int columna) implements NodoExpr {
        @Override
        public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LEER; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccion ctx) {
            // TODO: implementar en el paso correspondiente — necesita una
            // instrucción C3D de lectura hacia un temporal (equivalente de
            // lectura a lo que Imprimir1 es de escritura). Sigue el mismo
            // patrón que AccesoArray/AccesoAtributo por ahora.
            throw new UnsupportedOperationException("Leer (expresion) pendiente");
        }
    }
}