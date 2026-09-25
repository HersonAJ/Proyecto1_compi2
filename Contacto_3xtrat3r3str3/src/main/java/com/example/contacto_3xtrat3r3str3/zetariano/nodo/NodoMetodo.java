package com.example.contacto_3xtrat3r3str3.zetariano.nodo;

import com.example.contacto_3xtrat3r3str3.c3d_v2.GestorCodigoIntermedio;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.FuncionC;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.ParametroC;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.VariableLocalC;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.z.ContextoTraduccionZ;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.z.TipoCZ;
import com.example.contacto_3xtrat3r3str3.zetariano.semantica.TablaSimbolosZ;

import java.util.ArrayList;
import java.util.List;

public record NodoMetodo(int linea, int columna, String nombre,
                         List<NodoParametroZ> parametros,
                         String tipoRetorno,
                         List<NodoSentencia> cuerpo) implements NodoAST {

    public FuncionC aFuncionC(TablaSimbolosZ tabla, String nombreClase) {
        GestorCodigoIntermedio gestor = new GestorCodigoIntermedio();

        String nombreC = construirNombreC(nombreClase);
        ContextoTraduccionZ ctx = new ContextoTraduccionZ(gestor, tabla, nombreClase, nombreC);

        tabla.entrarScope(nombreC);
        try {
            for (NodoParametroZ p : parametros) {
                tabla.declararVariable(p.nombre(), p.tipo());
            }

            List<VariableLocalC> locales = new ArrayList<>();
            declararLocales(tabla, cuerpo, locales);

            for (NodoSentencia s : cuerpo) {
                s.aCodigoIntermedio(ctx);
            }

            List<ParametroC> paramsC = new ArrayList<>();
            paramsC.add(new ParametroC("struct " + nombreClase + "*", "this"));
            for (NodoParametroZ p : parametros) {
                paramsC.add(aParametroC(p));
            }

            String tipoRetornoC = (tipoRetorno == null)
                    ? "void"
                    : tipoRetornoC(tipoRetorno);

            List<com.example.contacto_3xtrat3r3str3.c3d_v2.Cuarteta> cuartetas =
                    gestor.getCuartetas();
            List<String> tiposTemporales = gestor.getContador().getTiposTemporales();

            return new FuncionC(tipoRetornoC, nombreC, paramsC, locales,
                    cuartetas, tiposTemporales);
        } finally {
            tabla.salirScope();
        }
    }

    private String construirNombreC(String nombreClase) {
        StringBuilder sb = new StringBuilder();
        sb.append(nombreClase).append('_').append(nombre);
        for (NodoParametroZ p : parametros) {
            sb.append('_').append(p.tipo());
        }
        return sb.toString();
    }

    private static String tipoRetornoC(String tipoZ) {
        if (TipoCZ.esPrimitivo(tipoZ)) {
            return TipoCZ.baseValorAC(tipoZ);
        }
        return "struct " + tipoZ + "*";
    }

    private static ParametroC aParametroC(NodoParametroZ p) {
        String tipoC;
        if (TipoCZ.esPrimitivo(p.tipo())) {
            tipoC = TipoCZ.baseValorAC(p.tipo());
        } else {
            tipoC = "struct " + p.tipo() + "*";
        }
        return new ParametroC(tipoC, p.nombre());
    }

    private void declararLocales(TablaSimbolosZ tabla,
                                 List<NodoSentencia> sentencias,
                                 List<VariableLocalC> acumuladas) {
        declararLocales(tabla, sentencias, acumuladas, new java.util.HashSet<>());
    }

    private void declararLocales(TablaSimbolosZ tabla,
                                 List<NodoSentencia> sentencias,
                                 List<VariableLocalC> acumuladas,
                                 java.util.Set<String> yaDeclarados) {
        for (NodoSentencia s : sentencias) {
            switch (s) {
                case NodoSentencia.DeclaracionVariable d -> {
                    if (yaDeclarados.contains(d.nombre())) continue;
                    yaDeclarados.add(d.nombre());

                    if (d.dimensiones() == 0) {
                        // Variable simple u objeto.
                        tabla.declararVariable(d.nombre(), d.tipo(), 0);
                        acumuladas.add(new VariableLocalC(
                                TipoCZ.baseAC(d.tipo(), !TipoCZ.esPrimitivo(d.tipo())),
                                d.nombre()));
                    } else {
                        // Arreglo: en C es puntero. La reserva real viene del 'new'.
                        tabla.declararVariable(d.nombre(), d.tipo(), d.dimensiones());
                        String tipoBaseC = TipoCZ.esPrimitivo(d.tipo())
                                ? TipoCZ.baseValorAC(d.tipo())
                                : "struct " + d.tipo();
                        String tipoC = tipoBaseC + "*".repeat(d.dimensiones());
                        acumuladas.add(new VariableLocalC(tipoC, d.nombre()));
                    }
                }
                case NodoSentencia.Condicional c -> {
                    declararLocales(tabla, c.cuerpoSi(), acumuladas, yaDeclarados);
                    if (c.cuerpoSino() != null) {
                        declararLocales(tabla, c.cuerpoSino(), acumuladas, yaDeclarados);
                    }
                }
                case NodoSentencia.CicloPara c -> {
                    if (c.inicializacion() instanceof NodoSentencia.DeclaracionVariable d) {
                        if (!yaDeclarados.contains(d.nombre())) {
                            yaDeclarados.add(d.nombre());
                            if (d.dimensiones() == 0) {
                                tabla.declararVariable(d.nombre(), d.tipo(), 0);
                                acumuladas.add(new VariableLocalC(
                                        TipoCZ.baseAC(d.tipo(), !TipoCZ.esPrimitivo(d.tipo())),
                                        d.nombre()));
                            } else {
                                tabla.declararVariable(d.nombre(), d.tipo(), d.dimensiones());
                                String tipoBaseC = TipoCZ.esPrimitivo(d.tipo())
                                        ? TipoCZ.baseValorAC(d.tipo())
                                        : "struct " + d.tipo();
                                String tipoC = tipoBaseC + "*".repeat(d.dimensiones());
                                acumuladas.add(new VariableLocalC(tipoC, d.nombre()));
                            }
                        }
                    }
                    declararLocales(tabla, c.cuerpo(), acumuladas, yaDeclarados);
                }
                case NodoSentencia.CicloMientras c ->
                        declararLocales(tabla, c.cuerpo(), acumuladas, yaDeclarados);
                case NodoSentencia.CicloHacerMientras c ->
                        declararLocales(tabla, c.cuerpo(), acumuladas, yaDeclarados);
                case NodoSentencia.Switch sw -> {
                    for (NodoSentencia.CasoSwitch caso : sw.casos()) {
                        declararLocales(tabla, caso.cuerpo(), acumuladas, yaDeclarados);
                    }
                    if (sw.casoDefault() != null) {
                        declararLocales(tabla, sw.casoDefault().cuerpo(), acumuladas, yaDeclarados);
                    }
                }
                default -> {
                }
            }
        }
    }
}