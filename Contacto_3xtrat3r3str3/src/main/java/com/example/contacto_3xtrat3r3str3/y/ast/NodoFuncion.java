package com.example.contacto_3xtrat3r3str3.y.ast;

import com.example.contacto_3xtrat3r3str3.c3d_v2.Cuarteta;
import com.example.contacto_3xtrat3r3str3.c3d_v2.GestorCodigoIntermedio;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.*;
import com.example.contacto_3xtrat3r3str3.y.semantica.TablaSimbolos;

import java.util.ArrayList;
import java.util.List;

public sealed interface NodoFuncion extends NodoAST permits NodoFuncion.Funcion {

    TipoNodoEstructura tipoNodo();

    record Funcion(int linea, int columna,
                   String nombre,
                   List<NodoParametro> parametros,
                   String tipoRetorno,
                   List<NodoSentencia> cuerpo) implements NodoFuncion {

        @Override
        public TipoNodoEstructura tipoNodo() {
            return TipoNodoEstructura.FUNCION;
        }

        public FuncionC aFuncionC(TablaSimbolos tabla) {
            GestorCodigoIntermedio gestor = new GestorCodigoIntermedio();
            ContextoTraduccion ctx = new ContextoTraduccion(gestor, tabla);

            List<VariableLocalC> variablesLocales = new ArrayList<>();

            tabla.entrarScope(nombre);
            try {
                declararParametrosEnScope(tabla);
                recogerYDeclararVariablesLocales(tabla, cuerpo, variablesLocales);

                for (NodoSentencia s : cuerpo) {
                    s.aCodigoIntermedio(ctx);
                }
            } finally {
                tabla.salirScope();
            }

            List<ParametroC> paramsC = new ArrayList<>();
            for (NodoParametro p : parametros) {
                if (p instanceof NodoParametro.Parametro par) {
                    paramsC.add(aParametroC(par));
                }
            }

            String tipoRetornoC = (tipoRetorno == null) ? "void" : TipoC.primitivoAC(tipoRetorno);
            List<Cuarteta> cuartetas = gestor.getCuartetas();
            List<String> tiposTemporales = gestor.getContador().getTiposTemporales();

            return new FuncionC(tipoRetornoC, nombre, paramsC, variablesLocales,
                    cuartetas, tiposTemporales);
        }

        // Declaración en la tabla de símbolos + recolección para C
        private void declararParametrosEnScope(TablaSimbolos tabla) {
            for (NodoParametro p : parametros) {
                if (p instanceof NodoParametro.Parametro par) {
                    if (par.esEstructura()) {
                        tabla.declararVariable(par.nombre(), par.tipoEstructura(),
                                false, 0, true, par.tipoEstructura(), List.of());
                    } else if (par.esArreglo()) {
                        tabla.declararVariable(par.nombre(), par.tipoPrimitivo(),
                                true, 1, false, null, List.of());
                    } else {
                        tabla.declararVariable(par.nombre(), par.tipoPrimitivo());
                    }
                }
            }
        }

        private void recogerYDeclararVariablesLocales(TablaSimbolos tabla,
                                                      List<NodoSentencia> sentencias,
                                                      List<VariableLocalC> acumuladas) {
            for (NodoSentencia s : sentencias) {
                switch (s) {
                    case NodoSentencia.DeclaracionVariable d -> {
                        tabla.declararVariable(d.nombre(), d.tipo());
                        acumuladas.add(new VariableLocalC(
                                TipoC.primitivoAC(d.tipo()), d.nombre()));
                    }
                    case NodoSentencia.DeclaracionArreglo d -> {
                        tabla.declararVariable(d.nombre(), d.tipo(),
                                true, 1, false, null, List.of(d.tamano()));
                        acumuladas.add(new VariableLocalC(
                                TipoC.primitivoAC(d.tipo()),
                                d.nombre() + "[" + d.tamano() + "]"));
                    }
                    case NodoSentencia.DeclaracionMatriz d -> {
                        tabla.declararVariable(d.nombre(), d.tipo(),
                                true, 2, false, null, List.of(d.filas(), d.columnas()));
                        acumuladas.add(new VariableLocalC(
                                TipoC.primitivoAC(d.tipo()),
                                d.nombre() + "[" + d.filas() + "][" + d.columnas() + "]"));
                    }
                    case NodoSentencia.DeclaracionEstructura d -> {
                        tabla.declararVariable(d.nombre(), d.tipoEstructura(),
                                false, 0, true, d.tipoEstructura(), List.of());
                        acumuladas.add(new VariableLocalC(d.tipoEstructura(), d.nombre()));
                    }
                    case NodoSentencia.Condicional c -> {
                        recogerYDeclararVariablesLocales(tabla, c.cuerpoSi(), acumuladas);
                        if (c.cuerpoSino() != null)
                            recogerYDeclararVariablesLocales(tabla, c.cuerpoSino(), acumuladas);
                        if (c.cuerpoContrario() != null)
                            recogerYDeclararVariablesLocales(tabla, c.cuerpoContrario(), acumuladas);
                    }
                    case NodoSentencia.CicloPara c -> {
                        tabla.declararVariable(c.nombreVariable(), c.tipoInicializacion());
                        acumuladas.add(new VariableLocalC(
                                TipoC.primitivoAC(c.tipoInicializacion()), c.nombreVariable()));
                        recogerYDeclararVariablesLocales(tabla, c.cuerpo(), acumuladas);
                    }
                    case NodoSentencia.CicloMientras c ->
                            recogerYDeclararVariablesLocales(tabla, c.cuerpo(), acumuladas);
                    case NodoSentencia.CicloHacerMientras c ->
                            recogerYDeclararVariablesLocales(tabla, c.cuerpo(), acumuladas);
                    case NodoSentencia.Elegir e -> {
                        for (NodoSentencia.CasoElegir caso : e.casos()) {
                            recogerYDeclararVariablesLocales(tabla, caso.cuerpo(), acumuladas);
                        }
                        if (e.siempre() != null) {
                            recogerYDeclararVariablesLocales(tabla, e.siempre().cuerpo(), acumuladas);
                        }
                    }
                    default -> { }
                }
            }
        }

        private static ParametroC aParametroC(NodoParametro.Parametro p) {
            if (p.esEstructura()) {
                return new ParametroC(p.tipoEstructura() + "*", p.nombre());
            }
            if (p.esArreglo()) {
                return new ParametroC(TipoC.primitivoAC(p.tipoPrimitivo()) + "*", p.nombre());
            }
            return new ParametroC(TipoC.primitivoAC(p.tipoPrimitivo()), p.nombre());
        }
    }

