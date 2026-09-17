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

    private void procesarSentencia(NodoSentencia sentencia) {
        switch (sentencia.tipoNodo()) {
            case DECLARACION_VARIABLE -> declaraciones.declararVariable((NodoSentencia.DeclaracionVariable) sentencia);
            case DECLARACION_ARREGLO -> declaraciones.declararArreglo((NodoSentencia.DeclaracionArreglo) sentencia);
            case DECLARACION_MATRIZ -> declaraciones.declararMatriz((NodoSentencia.DeclaracionMatriz) sentencia);
            case ASIGNACION -> {}
            case INCREMENTO_DECREMENTO -> {}
            case CONDICIONAL -> procesarCondicional((NodoSentencia.Condicional) sentencia);
            case ELEGIR -> procesarElegir((NodoSentencia.Elegir) sentencia);
            case CICLO_PARA -> procesarCicloPara((NodoSentencia.CicloPara) sentencia);
            case CICLO_MIENTRAS -> procesarCicloMientras((NodoSentencia.CicloMientras) sentencia);
            case CICLO_HACER_MIENTRAS -> procesarCicloHacerMientras((NodoSentencia.CicloHacerMientras) sentencia);
            case RETORNO -> {}
            case IMPRIMIR -> {}
            case LEER -> {}
            case ROMPER -> flujo.validarRomper((NodoSentencia.Romper) sentencia);
            case CONTINUAR -> flujo.vallidarContinuar((NodoSentencia.Continuar) sentencia);
        }
    }

    private void procesarCondicional(NodoSentencia.Condicional condicional) {
        tabla.entrarScope("si");
        procesarBloque(condicional.cuerpoSi());
        tabla.salirScope();

        if (condicional.cuerpoSino() != null) {
            tabla.entrarScope("sino");
            procesarBloque(condicional.cuerpoSino());
            tabla.salirScope();
        }
        if (condicional.cuerpoContrario() != null) {
            tabla.entrarScope("contrario");
            procesarBloque(condicional.cuerpoContrario());
            tabla.salirScope();
        }
    }

    private void procesarElegir(NodoSentencia.Elegir elegir) {
        for (NodoSentencia.CasoElegir caso : elegir.casos()) {
            tabla.entrarScope("caso");
            procesarBloque(caso.cuerpo());
            tabla.salirScope();
        }
        if (elegir.siempre() != null) {
            tabla.entrarScope("siempre");
            procesarBloque(elegir.siempre().cuerpo());
            tabla.salirScope();
        }
    }

    private void procesarCicloPara(NodoSentencia.CicloPara ciclo) {
        tabla.entrarScope("para");
        declaraciones.declararVariableCiclo(ciclo.nombreVariable(), ciclo.tipoInicializacion(), ciclo.linea(), ciclo.columna());
        flujo.entrarCiclo();
        procesarBloque(ciclo.cuerpo());
        flujo.salirCiclo();
        tabla.salirScope();
    }

    private void procesarCicloMientras(NodoSentencia.CicloMientras c) {
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
    }

    public List<ErrorSemantico> getErrores() {
        return errores;
    }
}
