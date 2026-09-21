package com.example.contacto_3xtrat3r3str3.piglatin.builder;

import com.example.contacto_3xtrat3r3str3.piglatin.nodo.*;
import com.example.piglatin.analizador.gramatica.PigParser;
import com.example.piglatin.analizador.gramatica.PigParserBaseVisitor;

import java.util.ArrayList;
import java.util.List;

public class ASTBuilderPig extends PigParserBaseVisitor<NodoAST> {

    // ============================================================
    // PROGRAMA
    // ============================================================

    @Override
    public NodoAST visitPrograma(PigParser.ProgramaContext ctx) {
        // Importaciones
        List<NodoImportacion> importaciones = new ArrayList<>();
        for (PigParser.ImportacionContext imp : ctx.importacion()) {
            NodoAST nodo = visit(imp);
            if (nodo instanceof NodoImportacion ni) importaciones.add(ni);
        }

        // Variables globales (opcional)
        List<NodoSentencia> variablesGlobales = new ArrayList<>();
        if (ctx.seccionVariables() != null) {
            for (PigParser.DeclaracionVarContext d : ctx.seccionVariables().declaracionVar()) {
                NodoAST nodo = visit(d);
                if (nodo instanceof NodoSentencia ns) variablesGlobales.add(ns);
            }
        }

        // Cuerpo del MAIOR
        List<NodoSentencia> cuerpoMain = new ArrayList<>();
        if (ctx.seccionMain() != null) {
            for (PigParser.SentenciaContext s : ctx.seccionMain().sentencia()) {
                NodoAST nodo = visit(s);
                if (nodo instanceof NodoSentencia ns) cuerpoMain.add(ns);
            }
        }

        return new NodoPrograma(linea(ctx), columna(ctx), importaciones, variablesGlobales, cuerpoMain);
    }

    // ============================================================
    // IMPORTACIONES
    // ============================================================

    @Override
    public NodoAST visitImportacion(PigParser.ImportacionContext ctx) {
        String ruta = ctx.rutaArchivo().getText();
        return new NodoImportacion(linea(ctx), columna(ctx), ruta);
    }

    @Override
    public NodoAST visitRutaArchivo(PigParser.RutaArchivoContext ctx) {
        return null; // regla contenedora, se lee desde visitImportacion
    }

    @Override
    public NodoAST visitTipoArchivo(PigParser.TipoArchivoContext ctx) {
        return null; // regla contenedora
    }

    @Override
    public NodoAST visitSeccionVariables(PigParser.SeccionVariablesContext ctx) {
        return null; // regla contenedora, se recorre desde visitPrograma
    }

    @Override
    public NodoAST visitSeccionMain(PigParser.SeccionMainContext ctx) {
        return null; // regla contenedora, se recorre desde visitPrograma
    }

    // ============================================================
    // DECLARACIONES
    // ============================================================

