package com.example.contacto_3xtrat3r3str3.y.semantica;

import com.example.contacto_3xtrat3r3str3.y.ast.*;
import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;

import java.util.ArrayList;
import java.util.List;

public class ValidadorSemantico {

    private final TablaSimbolos tabla = new TablaSimbolos();
    private final List<ErrorSemantico> errores = new ArrayList<>();

    private final ValidadorDeclaraciones declaraciones = new ValidadorDeclaraciones(tabla, errores);
    private final ValidadorAlcance alcance = new ValidadorAlcance(tabla, errores);
    private final ValidadorTipos tipos = new ValidadorTipos(tabla, errores);
    private final ValidadorEstructuras estructuras = new ValidadorEstructuras(tabla, errores);
    private final ValidadorFlujo flujo = new ValidadorFlujo(errores);

    private NodoFuncion.Funcion funcionActual;

    public List<ErrorSemantico> analizar(NodoPrograma.Programa programa) {
        for (NodoEstructura e : programa.estructuras()) {
            declaraciones.declararEstructura((NodoEstructura.Estructura) e);
        }
        for (NodoFuncion f : programa.funciones()) {
            declaraciones.registrarFirmaFuncion((NodoFuncion.Funcion) f);
        }
        for (NodoFuncion f : programa.funciones()) {
            procesarFuncion((NodoFuncion.Funcion) f);
        }

        return errores;
    }

    private void procesarFuncion(NodoFuncion.Funcion funcion) {
        funcionActual = funcion;
        tabla.entrarScope(funcion.nombre());
        declaraciones.declararParametrosEnScope(funcion);
        procesarBloque(funcion.cuerpo());
        tabla.salirScope();
        funcionActual = null;
    }

    private void procesarBloque(List<NodoSentencia> bloque) {
        for (NodoSentencia s : bloque) procesarSentencia(s);
    }

    private void procesarSentencia(NodoSentencia s) {
        switch (s.tipoNodo()) {
            case DECLARACION_VARIABLE -> {
                NodoSentencia.DeclaracionVariable d = (NodoSentencia.DeclaracionVariable) s;
                alcance.resolverExpresion(d.inicializacion());
                declaraciones.declararVariable(d);
            }
            case DECLARACION_ARREGLO -> {
                NodoSentencia.DeclaracionArreglo d = (NodoSentencia.DeclaracionArreglo) s;
                for (var expr : d.inicializacion()) alcance.resolverExpresion(expr);
                declaraciones.declararArreglo(d);
            }
            case DECLARACION_MATRIZ -> declaraciones.declararMatriz((NodoSentencia.DeclaracionMatriz) s);
            case DECLARACION_ESTRUCTURA -> {
                NodoSentencia.DeclaracionEstructura d = (NodoSentencia.DeclaracionEstructura) s;
                alcance.resolverTipoEstructura(d.tipoEstructura(), d.linea(), d.columna());
                for (var expr : d.inicializacion()) alcance.resolverExpresion(expr);
                declaraciones.declararVariableEstructura(d);
            }
            case ASIGNACION -> {
                NodoSentencia.Asignacion a = (NodoSentencia.Asignacion) s;
                alcance.resolverExpresion(a.destino());
                alcance.resolverExpresion(a.valor());
                // TODO(ValidadorTipos): compatibilidad entre destino y valor
            }
            case INCREMENTO_DECREMENTO -> {
                NodoSentencia.IncrementoDecremento i = (NodoSentencia.IncrementoDecremento) s;
                alcance.resolverNombre(i.nombre(), i.linea(), i.columna());
                // TODO(ValidadorTipos): confirmar que su tipo sea numerico
            }
            case CONDICIONAL -> procesarCondicional((NodoSentencia.Condicional) s);
            case ELEGIR -> procesarElegir((NodoSentencia.Elegir) s);
            case CICLO_PARA -> procesarCicloPara((NodoSentencia.CicloPara) s);
            case CICLO_MIENTRAS -> procesarCicloMientras((NodoSentencia.CicloMientras) s);
            case CICLO_HACER_MIENTRAS -> procesarCicloHacerMientras((NodoSentencia.CicloHacerMientras) s);
            case RETORNO -> {
                NodoSentencia.Retorno r = (NodoSentencia.Retorno) s;
                alcance.resolverExpresion(r.valor());
                // TODO(ValidadorTipos): comparar contra funcionActual.tipoRetorno()
            }
            case IMPRIMIR -> alcance.resolverExpresion(((NodoSentencia.Imprimir) s).expresion());
            case LEER -> { }
            case ROMPER -> flujo.validarRomper((NodoSentencia.Romper) s);
            case CONTINUAR -> flujo.validarContinuar((NodoSentencia.Continuar) s);
        }
    }

    private void procesarCondicional(NodoSentencia.Condicional c) {
        alcance.resolverExpresion(c.condicion());
        tabla.entrarScope("si");
        procesarBloque(c.cuerpoSi());
        tabla.salirScope();

        if (c.cuerpoSino() != null) {
            alcance.resolverExpresion(c.condicionSino());
            tabla.entrarScope("sino");
            procesarBloque(c.cuerpoSino());
            tabla.salirScope();
        }
        if (c.cuerpoContrario() != null) {
            tabla.entrarScope("contrario");
            procesarBloque(c.cuerpoContrario());
            tabla.salirScope();
        }
    }

    private void procesarElegir(NodoSentencia.Elegir e) {
        alcance.resolverExpresion(e.expresion());
        for (NodoSentencia.CasoElegir caso : e.casos()) {
            tabla.entrarScope("caso");
            procesarBloque(caso.cuerpo());
            tabla.salirScope();
        }
        if (e.siempre() != null) {
            tabla.entrarScope("siempre");
            procesarBloque(e.siempre().cuerpo());
            tabla.salirScope();
        }
    }


    private void procesarCicloPara(NodoSentencia.CicloPara c) {
        tabla.entrarScope("para");
        declaraciones.declararVariableCiclo(c.nombreVariable(), c.tipoInicializacion(), c.linea(), c.columna());
        alcance.resolverExpresion(c.valorInicial());
        alcance.resolverExpresion(c.condicion());
        //la variable que se incrementa/decrementa en el 'para' debe ser la misma declarada en la inicializacion
        //(no lo valida resolverNombre porque acabamos de declararla arriba; esto es coherencia, no existencia)
        flujo.entrarCiclo();
        procesarBloque(c.cuerpo());
        flujo.salirCiclo();
        tabla.salirScope();
    }

    private void procesarCicloMientras(NodoSentencia.CicloMientras c) {
        alcance.resolverExpresion(c.condicion());
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
    }

    public List<ErrorSemantico> getErrores() {
        return errores;
    }
}
