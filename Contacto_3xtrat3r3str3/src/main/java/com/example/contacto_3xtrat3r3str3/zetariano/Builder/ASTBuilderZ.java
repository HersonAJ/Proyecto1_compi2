package com.example.contacto_3xtrat3r3str3.zetariano.Builder;

import com.example.contacto_3xtrat3r3str3.zetariano.nodo.*;
import com.example.zetariano.analizador.gramatica.ZParser;
import com.example.zetariano.analizador.gramatica.ZParserBaseVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

public class ASTBuilderZ extends ZParserBaseVisitor<NodoAST> {

    // PROGRAMA Y CLASE
    @Override
    public NodoAST visitPrograma(ZParser.ProgramaContext ctx) {
        NodoClase clase = (NodoClase) visit(ctx.claseDefinicion());
        return new NodoPrograma(linea(ctx), columna(ctx), clase);
    }

    @Override
    public NodoAST visitClaseDefinicion(ZParser.ClaseDefinicionContext ctx) {
        String nombre = ctx.ID().getText();

        List<NodoAtributo> atributos = new ArrayList<>();
        List<NodoConstructor> constructores = new ArrayList<>();
        List<NodoMetodo> metodos = new ArrayList<>();

        for (ZParser.MiembroClaseContext m : ctx.miembroClase()) {
            NodoAST nodo = visit(m);
            if (nodo instanceof NodoAtributo a) atributos.add(a);
            else if (nodo instanceof NodoConstructor c) constructores.add(c);
            else if (nodo instanceof NodoMetodo me) metodos.add(me);
        }

        return new NodoClase(linea(ctx), columna(ctx), nombre, atributos, constructores, metodos);
    }