    @Override
    public NodoAST visitDeclaracionVar(PigParser.DeclaracionVarContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public NodoAST visitVariable(PigParser.VariableContext ctx) {
        String nombre = ctx.ID().getText();

        // Caso 1: tipo primitivo con inicialización opcional
        if (ctx.tipoPrimitivo() != null) {
            String tipo = ctx.tipoPrimitivo().getText();
            NodoExpr inicializacion = ctx.expr() != null ? (NodoExpr) visit(ctx.expr()) : null;
            return new NodoSentencia.DeclaracionVariable(linea(ctx), columna(ctx), tipo, nombre, inicializacion);
        }

        // Caso 2: expresión (por ejemplo, novus Persona(...))
        NodoExpr inicializacion = (NodoExpr) visit(ctx.expr());

        // Si es una instancia de objeto, extraer el tipo de ahí.
        String tipo = null;
        if (inicializacion instanceof NodoExpr.InstanciaObjeto inst) {
            tipo = inst.tipoClase();
        }

        return new NodoSentencia.DeclaracionVariable(linea(ctx), columna(ctx), tipo, nombre, inicializacion);
    }

    @Override
    public NodoAST visitTipoPrimitivo(PigParser.TipoPrimitivoContext ctx) {
        return null; // regla contenedora, se lee con .getText()
    }

    @Override
    public NodoAST visitTipo(PigParser.TipoContext ctx) {
        return null; // regla contenedora, se lee con .getText()
    }

    @Override
    public NodoAST visitArreglo(PigParser.ArregloContext ctx) {
        String tipo = ctx.tipo().getText();
        String nombre = ctx.ID().getText();
        int tamano = Integer.parseInt(ctx.INT().getText());

        List<NodoExpr> inicializacion = new ArrayList<>();
        if (ctx.listaExpr() != null) {
            for (PigParser.ExprContext e : ctx.listaExpr().expr()) {
                NodoAST nodo = visit(e);
                if (nodo instanceof NodoExpr ne) inicializacion.add(ne);
            }
        }

        return new NodoSentencia.DeclaracionArreglo(linea(ctx), columna(ctx), tipo, nombre, tamano, inicializacion);
    }

    @Override
    public NodoAST visitListaExpr(PigParser.ListaExprContext ctx) {
        return null; // regla contenedora
    }

    @Override
    public NodoAST visitStructInstancia(PigParser.StructInstanciaContext ctx) {
        String tipo = ctx.ID(0).getText();
        String nombre = ctx.ID(1).getText();

        List<NodoExpr> inicializacion = new ArrayList<>();
        if (ctx.literalStruct() != null && ctx.literalStruct().listaExpr() != null) {
            for (PigParser.ExprContext e : ctx.literalStruct().listaExpr().expr()) {
                NodoAST nodo = visit(e);
                if (nodo instanceof NodoExpr ne) inicializacion.add(ne);
            }
        }

        return new NodoSentencia.DeclaracionStruct(linea(ctx), columna(ctx), tipo, nombre, inicializacion);
    }

    @Override
    public NodoAST visitLlamadaMetodo(PigParser.LlamadaMetodoContext ctx) {
        NodoExpr objeto = (NodoExpr) visit(ctx.referencia());
        String nombre = ctx.ID().getText();
        List<NodoExpr> argumentos = construirArgumentos(ctx.listaArgumentos());
        return new NodoExpr.LlamadaMetodo(linea(ctx), columna(ctx), objeto, nombre, argumentos);
    }

    @Override
    public NodoAST visitLiteralStruct(PigParser.LiteralStructContext ctx) {
        return null; // regla contenedora
    }

    // ============================================================
    // SENTENCIAS
    // ============================================================

    @Override
    public NodoAST visitSentencia(PigParser.SentenciaContext ctx) {
        // Caso especial: llamada a metodo como sentencia
        if (ctx.llamadaMetodo() != null) {
            NodoExpr llamada = (NodoExpr) visit(ctx.llamadaMetodo());
            return new NodoSentencia.LlamadaMetodoSentencia(linea(ctx), columna(ctx), (NodoExpr.LlamadaMetodo) llamada);
        }
        // Caso general: delegar al hijo
        return visit(ctx.getChild(0));
    }

    @Override
    public NodoAST visitAsignacion(PigParser.AsignacionContext ctx) {
        NodoExpr destino = (NodoExpr) visit(ctx.referencia());
        NodoExpr valor = (NodoExpr) visit(ctx.expr());
        return new NodoSentencia.Asignacion(linea(ctx), columna(ctx), destino, valor);
    }

    // ============================================================
    // REFERENCIAS
    // ============================================================

    @Override
    public NodoAST visitReferenciaBase(PigParser.ReferenciaBaseContext ctx) {
        return new NodoExpr.Identificador(linea(ctx), columna(ctx), ctx.ID().getText());
    }

    @Override
    public NodoAST visitAccesoAtributo(PigParser.AccesoAtributoContext ctx) {
        NodoExpr base = (NodoExpr) visit(ctx.referencia());
        return new NodoExpr.AccesoAtributo(linea(ctx), columna(ctx), base, ctx.ID().getText());
    }

    @Override
    public NodoAST visitAccesoArray(PigParser.AccesoArrayContext ctx) {
        NodoExpr base = (NodoExpr) visit(ctx.referencia());
        NodoExpr indice = (NodoExpr) visit(ctx.expr());
        return new NodoExpr.AccesoArray(linea(ctx), columna(ctx), base, indice);
    }

    // ============================================================
    // INCREMENTO / DECREMENTO (sentencia)
    // ============================================================

    @Override
    public NodoAST visitIncrementoDecremento(PigParser.IncrementoDecrementoContext ctx) {
        NodoExpr operando = (NodoExpr) visit(ctx.referencia());
        String op = ctx.INC() != null ? "++" : "--";
        boolean prefijo = ctx.getChild(0) != ctx.referencia();
        return new NodoSentencia.IncrementoDecremento(linea(ctx), columna(ctx), op, operando, prefijo);
    }

    // ============================================================
    // CONDICIONAL
    // ============================================================

    @Override
    public NodoAST visitCondicional(PigParser.CondicionalContext ctx) {
        NodoExpr condicion = (NodoExpr) visit(ctx.expr());
        List<NodoSentencia> cuerpoSi = construirBloque(ctx.bloqueSentencias());

        List<NodoSentencia.RamaAliter> ramasAliter = new ArrayList<>();
        for (PigParser.RamaAliterContext r : ctx.ramaAliter()) {
            NodoExpr condAliter = (NodoExpr) visit(r.expr());
            List<NodoSentencia> cuerpo = construirBloque(r.bloqueSentencias());
            ramasAliter.add(new NodoSentencia.RamaAliter(linea(r), columna(r), condAliter, cuerpo));
        }

        List<NodoSentencia> cuerpoAliter = null;
        if (ctx.ramaElse() != null) {
            cuerpoAliter = construirBloque(ctx.ramaElse().bloqueSentencias());
        }

        return new NodoSentencia.Condicional(linea(ctx), columna(ctx), condicion, cuerpoSi, ramasAliter, cuerpoAliter);
    }

    @Override
    public NodoAST visitBloqueSentencias(PigParser.BloqueSentenciasContext ctx) {
        return null; // regla contenedora
    }

    @Override
    public NodoAST visitRamaAliter(PigParser.RamaAliterContext ctx) {
        return null; // regla contenedora, se recorre desde visitCondicional
    }

    @Override
    public NodoAST visitRamaElse(PigParser.RamaElseContext ctx) {
        return null; // regla contenedora
    }

    private List<NodoSentencia> construirBloque(PigParser.BloqueSentenciasContext ctx) {
        List<NodoSentencia> cuerpo = new ArrayList<>();
        if (ctx == null) return cuerpo;
        for (PigParser.SentenciaContext s : ctx.sentencia()) {
            NodoAST nodo = visit(s);
            if (nodo instanceof NodoSentencia ns) cuerpo.add(ns);
        }
        return cuerpo;
    }

    // ============================================================
    // CICLOS
    // ============================================================

    @Override
    public NodoAST visitCicloDum(PigParser.CicloDumContext ctx) {
        NodoExpr condicion = (NodoExpr) visit(ctx.expr());
        List<NodoSentencia> cuerpo = new ArrayList<>();
        for (PigParser.SentenciaContext s : ctx.sentencia()) {
            NodoAST nodo = visit(s);
            if (nodo instanceof NodoSentencia ns) cuerpo.add(ns);
        }
        return new NodoSentencia.CicloDum(linea(ctx), columna(ctx), condicion, cuerpo);
    }

    @Override
    public NodoAST visitCicloFacere(PigParser.CicloFacereContext ctx) {
        List<NodoSentencia> cuerpo = new ArrayList<>();
        for (PigParser.SentenciaContext s : ctx.sentencia()) {
            NodoAST nodo = visit(s);
            if (nodo instanceof NodoSentencia ns) cuerpo.add(ns);
        }
        NodoExpr condicion = (NodoExpr) visit(ctx.expr());
        return new NodoSentencia.CicloFacere(linea(ctx), columna(ctx), cuerpo, condicion);
    }

    @Override
    public NodoAST visitCicloPer(PigParser.CicloPerContext ctx) {
        NodoSentencia inicializacion = (NodoSentencia) visit(ctx.variable());
        NodoExpr condicion = (NodoExpr) visit(ctx.expr());
        NodoSentencia actualizacion = (NodoSentencia) visit(ctx.incremento());

        List<NodoSentencia> cuerpo = new ArrayList<>();
        for (PigParser.SentenciaContext s : ctx.sentencia()) {
            NodoAST nodo = visit(s);
            if (nodo instanceof NodoSentencia ns) cuerpo.add(ns);
        }

        return new NodoSentencia.CicloPer(linea(ctx), columna(ctx), inicializacion, condicion, actualizacion, cuerpo);
    }

    @Override
    public NodoAST visitIncremento(PigParser.IncrementoContext ctx) {
        NodoExpr operando = (NodoExpr) visit(ctx.referencia());
        if (ctx.INC() != null || ctx.DEC() != null) {
            String op = ctx.INC() != null ? "++" : "--";
            return new NodoSentencia.IncrementoDecremento(linea(ctx), columna(ctx), op, operando, false);
        }
        // referencia = expr
        NodoExpr valor = (NodoExpr) visit(ctx.expr());
        return new NodoSentencia.Asignacion(linea(ctx), columna(ctx), operando, valor);
    }

    // ============================================================
    // INTERRUPCION DE CICLO
    // ============================================================

    @Override
    public NodoAST visitInterrupcionCiclo(PigParser.InterrupcionCicloContext ctx) {
        String tipo = ctx.PERGE() != null ? "perge" : "interrumpe";
        return new NodoSentencia.InterrupcionCiclo(linea(ctx), columna(ctx), tipo);
    }

    // ============================================================
    // LECTURA / ESCRITURA
    // ============================================================

    @Override
    public NodoAST visitLectura(PigParser.LecturaContext ctx) {
        String variable = ctx.ID() != null ? ctx.ID().getText() : null;
        return new NodoSentencia.Lectura(linea(ctx), columna(ctx), variable);
    }

    @Override
    public NodoAST visitEscritura(PigParser.EscrituraContext ctx) {
        List<NodoExpr> valores = new ArrayList<>();
        for (PigParser.ExprContext e : ctx.expr()) {
            NodoAST nodo = visit(e);
            if (nodo instanceof NodoExpr ne) valores.add(ne);
        }
        return new NodoSentencia.Escritura(linea(ctx), columna(ctx), valores);
    }

    // ============================================================
    // LLAMADA A FUNCION
    // ============================================================

    @Override
    public NodoAST visitLlamadaFuncion(PigParser.LlamadaFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        List<NodoExpr> argumentos = construirArgumentos(ctx.listaArgumentos());
        return new NodoExpr.LlamadaFuncion(linea(ctx), columna(ctx), nombre, argumentos);
    }

    @Override
    public NodoAST visitListaArgumentos(PigParser.ListaArgumentosContext ctx) {
        return null; // regla contenedora
    }

    private List<NodoExpr> construirArgumentos(PigParser.ListaArgumentosContext ctx) {
        List<NodoExpr> argumentos = new ArrayList<>();
        if (ctx == null) return argumentos;
        for (PigParser.ExprContext e : ctx.expr()) {
            NodoAST nodo = visit(e);
            if (nodo instanceof NodoExpr ne) argumentos.add(ne);
        }
        return argumentos;
    }

    // ============================================================
    // EXPRESIONES
    // ============================================================

    @Override
    public NodoAST visitExprParentesis(PigParser.ExprParentesisContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public NodoAST visitExprEntero(PigParser.ExprEnteroContext ctx) {
        return new NodoExpr.LiteralEntero(linea(ctx), columna(ctx), Integer.parseInt(ctx.INT().getText()));
    }

    @Override
    public NodoAST visitExprDecimal(PigParser.ExprDecimalContext ctx) {
        return new NodoExpr.LiteralDecimal(linea(ctx), columna(ctx), Double.parseDouble(ctx.FLOAT().getText()));
    }

    @Override
    public NodoAST visitExprTexto(PigParser.ExprTextoContext ctx) {
        String texto = ctx.STRING().getText();
        return new NodoExpr.LiteralTexto(linea(ctx), columna(ctx), texto.substring(1, texto.length() - 1));
    }

    @Override
    public NodoAST visitExprCaracter(PigParser.ExprCaracterContext ctx) {
        String texto = ctx.CHAR().getText();
        return new NodoExpr.LiteralCaracter(linea(ctx), columna(ctx), texto.charAt(1));
    }

    @Override
    public NodoAST visitExprVerum(PigParser.ExprVerumContext ctx) {
        return new NodoExpr.LiteralBool(linea(ctx), columna(ctx), true);
    }

    @Override
    public NodoAST visitExprFalsus(PigParser.ExprFalsusContext ctx) {
        return new NodoExpr.LiteralBool(linea(ctx), columna(ctx), false);
    }

    @Override
    public NodoAST visitExprReferencia(PigParser.ExprReferenciaContext ctx) {
        return visit(ctx.referencia());
    }

    @Override
    public NodoAST visitExprIncDecPrefijo(PigParser.ExprIncDecPrefijoContext ctx) {
        NodoExpr operando = new NodoExpr.Identificador(linea(ctx), columna(ctx), ctx.ID().getText());
        String op = ctx.INC() != null ? "++" : "--";
        return new NodoExpr.IncrementoDecremento(linea(ctx), columna(ctx), op, operando, true);
    }

    @Override
    public NodoAST visitExprIncDecPostfijo(PigParser.ExprIncDecPostfijoContext ctx) {
        NodoExpr operando = new NodoExpr.Identificador(linea(ctx), columna(ctx), ctx.ID().getText());
        String op = ctx.INC() != null ? "++" : "--";
        return new NodoExpr.IncrementoDecremento(linea(ctx), columna(ctx), op, operando, false);
    }

    @Override
    public NodoAST visitExprMulDiv(PigParser.ExprMulDivContext ctx) {
        return construirBinaria(ctx, ctx.expr(0), ctx.expr(1), ctx.getChild(1).getText());
    }

    @Override
    public NodoAST visitExprSumaResta(PigParser.ExprSumaRestaContext ctx) {
        return construirBinaria(ctx, ctx.expr(0), ctx.expr(1), ctx.getChild(1).getText());
    }

    @Override
    public NodoAST visitExprRelacional(PigParser.ExprRelacionalContext ctx) {
        return construirBinaria(ctx, ctx.expr(0), ctx.expr(1), ctx.getChild(1).getText());
    }

    @Override
    public NodoAST visitExprIgualdad(PigParser.ExprIgualdadContext ctx) {
        return construirBinaria(ctx, ctx.expr(0), ctx.expr(1), ctx.getChild(1).getText());
    }

    @Override
    public NodoAST visitExprAnd(PigParser.ExprAndContext ctx) {
        return construirBinaria(ctx, ctx.expr(0), ctx.expr(1), "&&");
    }

    @Override
    public NodoAST visitExprOr(PigParser.ExprOrContext ctx) {
        return construirBinaria(ctx, ctx.expr(0), ctx.expr(1), "||");
    }

    @Override
    public NodoAST visitExprLiteralCompuesto(PigParser.ExprLiteralCompuestoContext ctx) {
        List<NodoExpr> elementos = new ArrayList<>();
        if (ctx.listaExpr() != null) {
            for (PigParser.ExprContext e : ctx.listaExpr().expr()) {
                NodoAST nodo = visit(e);
                if (nodo instanceof NodoExpr ne) elementos.add(ne);
            }
        }
        return new NodoExpr.ListaLiteral(linea(ctx), columna(ctx), elementos);
    }

    @Override
    public NodoAST visitExprLlamada(PigParser.ExprLlamadaContext ctx) {
        return visit(ctx.llamadaFuncion());
    }

    @Override
    public NodoAST visitExprLlamadaMetodo(PigParser.ExprLlamadaMetodoContext ctx) {
        NodoExpr objeto = (NodoExpr) visit(ctx.expr());
        String nombre = ctx.ID().getText();
        List<NodoExpr> argumentos = construirArgumentos(ctx.listaArgumentos());
        return new NodoExpr.LlamadaMetodo(linea(ctx), columna(ctx), objeto, nombre, argumentos);
    }

    @Override
    public NodoAST visitExprInstanciaObjeto(PigParser.ExprInstanciaObjetoContext ctx) {
        String tipo = ctx.ID().getText();
        List<NodoExpr> argumentos = construirArgumentos(ctx.listaArgumentos());
        return new NodoExpr.InstanciaObjeto(linea(ctx), columna(ctx), tipo, argumentos);
    }

    private NodoExpr construirBinaria(org.antlr.v4.runtime.ParserRuleContext ctx,
                                      PigParser.ExprContext izqCtx,
                                      PigParser.ExprContext derCtx,
                                      String operador) {
        NodoExpr izq = (NodoExpr) visit(izqCtx);
        NodoExpr der = (NodoExpr) visit(derCtx);
        return new NodoExpr.Binaria(linea(ctx), columna(ctx), operador, izq, der);
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private int linea(org.antlr.v4.runtime.ParserRuleContext ctx) {
        return ctx.getStart().getLine();
    }

    private int columna(org.antlr.v4.runtime.ParserRuleContext ctx) {
        return ctx.getStart().getCharPositionInLine();
    }
}