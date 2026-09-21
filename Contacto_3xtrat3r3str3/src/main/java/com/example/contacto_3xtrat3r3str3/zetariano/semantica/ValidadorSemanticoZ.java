package com.example.contacto_3xtrat3r3str3.zetariano.semantica;

import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.*;

import java.util.ArrayList;
import java.util.List;

public class ValidadorSemanticoZ {

    private final TablaSimbolosZ tabla = new TablaSimbolosZ();
    private final List<ErrorSemantico> errores = new ArrayList<>();

    private final ValidadorDeclaracionesZ declaraciones = new ValidadorDeclaracionesZ(tabla, errores);
    private final ValidadorAlcanceZ alcance = new ValidadorAlcanceZ(tabla, errores);
    private final ValidadorFlujoZ flujo = new ValidadorFlujoZ(errores);
    private final ValidadorTiposZ tipos = new ValidadorTiposZ(tabla, errores);

    private String tipoRetornoActual;

    public List<ErrorSemantico> analizar(NodoPrograma programa) {
        NodoClase clase = programa.clase();

        declaraciones.declararClase(clase);
        declaraciones.declararAtributos(clase);
        declaraciones.declararConstructores(clase);
        declaraciones.declararMetodos(clase);

        for (NodoAtributoZ a : clase.atributos()) {
            alcance.validarTipoDeclarado(a.tipo(), a.linea(), a.columna());
        }

        // LOOP 1: CONSTRUCTORES
        for (NodoConstructor c : clase.constructores()) {

            validarTiposDeParametros(c.parametros());
            tipoRetornoActual = null;
            procesarCuerpoConParametros(c.parametros(), c.cuerpo());
        }

        // LOOP 2: METODOS
        for (NodoMetodo m : clase.metodos()) {
            validarTiposDeParametros(m.parametros());
            tipoRetornoActual = m.tipoRetorno();
            procesarCuerpoConParametros(m.parametros(), m.cuerpo());
            flujo.validarRetornoGarantizado(m.tipoRetorno(), m.cuerpo(), m.linea(), m.columna());
        }

        return errores;
    }


    private void procesarCuerpoConParametros(List<NodoParametroZ> parametros, List<NodoSentencia> cuerpo) {
        tabla.entrarScope("miembro");
        declaraciones.declararParametrosEnScope(parametros);
        procesarBloque(cuerpo);
        tabla.salirScope();
    }

    private void procesarBloque(List<NodoSentencia> bloque) {
        flujo.validarCodigoInalcanzable(bloque);
        for (NodoSentencia s : bloque) procesarSentencia(s);
    }

    private void procesarSentencia(NodoSentencia s) {
        switch (s.tipoNodo()) {
            case DECLARACION_VARIABLE -> {
                NodoSentencia.DeclaracionVariable d = (NodoSentencia.DeclaracionVariable) s;
                alcance.validarTipoDeclarado(d.tipo(), d.linea(), d.columna());
                alcance.resolverExpresion(d.inicializacion());
                tipos.validarInicializacion(d.tipo(), d.dimensiones(), d.inicializacion());
                declaraciones.declararVariable(d);
            }
            case ASIGNACION -> {
                NodoSentencia.Asignacion a = (NodoSentencia.Asignacion) s;
                alcance.resolverExpresion(a.destino());
                alcance.resolverExpresion(a.valor());
                tipos.validarAsignacion(a.operador(), a.destino(), a.valor());
            }
            case EXPRESION_COMO_SENTENCIA -> {
                NodoExpr expr = ((NodoSentencia.ExpresionComoSentencia) s).expresion();
                alcance.resolverExpresion(expr);
                tipos.tipoDeExpresion(expr);
            }

            case CONDICIONAL -> procesarCondicional((NodoSentencia.Condicional) s);
            case SWITCH -> procesarSwitch((NodoSentencia.Switch) s);
            case CICLO_PARA -> procesarCicloPara((NodoSentencia.CicloPara) s);
            case CICLO_MIENTRAS -> procesarCicloMientras((NodoSentencia.CicloMientras) s);
            case CICLO_HACER_MIENTRAS -> procesarCicloHacerMientras((NodoSentencia.CicloHacerMientras) s);

            case RETORNO -> {
                NodoSentencia.Retorno r = (NodoSentencia.Retorno) s;
                alcance.resolverExpresion(r.valor());
                tipos.validarRetorno(r, tipoRetornoActual);
            }
            case IMPRIMIR -> {
                NodoExpr expr = ((NodoSentencia.Imprimir) s).expresion();
                alcance.resolverExpresion(expr);
                tipos.tipoDeExpresion(expr);
            }
            case LEER -> { }
            case ROMPER -> flujo.validarRomper((NodoSentencia.Romper) s);
            case CONTINUAR -> flujo.validarContinuar((NodoSentencia.Continuar) s);

            case CASO_SWITCH, CASO_DEFAULT -> { }
        }
    }

    private void procesarCondicional(NodoSentencia.Condicional c) {
        alcance.resolverExpresion(c.condicion());
        tipos.validarCondicionBooleana(c.condicion());
        tabla.entrarScope("si");
        procesarBloque(c.cuerpoSi());
        tabla.salirScope();

        if (c.cuerpoSino() != null) {
            tabla.entrarScope("sino");
            procesarBloque(c.cuerpoSino());
            tabla.salirScope();
        }
    }

    private void procesarSwitch(NodoSentencia.Switch sw) {
        alcance.resolverExpresion(sw.expresion());
        tipos.tipoDeExpresion(sw.expresion());
        tipos.validarTipoSwitch(sw.expresion());
        flujo.entrarSwitch();

        for (NodoSentencia.CasoSwitch caso : sw.casos()) {
            tabla.entrarScope("caso");
            procesarBloque(caso.cuerpo());
            tabla.salirScope();
        }
        if (sw.casoDefault() != null) {
            tabla.entrarScope("default");
            procesarBloque(sw.casoDefault().cuerpo());
            tabla.salirScope();
        }

        flujo.salirSwitch();
    }

    private void procesarCicloPara(NodoSentencia.CicloPara c) {
        tabla.entrarScope("para");
        if (c.inicializacion() != null) procesarSentencia(c.inicializacion());
        alcance.resolverExpresion(c.condicion());
        tipos.validarCondicionBooleana(c.condicion());

        flujo.entrarCiclo();
        procesarBloque(c.cuerpo());
        if (c.actualizacion() != null) procesarSentencia(c.actualizacion());
        flujo.salirCiclo();

        tabla.salirScope();
    }

    private void procesarCicloMientras(NodoSentencia.CicloMientras c) {
        alcance.resolverExpresion(c.condicion());
        tipos.validarCondicionBooleana(c.condicion());
        tabla.entrarScope("mientras");
        flujo.entrarCiclo();
        procesarBloque(c.cuerpo());
        flujo.salirCiclo();
        tabla.salirScope();
    }

    private void procesarCicloHacerMientras(NodoSentencia.CicloHacerMientras c) {
        tabla.entrarScope("hacer-mientras");
        flujo.entrarCiclo();
        procesarBloque(c.cuerpo());
        flujo.salirCiclo();
        tabla.salirScope();
        alcance.resolverExpresion(c.condicion());
        tipos.validarCondicionBooleana(c.condicion());
    }

    private void validarTiposDeParametros(List<NodoParametroZ> parametros) {
        for (NodoParametroZ p : parametros) {
            alcance.validarTipoDeclarado(p.tipo(), p.linea(), p.columna());
        }
    }

        public List<ErrorSemantico> getErrores() {
        return errores;
    }
}