    //regla contenedora: delega al unico hijo real (atributo, constructor o metodo)
    @Override
    public NodoAST visitMiembroClase(ZParser.MiembroClaseContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public NodoAST visitAtributo(ZParser.AtributoContext ctx) {
        String tipo = ctx.tipo().getText();
        String nombre = ctx.ID().getText();
        return new NodoAtributo(linea(ctx), columna(ctx), tipo, nombre);
    }

    @Override
    public NodoAST visitConstructor(ZParser.ConstructorContext ctx) {
        String nombre = ctx.ID().getText();
        List<NodoParametro> parametros = construirParametros(ctx.parametros());
        List<NodoSentencia> cuerpo = construirBloque(ctx.bloque());
        return new NodoConstructor(linea(ctx), columna(ctx), nombre, parametros, cuerpo);
    }

    @Override
    public NodoAST visitMetodo(ZParser.MetodoContext ctx) {
        String nombre = ctx.ID().getText();
        String tipoRetorno = ctx.VOID() != null ? null : ctx.tipo().getText();
        List<NodoParametro> parametros = construirParametros(ctx.parametros());
        List<NodoSentencia> cuerpo = construirBloque(ctx.bloque());
        return new NodoMetodo(linea(ctx), columna(ctx), nombre, parametros, tipoRetorno, cuerpo);
    }

    @Override
    public NodoAST visitParametro(ZParser.ParametroContext ctx) {
        return new NodoParametro(linea(ctx), columna(ctx), ctx.tipo().getText(), ctx.ID().getText());
    }

    private List<NodoParametro> construirParametros(ZParser.ParametrosContext ctx) {
        List<NodoParametro> parametros = new ArrayList<>();
        if (ctx == null) return parametros;
        for (ZParser.ParametroContext p : ctx.parametro()) {
            NodoAST nodo = visit(p);
            if (nodo instanceof NodoParametro param) parametros.add(param);
        }
        return parametros;
    }

    //regla contenedora, no se usa directamente (ver construirParametros)
    @Override
    public NodoAST visitParametros(ZParser.ParametrosContext ctx) {
        return null;
    }

    //regla contenedora, el tipo se lee siempre con ctx.tipo().getText() desde el padre
    @Override
    public NodoAST visitTipo(ZParser.TipoContext ctx) {
        return null;
    }

    // BLOQUES Y SENTENCIAS
    private List<NodoSentencia> construirBloque(ZParser.BloqueContext ctx) {
        List<NodoSentencia> cuerpo = new ArrayList<>();
        if (ctx == null) return cuerpo;
        for (ZParser.SentenciaContext s : ctx.sentencia()) {
            NodoAST nodo = visit(s);
            if (nodo instanceof NodoSentencia sentencia) cuerpo.add(sentencia);
        }
        return cuerpo;
    }

    //convierte 'sentenciaOBloque' (con o sin llaves) siempre a una lista uniforme
    private List<NodoSentencia> construirCuerpo(ZParser.SentenciaOBloqueContext ctx) {
        if (ctx.bloque() != null) {
            return construirBloque(ctx.bloque());
        }
        NodoAST nodo = visit(ctx.sentencia());
        List<NodoSentencia> cuerpo = new ArrayList<>();
        if (nodo instanceof NodoSentencia sentencia) cuerpo.add(sentencia);
        return cuerpo;
    }

    //regla contenedora
    @Override
    public NodoAST visitBloque(ZParser.BloqueContext ctx) {
        return null;
    }

    //regla contenedora: delega al unico hijo real
    @Override
    public NodoAST visitSentencia(ZParser.SentenciaContext ctx) {
        return visit(ctx.getChild(0));
    }

    //regla contenedora, ver construirCuerpo
    @Override
    public NodoAST visitSentenciaOBloque(ZParser.SentenciaOBloqueContext ctx) {
        return null;
    }

    // DECLARACION Y ASIGNACION
    @Override
    public NodoAST visitDeclaracion(ZParser.DeclaracionContext ctx) {
        String tipo = ctx.tipoDeclaracion().tipo().getText();
        int dimensiones = ctx.tipoDeclaracion().COR_IZQ().size();
        String nombre = ctx.ID().getText();
        NodoExpr inicializacion = ctx.inicializador() != null ? (NodoExpr) visit(ctx.inicializador()) : null;
        return new NodoSentencia.DeclaracionVariable(linea(ctx), columna(ctx), tipo, nombre, dimensiones, inicializacion);
    }

    @Override
    public NodoAST visitTipoDeclaracion(ZParser.TipoDeclaracionContext ctx) {
        return null;
    }

    @Override
    public NodoAST visitInicializador(ZParser.InicializadorContext ctx) {
        //forma '{ expr, expr, ... }' -> literal de lista (arreglos)
        if (ctx.LLAVE_IZQ() != null) {
            List<NodoExpr> elementos = new ArrayList<>();
            if (ctx.listaExpresiones() != null) {
                for (ZParser.ExpresionContext e : ctx.listaExpresiones().expresion()) {
                    NodoAST nodo = visit(e);
                    if (nodo instanceof NodoExpr expr) elementos.add(expr);
                }
            }
            return new NodoExpr.ListaLiteral(linea(ctx), columna(ctx), elementos);
        }
        //forma normal: cualquier expresion ('new Persona(...)', 'new int[5]', literal, etc.)
        return visit(ctx.expresion());
    }

    //regla contenedora, se recorre desde visitInicializador
    @Override
    public NodoAST visitListaExpresiones(ZParser.ListaExpresionesContext ctx) {
        return null;
    }

    @Override
    public NodoAST visitAsignacion(ZParser.AsignacionContext ctx) {
        NodoExpr destino = (NodoExpr) visit(ctx.expresion(0));
        String operador = ctx.getChild(1).getText(); // '=', '+=', '-=' o '*='
        NodoExpr valor = (NodoExpr) visit(ctx.expresion(1));
        return new NodoSentencia.Asignacion(linea(ctx), columna(ctx), operador, destino, valor);
    }

    @Override
    public NodoAST visitExpresionSentencia(ZParser.ExpresionSentenciaContext ctx) {
        NodoExpr expr = (NodoExpr) visit(ctx.expresion());
        return new NodoSentencia.ExpresionComoSentencia(linea(ctx), columna(ctx), expr);
    }

    // CONDICIONAL Y SWITCH
    @Override
    public NodoAST visitCondicional(ZParser.CondicionalContext ctx) {
        NodoExpr condicion = (NodoExpr) visit(ctx.expresion());
        List<NodoSentencia> cuerpoSi = construirCuerpo(ctx.sentenciaOBloque(0));
        List<NodoSentencia> cuerpoSino = ctx.ELSE() != null
                ? construirCuerpo(ctx.sentenciaOBloque(1))
                : null;
        return new NodoSentencia.Condicional(linea(ctx), columna(ctx), condicion, cuerpoSi, cuerpoSino);
    }

    @Override
    public NodoAST visitSwitchSentencia(ZParser.SwitchSentenciaContext ctx) {
        NodoExpr expresion = (NodoExpr) visit(ctx.expresion());

        List<NodoSentencia.CasoSwitch> casos = new ArrayList<>();
        for (ZParser.CasoSwitchContext c : ctx.casoSwitch()) {
            NodoAST nodo = visit(c);
            if (nodo instanceof NodoSentencia.CasoSwitch caso) casos.add(caso);
        }

        NodoSentencia.CasoDefault casoDefault = null;
        if (ctx.casoDefault() != null) {
            NodoAST nodo = visit(ctx.casoDefault());
            if (nodo instanceof NodoSentencia.CasoDefault cd) casoDefault = cd;
        }

        return new NodoSentencia.Switch(linea(ctx), columna(ctx), expresion, casos, casoDefault);
    }

    @Override
    public NodoAST visitCasoSwitch(ZParser.CasoSwitchContext ctx) {
        NodoExpr valor = (NodoExpr) visit(ctx.literal());
        List<NodoSentencia> cuerpo = new ArrayList<>();
        for (ZParser.SentenciaContext s : ctx.sentencia()) {
            NodoAST nodo = visit(s);
            if (nodo instanceof NodoSentencia sentencia) cuerpo.add(sentencia);
        }
        return new NodoSentencia.CasoSwitch(linea(ctx), columna(ctx), valor, cuerpo);
    }

    @Override
    public NodoAST visitCasoDefault(ZParser.CasoDefaultContext ctx) {
        List<NodoSentencia> cuerpo = new ArrayList<>();
        for (ZParser.SentenciaContext s : ctx.sentencia()) {
            NodoAST nodo = visit(s);
            if (nodo instanceof NodoSentencia sentencia) cuerpo.add(sentencia);
        }
        return new NodoSentencia.CasoDefault(linea(ctx), columna(ctx), cuerpo);
    }

    // CICLOS
    @Override
    public NodoAST visitCicloFor(ZParser.CicloForContext ctx) {
        NodoSentencia inicializacion = ctx.forInit() != null ? (NodoSentencia) visit(ctx.forInit()) : null;
        NodoExpr condicion = ctx.expresion() != null ? (NodoExpr) visit(ctx.expresion()) : null;
        NodoSentencia actualizacion = ctx.forActualizacion() != null ? (NodoSentencia) visit(ctx.forActualizacion()) : null;
        List<NodoSentencia> cuerpo = construirCuerpo(ctx.sentenciaOBloque());
        return new NodoSentencia.CicloPara(linea(ctx), columna(ctx), inicializacion, condicion, actualizacion, cuerpo);
    }

    @Override
    public NodoAST visitForInit(ZParser.ForInitContext ctx) {
        //alternativa 1: 'tipo ID = expresion' -> se modela como declaracion (dimensiones=0, no se declaran arreglos aqui)
        if (ctx.tipo() != null) {
            String tipo = ctx.tipo().getText();
            String nombre = ctx.ID().getText();
            NodoExpr valor = (NodoExpr) visit(ctx.expresion());
            return new NodoSentencia.DeclaracionVariable(linea(ctx), columna(ctx), tipo, nombre, 0, valor);
        }
        //alternativa 2: solo una expresion ( reusar una variable ya declarada afuera)
        NodoExpr expr = (NodoExpr) visit(ctx.expresion());
        return new NodoSentencia.ExpresionComoSentencia(linea(ctx), columna(ctx), expr);
    }

    @Override
    public NodoAST visitForActualizacion(ZParser.ForActualizacionContext ctx) {
        NodoExpr base = (NodoExpr) visit(ctx.expresion());
        //alternativa con '++' o '--' al final -> se modela como incremento/decremento postfijo
        if (ctx.INCREMENTO() != null || ctx.DECREMENTO() != null) {
            String operador = ctx.INCREMENTO() != null ? "++" : "--";
            NodoExpr incremento = new NodoExpr.IncrementoDecremento(linea(ctx), columna(ctx), operador, base, false);
            return new NodoSentencia.ExpresionComoSentencia(linea(ctx), columna(ctx), incremento);
        }
        //alternativa de solo expresion
        return new NodoSentencia.ExpresionComoSentencia(linea(ctx), columna(ctx), base);
    }

    @Override
    public NodoAST visitCicloWhile(ZParser.CicloWhileContext ctx) {
        NodoExpr condicion = (NodoExpr) visit(ctx.expresion());
        List<NodoSentencia> cuerpo = construirCuerpo(ctx.sentenciaOBloque());
        return new NodoSentencia.CicloMientras(linea(ctx), columna(ctx), condicion, cuerpo);
    }

    @Override
    public NodoAST visitCicloDoWhile(ZParser.CicloDoWhileContext ctx) {
        List<NodoSentencia> cuerpo = construirBloque(ctx.bloque());
        NodoExpr condicion = (NodoExpr) visit(ctx.expresion());
        return new NodoSentencia.CicloHacerMientras(linea(ctx), columna(ctx), cuerpo, condicion);
    }

    // RETORNO Y E/S
    @Override
    public NodoAST visitRetorno(ZParser.RetornoContext ctx) {
        NodoExpr valor = ctx.expresion() != null ? (NodoExpr) visit(ctx.expresion()) : null;
        return new NodoSentencia.Retorno(linea(ctx), columna(ctx), valor);
    }

    @Override
    public NodoAST visitImprimir(ZParser.ImprimirContext ctx) {
        boolean saltoDeLinea = ctx.PRINTLN() != null;
        NodoExpr expresion = (NodoExpr) visit(ctx.expresion());
        return new NodoSentencia.Imprimir(linea(ctx), columna(ctx), saltoDeLinea, expresion);
    }

    @Override
    public NodoAST visitLeer(ZParser.LeerContext ctx) {
        return new NodoSentencia.Leer(linea(ctx), columna(ctx));
    }

    @Override
    public NodoAST visitRomper(ZParser.RomperContext ctx) {
        return new NodoSentencia.Romper(linea(ctx), columna(ctx));
    }

    @Override
    public NodoAST visitContinuar(ZParser.ContinuarContext ctx) {
        return new NodoSentencia.Continuar(linea(ctx), columna(ctx));
    }

    // EXPRESIONES BASICAS
    @Override
    public NodoAST visitExprParentesis(ZParser.ExprParentesisContext ctx) {
        return visit(ctx.expresion());
    }

    @Override
    public NodoAST visitExprLiteral(ZParser.ExprLiteralContext ctx) {
        return visit(ctx.literal());
    }

    @Override
    public NodoAST visitExprIdentificador(ZParser.ExprIdentificadorContext ctx) {
        return new NodoExpr.Identificador(linea(ctx), columna(ctx), ctx.ID().getText());
    }

    @Override
    public NodoAST visitLiteral(ZParser.LiteralContext ctx) {
        if (ctx.ENTERO_LIT() != null) {
            return new NodoExpr.LiteralEntero(linea(ctx), columna(ctx), Integer.parseInt(ctx.ENTERO_LIT().getText()));
        }
        if (ctx.DECIMAL_LIT() != null) {
            return new NodoExpr.LiteralDecimal(linea(ctx), columna(ctx), Double.parseDouble(ctx.DECIMAL_LIT().getText()));
        }
        if (ctx.CADENA_LIT() != null) {
            String texto = ctx.CADENA_LIT().getText();
            return new NodoExpr.LiteralCadena(linea(ctx), columna(ctx), texto.substring(1, texto.length() - 1));
        }
        if (ctx.CARACTER_LIT() != null) {
            String texto = ctx.CARACTER_LIT().getText();
            return new NodoExpr.LiteralCaracter(linea(ctx), columna(ctx), texto.charAt(1));
        }
        if (ctx.TRUE() != null) {
            return new NodoExpr.LiteralBool(linea(ctx), columna(ctx), true);
        }
        if (ctx.FALSE() != null) {
            return new NodoExpr.LiteralBool(linea(ctx), columna(ctx), false);
        }
        //NULL_LIT
        return new NodoExpr.LiteralNulo(linea(ctx), columna(ctx));
    }

    // ACCESOS Y LLAMADAS
    @Override
    public NodoAST visitExprAccesoAtributo(ZParser.ExprAccesoAtributoContext ctx) {
        NodoExpr objeto = (NodoExpr) visit(ctx.expresion());
        return new NodoExpr.AccesoAtributo(linea(ctx), columna(ctx), objeto, ctx.ID().getText());
    }

    @Override
    public NodoAST visitExprAccesoArray(ZParser.ExprAccesoArrayContext ctx) {
        NodoExpr arreglo = (NodoExpr) visit(ctx.expresion(0));
        NodoExpr indice = (NodoExpr) visit(ctx.expresion(1));
        return new NodoExpr.AccesoArray(linea(ctx), columna(ctx), arreglo, indice);
    }

    @Override
    public NodoAST visitExprLlamadaFuncion(ZParser.ExprLlamadaFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        List<NodoExpr> argumentos = construirArgumentos(ctx.argumentos());
        return new NodoExpr.LlamadaFuncion(linea(ctx), columna(ctx), nombre, argumentos);
    }

    @Override
    public NodoAST visitExprLlamadaMetodo(ZParser.ExprLlamadaMetodoContext ctx) {
        NodoExpr objeto = (NodoExpr) visit(ctx.expresion());
        String nombre = ctx.ID().getText();
        List<NodoExpr> argumentos = construirArgumentos(ctx.argumentos());
        return new NodoExpr.LlamadaMetodo(linea(ctx), columna(ctx), objeto, nombre, argumentos);
    }

    @Override
    public NodoAST visitExprInstanciacionObjeto(ZParser.ExprInstanciacionObjetoContext ctx) {
        String tipoClase = ctx.ID().getText();
        List<NodoExpr> argumentos = construirArgumentos(ctx.argumentos());
        return new NodoExpr.InstanciaObjeto(linea(ctx), columna(ctx), tipoClase, argumentos);
    }

    @Override
    public NodoAST visitExprArregloNuevo(ZParser.ExprArregloNuevoContext ctx) {
        String tipoBase = ctx.tipo().getText();
        List<NodoExpr> dimensiones = new ArrayList<>();

        //recorremos los hijos manualmente porque cada '[ ]' puede traer o no una expresion de tamano
        //('new int[3][]' -> la segunda dimension queda como null)
        int totalHijos = ctx.getChildCount();
        for (int i = 0; i < totalHijos; i++) {
            ParseTree hijo = ctx.getChild(i);
            if (hijo instanceof ZParser.ExpresionContext expCtx) {
                dimensiones.add((NodoExpr) visit(expCtx));
            } else if (hijo instanceof TerminalNode tn && tn.getSymbol().getType() == ZParser.COR_IZQ) {
                ParseTree siguiente = ctx.getChild(i + 1);
                if (siguiente instanceof TerminalNode tnSig && tnSig.getSymbol().getType() == ZParser.COR_DER) {
                    dimensiones.add(null); //corchete vacio
                }
            }
        }

        return new NodoExpr.ArregloNuevo(linea(ctx), columna(ctx), tipoBase, dimensiones);
    }

    private List<NodoExpr> construirArgumentos(ZParser.ArgumentosContext ctx) {
        List<NodoExpr> argumentos = new ArrayList<>();
        if (ctx == null) return argumentos;
        for (ZParser.ExpresionContext e : ctx.expresion()) {
            NodoAST nodo = visit(e);
            if (nodo instanceof NodoExpr expr) argumentos.add(expr);
        }
        return argumentos;
    }

    //regla contenedora, va a construirArgumentos
    @Override
    public NodoAST visitArgumentos(ZParser.ArgumentosContext ctx) {
        return null;
    }

    // INCREMENTO/DECREMENTO Y OPERACIONES UNARIAS
    @Override
    public NodoAST visitExprPostIncrementoDecremento(ZParser.ExprPostIncrementoDecrementoContext ctx) {
        NodoExpr operando = (NodoExpr) visit(ctx.expresion());
        String operador = ctx.INCREMENTO() != null ? "++" : "--";
        return new NodoExpr.IncrementoDecremento(linea(ctx), columna(ctx), operador, operando, false);
    }

    @Override
    public NodoAST visitExprPreIncrementoDecremento(ZParser.ExprPreIncrementoDecrementoContext ctx) {
        NodoExpr operando = (NodoExpr) visit(ctx.expresion());
        String operador = ctx.INCREMENTO() != null ? "++" : "--";
        return new NodoExpr.IncrementoDecremento(linea(ctx), columna(ctx), operador, operando, true);
    }

    @Override
    public NodoAST visitExprUnaria(ZParser.ExprUnariaContext ctx) {
        String operador = ctx.MENOS() != null ? "-" : "!";
        NodoExpr operando = (NodoExpr) visit(ctx.expresion());
        return new NodoExpr.Unaria(linea(ctx), columna(ctx), operador, operando);
    }

    // OPERACIONES BINARIAS
    @Override
    public NodoAST visitExprMultiplicacion(ZParser.ExprMultiplicacionContext ctx) {
        return construirBinaria(ctx, ctx.expresion(0), ctx.expresion(1), ctx.getChild(1).getText());
    }

    @Override
    public NodoAST visitExprSumaResta(ZParser.ExprSumaRestaContext ctx) {
        return construirBinaria(ctx, ctx.expresion(0), ctx.expresion(1), ctx.getChild(1).getText());
    }

    @Override
    public NodoAST visitExprRelacional(ZParser.ExprRelacionalContext ctx) {
        return construirBinaria(ctx, ctx.expresion(0), ctx.expresion(1), ctx.getChild(1).getText());
    }

    @Override
    public NodoAST visitExprIgualdad(ZParser.ExprIgualdadContext ctx) {
        return construirBinaria(ctx, ctx.expresion(0), ctx.expresion(1), ctx.getChild(1).getText());
    }

    @Override
    public NodoAST visitExprAnd(ZParser.ExprAndContext ctx) {
        return construirBinaria(ctx, ctx.expresion(0), ctx.expresion(1), "&&");
    }

    @Override
    public NodoAST visitExprOr(ZParser.ExprOrContext ctx) {
        return construirBinaria(ctx, ctx.expresion(0), ctx.expresion(1), "||");
    }

    private NodoExpr construirBinaria(org.antlr.v4.runtime.ParserRuleContext ctx,
                                      ZParser.ExpresionContext izqCtx,
                                      ZParser.ExpresionContext derCtx,
                                      String operador) {
        NodoExpr izq = (NodoExpr) visit(izqCtx);
        NodoExpr der = (NodoExpr) visit(derCtx);
        return new NodoExpr.Binaria(linea(ctx), columna(ctx), operador, izq, der);
    }

    @Override
    public NodoAST visitExprTernaria(ZParser.ExprTernariaContext ctx) {
        NodoExpr condicion = (NodoExpr) visit(ctx.expresion(0));
        NodoExpr siVerdadero = (NodoExpr) visit(ctx.expresion(1));
        NodoExpr siFalso = (NodoExpr) visit(ctx.expresion(2));
        return new NodoExpr.Ternaria(linea(ctx), columna(ctx), condicion, siVerdadero, siFalso);
    }

    // HELPERS
    private int linea(org.antlr.v4.runtime.ParserRuleContext ctx) {
        return ctx.getStart().getLine();
    }

    private int columna(org.antlr.v4.runtime.ParserRuleContext ctx) {
        return ctx.getStart().getCharPositionInLine();
    }
}