package com.example.contacto_3xtrat3r3str3.y.Builder;

import com.example.contacto_3xtrat3r3str3.y.ast.*;
import com.example.y.analizador.gramatica.YParser;
import com.example.y.analizador.gramatica.YParserBaseVisitor;

import java.util.ArrayList;
import java.util.List;

public class ASTBuilder extends YParserBaseVisitor<NodoAST> {

    //programa
    @Override
    public NodoAST visitPrograma(YParser.ProgramaContext ctx) {
        // Estructuras (opcional)
        List<NodoEstructura> estructuras = new ArrayList<>();
        if (ctx.seccionEstructuras() != null) {
            for (YParser.DefinicionEstructuraContext d : ctx.seccionEstructuras().definicionEstructura()) {
                NodoAST nodo = visit(d);
                if (nodo instanceof NodoEstructura) {
                    estructuras.add((NodoEstructura) nodo);
                }
            }
        }

        List<NodoFuncion> funciones = new ArrayList<>();
        if (ctx.seccionFunciones() != null) {
            for (YParser.DefinicionFuncionContext f : ctx.seccionFunciones().definicionFuncion()) {
                NodoAST nodo = visit(f);
                if (nodo instanceof NodoFuncion) {
                    funciones.add((NodoFuncion) nodo);
                }
            }
        }
        return new NodoPrograma.Programa(linea(ctx), columna(ctx), estructuras, funciones);
    }

    //estructuras
    @Override
    public NodoAST visitSeccionEstructuras(YParser.SeccionEstructurasContext ctx) {
        //esta regla no genera un nodo propio
        return null;
    }

    @Override
    public NodoAST visitDefinicionEstructura(YParser.DefinicionEstructuraContext ctx) {
        if (ctx.ID() == null) {
            return null;
        }

        List<NodoAtributo> atributos = new ArrayList<>();
        for (YParser.AtributoEstructuraContext a : ctx.atributoEstructura()) {
            NodoAST nodo = visit(a);
            if (nodo instanceof NodoAtributo) {
                atributos.add((NodoAtributo) nodo);
            }
        }
        return new NodoEstructura.Estructura(linea(ctx), columna(ctx), ctx.ID().getText(), atributos);
    }