        private void declararVariablesLocales(TablaSimbolos tabla, List<NodoSentencia> sentencias) {
            for (NodoSentencia s : sentencias) {
                switch (s) {
                    case NodoSentencia.DeclaracionVariable d -> {
                        tabla.declararVariable(d.nombre(), d.tipo());
                    }
                    case NodoSentencia.DeclaracionArreglo d -> {
                        tabla.declararVariable(d.nombre(), d.tipo(),
                                true, 1, false, null, List.of(d.tamano()));
                    }
                    case NodoSentencia.DeclaracionMatriz d -> {
                        tabla.declararVariable(d.nombre(), d.tipo(),
                                true, 2, false, null, List.of(d.filas(), d.columnas()));
                    }
                    case NodoSentencia.DeclaracionEstructura d -> {
                        tabla.declararVariable(d.nombre(), d.tipoEstructura(),
                                false, 0, true, d.tipoEstructura(), List.of());
                    }
                    case NodoSentencia.Condicional c -> {
                        declararVariablesLocales(tabla, c.cuerpoSi());
                        if (c.cuerpoSino() != null) declararVariablesLocales(tabla, c.cuerpoSino());
                        if (c.cuerpoContrario() != null) declararVariablesLocales(tabla, c.cuerpoContrario());
                    }
                    case NodoSentencia.CicloPara c -> {
                        tabla.declararVariable(c.nombreVariable(), c.tipoInicializacion());
                        declararVariablesLocales(tabla, c.cuerpo());
                    }
                    case NodoSentencia.CicloMientras c -> declararVariablesLocales(tabla, c.cuerpo());
                    case NodoSentencia.CicloHacerMientras c -> declararVariablesLocales(tabla, c.cuerpo());
                    case NodoSentencia.Elegir e -> {
                        for (NodoSentencia.CasoElegir caso : e.casos()) {
                            declararVariablesLocales(tabla, caso.cuerpo());
                        }
                        if (e.siempre() != null) {
                            declararVariablesLocales(tabla, e.siempre().cuerpo());
                        }
                    }
                    default -> { /* no declara nada */ }
                }
            }
        }

        // Conversión de parámetros a C
        private static ParametroC aParametroC(NodoParametro.Parametro p) {
            if (p.esEstructura()) {
                return new ParametroC(p.tipoEstructura() + "*", p.nombre());
            }
            if (p.esArreglo()) {
                return new ParametroC(TipoC.primitivoAC(p.tipoPrimitivo()) + "*", p.nombre());
            }
            return new ParametroC(TipoC.primitivoAC(p.tipoPrimitivo()), p.nombre());
        }
    }
