package com.example.contacto_3xtrat3r3str3.piglatin.nodo;

import com.example.contacto_3xtrat3r3str3.c3d_v2.*;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.z.LlamadaMetodo1;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.z.NewObjeto;
import com.example.contacto_3xtrat3r3str3.c3d_v2.pig.ContextoTraduccionPig;
import com.example.contacto_3xtrat3r3str3.c3d_v2.pig.LiteralPig;
import com.example.contacto_3xtrat3r3str3.c3d_v2.pig.PromocionTiposPig;
import com.example.contacto_3xtrat3r3str3.c3d_v2.pig.TipoPigC;
import com.example.contacto_3xtrat3r3str3.piglatin.semantica.TablaSimbolosPig;

import java.util.ArrayList;
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
    AccesoMemoria aCodigoIntermedio(ContextoTraduccionPig ctx);

    // LITERALES
    record LiteralEntero(int linea, int columna, int valor) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_ENTERO; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccionPig ctx) {
            return new LiteralPig(valor, "numerus");
        }
    }
    record LiteralDecimal(int linea, int columna, double valor) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_DECIMAL; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccionPig ctx) {
            return new LiteralPig(valor, "decimalis");
        }
    }

    record LiteralTexto(int linea, int columna, String valor) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_TEXTO; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccionPig ctx) {
            return new LiteralPig(valor, "textum");
        }
    }

    record LiteralCaracter(int linea, int columna, char valor) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_CARACTER; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccionPig ctx) {
            return new LiteralPig(valor, "littera");
        }
    }

    record LiteralBool(int linea, int columna, boolean valor) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LITERAL_BOOL; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccionPig ctx) {
            return new LiteralPig(valor, "bool");
        }
    }

    // para '{1, 2, 3}' dentro de expresiones
    record ListaLiteral(int linea, int columna, List<NodoExpr> elementos) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LISTA_LITERAL; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccionPig ctx) {
            throw new IllegalStateException(
                    "ListaLiteral solo puede usarse como inicializador de arreglo o struct "
                            + "(línea " + linea + ")");
        }
    }

    // ACCESOS
    record Identificador(int linea, int columna, String nombre) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.IDENTIFICADOR; }

        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccionPig ctx) {
            var simboloOpt = ctx.getTabla().buscarVariable(nombre);
            if (simboloOpt.isEmpty()) {
                throw new IllegalStateException(
                        "Variable no declarada: '" + nombre + "' (línea " + linea + ")");
            }
            var simbolo = simboloOpt.get();

            String tipoC = TipoPigC.baseAC(simbolo.tipo(), simbolo.esObjeto());
            return new AccesoVariable(nombre, tipoC);
        }
    }

    record AccesoArray(int linea, int columna, NodoExpr arreglo, NodoExpr indice) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.ACCESO_ARRAY; }
        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccionPig ctx) {
            AccesoMemoria base = arreglo.aCodigoIntermedio(ctx);
            AccesoMemoria indiceAcc = indice.aCodigoIntermedio(ctx);

            String tipoElemento = obtenerTipoElemento(ctx, arreglo);
            return new AccesoArreglo(base, indiceAcc, tipoElemento);
        }

        private String obtenerTipoElemento(ContextoTraduccionPig ctx, NodoExpr baseExpr) {
            if (baseExpr instanceof Identificador id) {
                return ctx.getTabla().buscarVariable(id.nombre())
                        .map(s -> s.tipo())
                        .orElse("numerus");
            }
            if (baseExpr instanceof AccesoArray inner) {
                return obtenerTipoElemento(ctx, inner.arreglo());
            }
            if (baseExpr instanceof AccesoAtributo atr) {
                // Buscar el tipo del campo
                return "numerus"; // fallback por ahora
            }
            return "numerus";
        }
    }

    record AccesoAtributo(int linea, int columna, NodoExpr objeto, String atributo) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.ACCESO_ATRIBUTO; }
        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccionPig ctx) {
            AccesoMemoria base = objeto.aCodigoIntermedio(ctx);
            boolean porPuntero = esBaseObjeto(ctx, objeto);

            String tipoCampo = obtenerTipoCampo(ctx, objeto, atributo);
            return new AccesoAtributo1(base, atributo, porPuntero, tipoCampo);
        }

        private boolean esBaseObjeto(ContextoTraduccionPig ctx, NodoExpr baseExpr) {
            if (baseExpr instanceof Identificador id) {
                return ctx.getTabla().buscarVariable(id.nombre())
                        .map(s -> s.esObjeto())
                        .orElse(false);
            }
            return false; // fallback: tratar como struct por valor
        }

        private String obtenerTipoCampo(ContextoTraduccionPig ctx, NodoExpr baseExpr, String campo) {
            // Resolver el tipo de la base
            if (baseExpr instanceof Identificador id) {
                var v = ctx.getTabla().buscarVariable(id.nombre());
                if (v.isEmpty()) return "numerus";
                var simbolo = v.get();
                String tipoBase = simbolo.tipo();

                // Si es struct de .y
                var est = ctx.getTabla().buscarEstructura(tipoBase);
                if (est.isPresent()) {
                    String tipoCampo = est.get().atributos().get(campo);
                    if (tipoCampo != null) return tipoCampo;
                }
                // Si es clase de .z
                var clase = ctx.getTabla().buscarClase(tipoBase);
                if (clase.isPresent()) {
                    var atr = clase.get().atributos().get(campo);
                    if (atr != null) return atr.tipo();
                }
            }
            return "numerus";
        }
    }

    // OPERACIONES
    record Binaria(int linea, int columna, String operador, NodoExpr izquierda, NodoExpr derecha) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.BINARIA; }
        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccionPig ctx) {
            GestorCodigoIntermedio g = ctx.getGestor();

            AccesoMemoria izq = izquierda.aCodigoIntermedio(ctx);
            AccesoMemoria der = derecha.aCodigoIntermedio(ctx);

            // Caso especial: == y != con textum -> strcmp
            boolean hayTexto = "textum".equals(izq.getTipo()) || "textum".equals(der.getTipo());
            boolean esComparacion = "==".equals(operador) || "!=".equals(operador);
            if (hayTexto && esComparacion) {
                int idCmp = g.getContador().siguienteTemporal("numerus");
                AccesoTemporal tCmp = new AccesoTemporal(idCmp, "numerus");
                g.emitir(new OperacionBinaria(tCmp, izq, "strcmp", der));

                int idRes = g.getContador().siguienteTemporal("numerus");
                AccesoTemporal tRes = new AccesoTemporal(idRes, "numerus");
                g.emitir(new OperacionBinaria(tRes, tCmp, operador, new LiteralPig(0, "numerus")));
                return tRes;
            }

            // Promoción
            PromocionTiposPig.Resultado prom = PromocionTiposPig.promover(izq.getTipo(), der.getTipo());

            if (prom.conversionIzq() != null) {
                int idConv = g.getContador().siguienteTemporal(prom.conversionIzq());
                AccesoTemporal tConv = new AccesoTemporal(idConv, prom.conversionIzq());
                g.emitir(new ConversionTipo(tConv, TipoPigC.baseValorAC(prom.conversionIzq()), izq));
                izq = tConv;
            }
            if (prom.conversionDer() != null) {
                int idConv = g.getContador().siguienteTemporal(prom.conversionDer());
                AccesoTemporal tConv = new AccesoTemporal(idConv, prom.conversionDer());
                g.emitir(new ConversionTipo(tConv, TipoPigC.baseValorAC(prom.conversionDer()), der));
                der = tConv;
            }

            int idT = g.getContador().siguienteTemporal(prom.tipoResultado());
            AccesoTemporal t = new AccesoTemporal(idT, prom.tipoResultado());
            g.emitir(new OperacionBinaria(t, izq, operador, der));
            return t;
        }
    }

    record IncrementoDecremento(int linea, int columna, String operador,
                                NodoExpr operando, boolean prefijo) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.INCREMENTO_DECREMENTO; }
        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccionPig ctx) {
            GestorCodigoIntermedio g = ctx.getGestor();

            AccesoMemoria op = operando.aCodigoIntermedio(ctx);

            String opBinario = "++".equals(operador) ? "+" : "-";
            AccesoMemoria uno = new LiteralPig(1, "numerus");

            // x = x ± 1
            g.emitir(new OperacionBinaria(op, op, opBinario, uno));

            // Devolvemos el propio acceso (el valor actualizado ya está en x).
            return op;
        }
    }

    // LLAMADAS Y OBJETOS
    record LlamadaFuncion(int linea, int columna, String nombre, List<NodoExpr> argumentos) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LLAMADA_FUNCION; }
        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccionPig ctx) {
            GestorCodigoIntermedio g = ctx.getGestor();

            // 1. Evaluar argumentos.
            List<AccesoMemoria> args = new ArrayList<>();
            for (NodoExpr arg : argumentos) {
                args.add(arg.aCodigoIntermedio(ctx));
            }

            // 2. Buscar la función en la tabla.
            var funOpt = ctx.getTabla().buscarFuncion(nombre);
            if (funOpt.isEmpty()) {
                throw new IllegalStateException(
                        "Función no encontrada: '" + nombre + "' (línea " + linea + ")");
            }
            var funcion = funOpt.get();

            // 3. Nombre C: igual al nombre .y (sin sobrecarga).
            String nombreC = nombre;

            // 4. ¿Tiene retorno?
            String tipoRetorno = funcion.tipoRetorno();   // null si void
            if (tipoRetorno == null) {
                g.emitir(new Llamada(null, nombreC, args));
                return null;
            } else {
                int idT = g.getContador().siguienteTemporal(tipoRetorno);
                AccesoTemporal t = new AccesoTemporal(idT, tipoRetorno);
                g.emitir(new Llamada(t, nombreC, args));
                return t;
            }
        }
    }

    record LlamadaMetodo(int linea, int columna, NodoExpr objeto,
                         String nombre, List<NodoExpr> argumentos) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.LLAMADA_METODO; }
        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccionPig ctx) {
            GestorCodigoIntermedio g = ctx.getGestor();

            // 1. Evaluar receptor y argumentos.
            AccesoMemoria receptorAcc = objeto.aCodigoIntermedio(ctx);
            List<AccesoMemoria> args = new ArrayList<>();
            for (NodoExpr arg : argumentos) {
                args.add(arg.aCodigoIntermedio(ctx));
            }

            // 2. Nombre de la clase del receptor.
            String clase = obtenerNombreClase(receptorAcc, ctx);

            // 3. Buscar la firma del método.
            List<String> tiposArgs = new ArrayList<>();
            for (AccesoMemoria a : args) tiposArgs.add(a.getTipo());

            var claseOpt = ctx.getTabla().buscarClase(clase);
            if (claseOpt.isEmpty()) {
                throw new IllegalStateException(
                        "Clase no encontrada: '" + clase + "' (línea " + linea + ")");
            }
            var defClase = claseOpt.get();

            // Buscar el método por nombre y tipos
            var firmaOpt = defClase.metodos().stream()
                    .filter(f -> f.nombre().equals(nombre)
                            && tiposCompatibles(f.parametros(), tiposArgs))
                    .findFirst();
            if (firmaOpt.isEmpty()) {
                throw new IllegalStateException(
                        "Método no encontrado: '" + nombre + "' en clase '" + clase
                                + "' (línea " + linea + ")");
            }
            var firma = firmaOpt.get();

            // 4. Nombre C.
            String nombreC = construirNombreC(clase, nombre, firma);

            // 5. Emitir.
            String tipoRetorno = tipoYAPig(firma.tipoRetorno());
            if (tipoRetorno == null) {
                g.emitir(new LlamadaMetodo1(null, receptorAcc, nombreC, args));
                return null;
            } else {
                int idT = g.getContador().siguienteTemporal(tipoRetorno);
                AccesoTemporal t = new AccesoTemporal(idT, tipoRetorno);
                g.emitir(new LlamadaMetodo1(t, receptorAcc, nombreC, args));
                return t;
            }
        }

        private static String obtenerNombreClase(AccesoMemoria receptor, ContextoTraduccionPig ctx) {
            String tipo = receptor.getTipo();
            if (tipo.startsWith("struct ") && tipo.endsWith("*")) {
                return tipo.substring("struct ".length(), tipo.length() - 1).trim();
            }
            if (tipo.startsWith("struct ")) {
                return tipo.substring("struct ".length()).trim();
            }
            return tipo;
        }

        private static boolean tiposCompatibles(List<TablaSimbolosPig.Parametro> params,
                                                List<String> tiposArgs) {
            if (params.size() != tiposArgs.size()) return false;
            for (int i = 0; i < params.size(); i++) {
                if (!params.get(i).tipo().equals(tiposArgs.get(i))) return false;
            }
            return true;
        }

        private static String construirNombreC(String clase, String metodo, TablaSimbolosPig.Firma firma) {
            StringBuilder sb = new StringBuilder();
            sb.append(clase).append('_').append(metodo);
            for (TablaSimbolosPig.Parametro p : firma.parametros()) {
                sb.append('_').append(p.tipo());
            }
            return sb.toString();
        }
    }

    record InstanciaObjeto(int linea, int columna, String tipoClase, List<NodoExpr> argumentos) implements NodoExpr {
        @Override public TipoNodoExpr tipoNodo() { return TipoNodoExpr.INSTANCIA_OBJETO; }
        @Override
        public AccesoMemoria aCodigoIntermedio(ContextoTraduccionPig ctx) {
            GestorCodigoIntermedio g = ctx.getGestor();

            // 1. Evaluar argumentos.
            List<AccesoMemoria> args = new ArrayList<>();
            for (NodoExpr arg : argumentos) {
                args.add(arg.aCodigoIntermedio(ctx));
            }

            // 2. Buscar la clase.
            var claseOpt = ctx.getTabla().buscarClase(tipoClase);
            if (claseOpt.isEmpty()) {
                throw new IllegalStateException(
                        "Clase no encontrada: '" + tipoClase + "' (línea " + linea + ")");
            }
            var defClase = claseOpt.get();

            // 3. Resolver el constructor.
            List<String> tiposArgs = new ArrayList<>();
            for (AccesoMemoria a : args) tiposArgs.add(a.getTipo());

            var firmaOpt = defClase.constructores().stream()
                    .filter(f -> tiposCompatibles(f.parametros(), tiposArgs))
                    .findFirst();
            if (firmaOpt.isEmpty()) {
                throw new IllegalStateException(
                        "Constructor no encontrado en clase '" + tipoClase
                                + "' (línea " + linea + ")");
            }
            var firma = firmaOpt.get();

            // 4. Nombre C del constructor.
            String nombreConstructorC = construirNombreConstructorC(tipoClase, firma);

            // 5. Temporal del tipo Clase*.
            String tipoC = "struct " + tipoClase + "*";
            int idT = g.getContador().siguienteTemporal(tipoC);
            AccesoTemporal t = new AccesoTemporal(idT, tipoC);

            // 6. Emitir.
            g.emitir(new NewObjeto(t, tipoClase, nombreConstructorC, args));
            return t;
        }

        private static String construirNombreConstructorC(String clase, TablaSimbolosPig.Firma firma) {
            StringBuilder sb = new StringBuilder();
            sb.append(clase).append("_constructor");
            for (TablaSimbolosPig.Parametro p : firma.parametros()) {
                sb.append('_').append(p.tipo());
            }
            return sb.toString();
        }
    }

    private static String tipoYAPig(String tipoY) {
        if (tipoY == null) return null;
        return switch (tipoY) {
            case "entero"   -> "numerus";
            case "flotante" -> "decimalis";
            case "caracter" -> "littera";
            case "cadena"   -> "textum";
            case "bool"     -> "bool";
            default         -> tipoY;   // structs y otros se dejan igual
        };
    }

    static boolean tiposCompatibles(List<TablaSimbolosPig.Parametro> params,
                                    List<String> tiposArgs) {
        if (params.size() != tiposArgs.size()) return false;
        for (int i = 0; i < params.size(); i++) {
            if (!params.get(i).tipo().equals(tiposArgs.get(i))) return false;
        }
        return true;
    }
}