    @Override
    public NodoAST visitAtributoEstructura(YParser.AtributoEstructuraContext ctx) {
        if (ctx.tipo() != null && !ctx.ID().isEmpty()) {
            String tipoPrimitivo = normalizarTipo(ctx.tipo().getText());
            String nombre = ctx.ID(0).getText();
            int tamanoArreglo = -1;

            if (ctx.ENTERO_LIT() != null) {
                try {
                    tamanoArreglo = Integer.parseInt(ctx.ENTERO_LIT().getText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }

            return new NodoAtributo.Atributo(linea(ctx), columna(ctx), tipoPrimitivo, null, nombre, tamanoArreglo);
        }

        if (ctx.ID().size() == 2) {
            return new NodoAtributo.Atributo(linea(ctx), columna(ctx), null, ctx.ID(0).getText(), ctx.ID(1).getText(), -1);
        }

        return null;
    }

    //funciones
    @Override
    public NodoAST visitSeccionFunciones(YParser.SeccionFuncionesContext ctx) {
        //regla contenedora no genera nodo propio
        return null;
    }

    @Override
    public NodoAST visitDefinicionFuncion(YParser.DefinicionFuncionContext ctx) {
        if (ctx.ID() == null) {
            return null;
        }

        String nombre = ctx.ID().getText();

        //tipo de retorno opcional
        String tipoRetorno = null;
        if (ctx.tipo() != null) {
            tipoRetorno = normalizarTipo(ctx.tipo().getText());
        }

        //tipo de retorno opcional
        List<NodoParametro> parametros = new ArrayList<>();
        if (ctx.parametros() != null) {
            for (YParser.ParametroContext p : ctx.parametros().parametro()) {
                NodoAST nodo = visit(p);
                if (nodo instanceof NodoParametro) {
                    parametros.add((NodoParametro) nodo);
                }
            }
        }

        //cuerpo de la funcion
        List<NodoSentencia> cuerpo = construirCuerpoDeFuncion(ctx.cuerpoFuncion());

        return new NodoFuncion.Funcion(linea(ctx), columna(ctx), nombre, parametros, tipoRetorno, cuerpo);
    }

    @Override
    public NodoAST visitParametro(YParser.ParametroContext ctx) {
        //caso: [] tipo ID -> arreglo por referencia
        if (ctx.COR_IZQ() != null && ctx.tipo() != null) {
            return new NodoParametro.Parametro(linea(ctx), columna(ctx),
                    normalizarTipo(ctx.tipo().getText()), null, ctx.ID(0).getText(), true, false);
        }

        //caso: tipo ID -> valor
        if (ctx.tipo() != null) {
            return new NodoParametro.Parametro(linea(ctx), columna(ctx),
                    normalizarTipo(ctx.tipo().getText()), null, ctx.ID(0).getText(), false, false);
        }

        //caso: {} Tipo ID -> estructura por referencia
        if (ctx.LLAVE_IZQ() != null && ctx.ID().size() == 2) {
            return new NodoParametro.Parametro(linea(ctx), columna(ctx),
                    null, ctx.ID(0).getText(), ctx.ID(1).getText(), false, true);
        }
        return null;
    }

    //cuerpo de funcion y bloques
    @Override
    public NodoAST visitCuerpoFuncion(YParser.CuerpoFuncionContext ctx) {
        return null;
    }

    @Override
    public NodoAST visitBloque(YParser.BloqueContext ctx) {
        return null;
    }

    private List<NodoSentencia> construirCuerpoDeFuncion(YParser.CuerpoFuncionContext ctx) {
        List<NodoSentencia> cuerpo = new ArrayList<>();
        if (ctx == null) {
            return cuerpo;
        }
        for (YParser.InstruccionContext i : ctx.instruccion()) {
            NodoAST nodo = visit(i);
            if (nodo instanceof NodoSentencia) {
                cuerpo.add((NodoSentencia) nodo);
            }
        }
        return  cuerpo;
    }

    private List<NodoSentencia> construirBloque(YParser.BloqueContext ctx) {
        List<NodoSentencia> cuerpo = new ArrayList<>();
        if (ctx == null) {
            return cuerpo;
        }
        for (YParser.InstruccionContext i : ctx.instruccion()) {
            NodoAST nodo = visit(i);
            if (nodo instanceof NodoSentencia) {
                cuerpo.add((NodoSentencia) nodo);
            }
        }
        return cuerpo;
    }

    //instrucciones
    @Override
    public NodoAST visitInstruccion(YParser.InstruccionContext ctx) {
        var hijo = ctx.getChild(0);
        if (hijo == null) {
            return null;
        }

        // Caso especial: una definicion de estructura dentro de una funcion
        if (hijo instanceof YParser.DefinicionEstructuraContext defCtx) {
            NodoAST nodo = visit(defCtx);
            if (nodo instanceof NodoEstructura.Estructura estructura) {
                return new NodoSentencia.DeclaracionEstructuraLocal(
                        linea(ctx), columna(ctx), estructura);
            }
            return null;
        }

        return visit(hijo);
    }

    //declaraciones
    @Override
    public NodoAST visitDeclaracion(YParser.DeclaracionContext ctx) {
        //caso 1: tipo ID (IGUAL expresion)? -> variable simple, sin corchetes
        if (ctx.tipo() != null && ctx.ID().size() == 1 && ctx.COR_IZQ().isEmpty()) {
            String tipo = normalizarTipo(ctx.tipo().getText());
            String nombre = ctx.ID(0).getText();
            NodoExpr inicializacion = ctx.expresion() != null ? (NodoExpr) visit(ctx.expresion()) : null;
            return new NodoSentencia.DeclaracionVariable(linea(ctx), columna(ctx), tipo, nombre, inicializacion);
        }

        //caso 2: tipo ID [ ENTERO_LIT ] (IGUAL { listaExpresiones })? -> arreglo 1D
        if (ctx.tipo() != null && ctx.COR_IZQ().size() == 1) {
            String tipo = normalizarTipo(ctx.tipo().getText());
            String nombre = ctx.ID(0).getText();
            int tamano = Integer.parseInt(ctx.ENTERO_LIT(0).getText());

            List<NodoExpr> inicializacion = new ArrayList<>();
            if (ctx.listaExpresiones() != null) {
                for (YParser.ExpresionContext e : ctx.listaExpresiones().expresion()) {
                    NodoAST nodo = visit(e);
                    if (nodo instanceof NodoExpr) {
                        inicializacion.add((NodoExpr) nodo);
                    }
                }
            }

            return new NodoSentencia.DeclaracionArreglo(linea(ctx), columna(ctx),tipo, nombre, tamano, inicializacion);
        }

        //caso 3: tipo ID [ ENTERO_LIT ] [ ENTERO_LIT ] -> matriz, sin inicializador
//caso 3: tipo ID [ ENTERO_LIT ] [ ENTERO_LIT ] -> matriz
        if (ctx.tipo() != null && ctx.COR_IZQ().size() == 2) {
            String tipo = normalizarTipo(ctx.tipo().getText());
            String nombre = ctx.ID(0).getText();
            int filas = Integer.parseInt(ctx.ENTERO_LIT(0).getText());
            int columnas = Integer.parseInt(ctx.ENTERO_LIT(1).getText());

            List<List<NodoExpr>> inicializacion = null;
            if (ctx.listaMatriz() != null) {
                inicializacion = new ArrayList<>();
                for (YParser.ListaExpresionesContext filaCtx : ctx.listaMatriz().listaExpresiones()) {
                    List<NodoExpr> fila = new ArrayList<>();
                    for (YParser.ExpresionContext e : filaCtx.expresion()) {
                        NodoAST nodo = visit(e);
                        if (nodo instanceof NodoExpr expr) {
                            fila.add(expr);
                        }
                    }
                    inicializacion.add(fila);
                }
            }

            return new NodoSentencia.DeclaracionMatriz(linea(ctx), columna(ctx),
                    tipo, nombre, filas, columnas, inicializacion);
        }

        //caso 4: ID ID (IGUAL { listaExpresiones })? -> instancia de estructura
        if (ctx.tipo() == null && ctx.ID().size() == 2) {
            String tipoEstructura = ctx.ID(0).getText();
            String nombre = ctx.ID(1).getText();

            List<NodoExpr> inicializacion = new ArrayList<>();
            if (ctx.listaExpresiones() != null) {
                for (YParser.ExpresionContext e : ctx.listaExpresiones().expresion()) {
                    NodoAST nodo = visit(e);
                    if (nodo instanceof NodoExpr) {
                        inicializacion.add((NodoExpr) nodo);
                    }
                }
            }
            return new NodoSentencia.DeclaracionEstructura(linea(ctx), columna(ctx), tipoEstructura, nombre, inicializacion);
        }
        return null;
    }

    @Override
    public NodoAST visitListaExpresiones(YParser.ListaExpresionesContext ctx) {
        //regla contenedora
        return null;
    }

    //asignaciones
    @Override
    public NodoAST visitAsignacion(YParser.AsignacionContext ctx) {
        NodoExpr destino = (NodoExpr) visit(ctx.accesoVariable());
        NodoExpr valor = (NodoExpr) visit(ctx.expresion());
        if (destino == null || valor == null) {
            return null;
        }
        return new NodoSentencia.Asignacion(linea(ctx), columna(ctx), destino, valor);
    }

    //incremento|decremento
    @Override
    public NodoAST visitIncrementoDecremento(YParser.IncrementoDecrementoContext ctx) {
        if (ctx.ID() == null) {
            return null;
        }

        String operador = ctx.INCREMENTO() != null ? "++" : "--";
        return new NodoSentencia.IncrementoDecremento(linea(ctx), columna(ctx), operador, ctx.ID().getText());
    }

    //instrucciones simples
    @Override
    public NodoAST visitRetorno(YParser.RetornoContext ctx) {
        NodoExpr valor = ctx.expresion() != null
                ? (NodoExpr) visit(ctx.expresion())
                : null;
        return new NodoSentencia.Retorno(linea(ctx), columna(ctx), valor);
    }

    @Override
    public NodoAST visitImprimir(YParser.ImprimirContext ctx) {
        NodoExpr valor = (NodoExpr) visit(ctx.expresion());
        if (valor == null) {
            return null;
        }
        return new NodoSentencia.Imprimir(linea(ctx), columna(ctx), valor);
    }

    @Override
    public NodoAST visitLeer(YParser.LeerContext ctx) {
        return new NodoSentencia.Leer(linea(ctx), columna(ctx));
    }

    @Override
    public NodoAST visitExprLeer(YParser.ExprLeerContext ctx) {
        return new NodoExpr.Leer(linea(ctx), columna(ctx));
    }
    @Override
    public NodoAST visitRomper(YParser.RomperContext ctx) {
        return new NodoSentencia.Romper(linea(ctx), columna(ctx));
    }

    @Override
    public NodoAST visitContinuar(YParser.ContinuarContext ctx) {
        return new NodoSentencia.Continuar(linea(ctx), columna(ctx));
    }

    //expresiones basicas que no dependen de otras
    @Override
    public NodoAST visitExprParentesis(YParser.ExprParentesisContext ctx) {
        return visit(ctx.expresion());
    }

    @Override
    public NodoAST visitExprLiteral(YParser.ExprLiteralContext ctx) {
        return visit(ctx.literal());
    }

    @Override
    public NodoAST visitExprAcceso(YParser.ExprAccesoContext ctx) {
        return visit(ctx.accesoVariable());
    }

    @Override
    public NodoAST visitLiteral(YParser.LiteralContext ctx) {
        if (ctx.ENTERO_LIT() != null) {
            return new NodoExpr.LiteralEntero(linea(ctx), columna(ctx), Integer.parseInt(ctx.ENTERO_LIT().getText()));
        }
        if (ctx.FLOTANTE_LIT() != null) {
            return new NodoExpr.LiteralFlotante(linea(ctx), columna(ctx), Double.parseDouble(ctx.FLOTANTE_LIT().getText()));
        }
        if (ctx.CADENA_LIT() != null) {
            String texto = ctx.CADENA_LIT().getText();
            //quitar las comillas dobles
            return new NodoExpr.LiteralCadena(linea(ctx), columna(ctx), texto.substring(1, texto.length() -1));
        }
        if (ctx.CARACTER_LIT() != null) {
            String texto = ctx.CARACTER_LIT().getText();
            //quitar las comillas simples
            return new NodoExpr.LiteralCaracter(linea(ctx),columna(ctx), texto.charAt(1));
        }
        if (ctx.VERDADERO() != null) {
            return new NodoExpr.LiteralBool(linea(ctx), columna(ctx), true);
        }
        if (ctx.FALSO() != null) {
            return new NodoExpr.LiteralBool(linea(ctx), columna(ctx), false);
        }
        return null;
    }

    //arreglos, atributos, identificadores
    @Override
    public NodoAST visitAccesoVariable(YParser.AccesoVariableContext ctx) {
        //base el primer id siempre esta presente
        NodoExpr base = new NodoExpr.Identificador(linea(ctx), columna(ctx), ctx.ID(0).getText());

        //recorrer los accesos encadenados .atributo o [indice]
        int totalHijos = ctx.getChildCount();
        for (int i  = 1; i < totalHijos; i++) {
            org.antlr.v4.runtime.tree.ParseTree hijo = ctx.getChild(i);

            // atributo
            if (hijo instanceof org.antlr.v4.runtime.tree.TerminalNode tn) {
                if (tn.getSymbol().getType() == YParser.PUNTO) {
                    //el siguiente hijo es el id del atributo
                    org.antlr.v4.runtime.tree.ParseTree sig = ctx.getChild(i + 1);
                    if (sig instanceof org.antlr.v4.runtime.tree.TerminalNode idAttr) {
                        base = new NodoExpr.AccesoAtributo(linea(ctx), columna(ctx), base, idAttr.getText());
                        i++;
                    }
                }
            }
            //[indice]
            else if (hijo instanceof YParser.ExpresionContext expCtx) {
                NodoExpr indice = (NodoExpr) visit(expCtx);
                if (indice != null) {
                    base = new NodoExpr.AccesoArray(linea(ctx), columna(ctx), base, indice);
                }
            }
        }
        return base;
    }

    //expresiones compuestas
    @Override
    public NodoAST visitExprSumaResta(YParser.ExprSumaRestaContext ctx) {
        return construirBinaria(ctx, ctx.expresion(0), ctx.expresion(1), ctx.getChild(1).getText());
    }

    @Override
    public NodoAST visitExprMultiplicacion(YParser.ExprMultiplicacionContext ctx) {
        return construirBinaria(ctx, ctx.expresion(0), ctx.expresion(1), ctx.getChild(1).getText());
    }

    @Override
    public NodoAST visitExprRelacional(YParser.ExprRelacionalContext ctx) {
        return construirBinaria(ctx, ctx.expresion(0), ctx.expresion(1), ctx.getChild(1).getText());
    }

    @Override
    public NodoAST visitExprIgualdad(YParser.ExprIgualdadContext ctx) {
        return construirBinaria(ctx, ctx.expresion(0), ctx.expresion(1), ctx.getChild(1).getText());
    }

    @Override
    public NodoAST visitExprAnd(YParser.ExprAndContext ctx) {
        return construirBinaria(ctx, ctx.expresion(0), ctx.expresion(1), "&&");
    }

    @Override
    public NodoAST visitExprOr(YParser.ExprOrContext ctx) {
        return construirBinaria(ctx,ctx.expresion(0), ctx.expresion(1), "||");
    }

    //helper para la construccion
    private NodoExpr construirBinaria(org.antlr.v4.runtime.ParserRuleContext ctx,
                                      YParser.ExpresionContext izqCtx,
                                      YParser.ExpresionContext derCtx,
                                      String operador) {
        if (izqCtx == null || derCtx == null) {
            return null;
        }
        NodoExpr izq = (NodoExpr) visit(izqCtx);
        NodoExpr der = (NodoExpr) visit(derCtx);
        if (izq == null || der == null) {
            return  null;
        }
        return new NodoExpr.Binaria(linea(ctx), columna(ctx), operador, izq, der);
    }

    @Override
    public NodoAST visitExprUnaria(YParser.ExprUnariaContext ctx) {
        //puede ser -expresion, !expresion
        String operador;
        if (ctx.MENOS() != null) {
            operador = "-";
        } else if (ctx.NOT() != null) {
            operador = "!";
        } else {
            return null;
        }

        NodoExpr operando = (NodoExpr) visit(ctx.expresion());
        if (operando == null) {
            return null;
        }
        return new NodoExpr.Unaria(linea(ctx), columna(ctx), operador, operando, true);
    }

    @Override
    public NodoAST visitExprLlamadaFuncion(YParser.ExprLlamadaFuncionContext ctx) {
        if (ctx.ID() == null) {
            return null;
        }
        List<NodoExpr> argumentos = new ArrayList<>();
        if (ctx.argumentos() != null) {
            for (YParser.ExpresionContext e : ctx.argumentos().expresion()) {
                NodoAST nodo = visit(e);
                if (nodo instanceof NodoExpr) {
                    argumentos.add((NodoExpr) nodo);
                }
            }
        }
        return new NodoExpr.LlamadaFuncion(linea(ctx), columna(ctx), ctx.ID().getText(), argumentos);
    }

    @Override
    public NodoAST visitArgumentos(YParser.ArgumentosContext ctx) {
        //regla conteneroda, no genera nodo propio
        return null;
    }

    //condicional
    @Override
    public NodoAST visitCondicional(YParser.CondicionalContext ctx) {
        //extraer la condicion principal
        NodoExpr condicionSi = (NodoExpr) visit(ctx.expresion(0));
        List<NodoSentencia> cuerpoSi = construirBloque(ctx.bloque(0));

        //sino opcional
        NodoExpr condicionSino = null;
        List<NodoSentencia> cuerpoSino = null;
        if (ctx.SINO() != null) {
            condicionSino = (NodoExpr) visit(ctx.expresion(1));
            cuerpoSino = construirBloque(ctx.bloque(1));
        }

        //contrario opcional
        List<NodoSentencia> cuerpoContrario = null;
        if (ctx.CONTRARIO() != null) {
            int indiceBloque = ctx.SINO() != null ? 2 : 1;
            cuerpoContrario = construirBloque(ctx.bloque(indiceBloque));
        }

        return new NodoSentencia.Condicional(linea(ctx), columna(ctx), condicionSi, cuerpoSi, condicionSino, cuerpoSino, cuerpoContrario);
    }

    //elegir
    @Override
    public NodoAST visitElegir(YParser.ElegirContext ctx) {
        NodoExpr expresion = (NodoExpr) visit(ctx.expresion());

        List<NodoSentencia.CasoElegir> casos = new ArrayList<>();
        for (YParser.CasoContext c : ctx.caso()) {
            NodoAST nodo = visit(c);
            if (nodo instanceof NodoSentencia.CasoElegir) {
                casos.add((NodoSentencia.CasoElegir) nodo);
            }
        }

        NodoSentencia.SiempreElegir siempre = null;
        if (ctx.siempre() != null) {
            NodoAST nodo = visit(ctx.siempre());
            if (nodo instanceof NodoSentencia.SiempreElegir) {
                siempre = (NodoSentencia.SiempreElegir) nodo;
            }
        }

        return new NodoSentencia.Elegir(linea(ctx), columna(ctx), expresion, casos, siempre);
    }

    @Override
    public NodoAST visitCaso(YParser.CasoContext ctx) {
        NodoExpr valor = (NodoExpr) visit(ctx.literal());
        if (valor == null) {
            return null;
        }
        List<NodoSentencia> cuerpo = construirBloque(ctx.bloque());
        return new NodoSentencia.CasoElegir(linea(ctx), columna(ctx), valor, cuerpo);
    }

    @Override
    public NodoAST visitSiempre(YParser.SiempreContext ctx) {
        List<NodoSentencia> cuerpo = construirBloque(ctx.bloque());
        return new NodoSentencia.SiempreElegir(linea(ctx), columna(ctx), cuerpo);
    }

    //ciclos
    @Override
    public NodoAST visitCicloPara(YParser.CicloParaContext ctx) {
        String tipo = normalizarTipo(ctx.inicializacionPara().tipo().getText());
        String nombre = ctx.inicializacionPara().ID().getText();
        NodoExpr valorInicial = (NodoExpr) visit(ctx.inicializacionPara().expresion());

        NodoExpr condicion = (NodoExpr) visit(ctx.condicionPara().expresion());

        String operadorActualizacion = ctx.actualizacionPara().INCREMENTO() != null ? "++" : "--";

        List<NodoSentencia> cuerpo = construirBloque(ctx.bloque());

        return new NodoSentencia.CicloPara(linea(ctx), columna(ctx), tipo, nombre, valorInicial, condicion, operadorActualizacion, cuerpo);
    }

    @Override
    public NodoAST visitInicializacionPara(YParser.InicializacionParaContext ctx) {
        return null;
    }

    @Override
    public NodoAST visitCondicionPara(YParser.CondicionParaContext ctx) {
        return null;
    }

    @Override
    public NodoAST visitActualizacionPara(YParser.ActualizacionParaContext ctx) {
        return null;
    }

    @Override
    public NodoAST visitCicloMientras(YParser.CicloMientrasContext ctx) {
        NodoExpr condicion = (NodoExpr) visit(ctx.expresion());
        List<NodoSentencia> cuerpo = construirBloque(ctx.bloque());
        return new NodoSentencia.CicloMientras(linea(ctx), columna(ctx), condicion, cuerpo);
    }

    @Override
    public NodoAST visitCicloHacerMientras(YParser.CicloHacerMientrasContext ctx) {
        List<NodoSentencia> cuerpo = construirBloque(ctx.bloque());
        NodoExpr condicion = (NodoExpr) visit(ctx.expresion());
        return new NodoSentencia.CicloHacerMientras(linea(ctx), columna(ctx), cuerpo, condicion);
    }
//helper
    protected int linea(org.antlr.v4.runtime.ParserRuleContext ctx) {
        return ctx.getStart().getLine();
    }

    protected int columna(org.antlr.v4.runtime.ParserRuleContext ctx) {
        return ctx.getStart().getCharPositionInLine();
    }

    protected String normalizarTipo(String texto) {
        return texto;
    }
}
