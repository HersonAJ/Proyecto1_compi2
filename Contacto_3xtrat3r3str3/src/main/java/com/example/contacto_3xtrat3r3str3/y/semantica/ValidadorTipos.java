package com.example.contacto_3xtrat3r3str3.y.semantica;

import com.example.contacto_3xtrat3r3str3.y.ast.NodoExpr;
import com.example.contacto_3xtrat3r3str3.y.ast.NodoSentencia;
import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;

import java.util.List;

public class ValidadorTipos {

    private final TablaSimbolos tabla;
    private final List<ErrorSemantico> errores;

    public ValidadorTipos(TablaSimbolos tabla, List<ErrorSemantico> errores) {
        this.tabla = tabla;
        this.errores = errores;
    }

    // TODO: pendiente jerarquia de conversion implicita confirmada por el encargado
    public String tipoDeExpresion(NodoExpr expresion) {
        return null;
    }

    // TODO: usa tipoDeExpresion() una vez este resuelta la jerarquia
    public void validarInicializacion(String tipoDeclarado, NodoExpr inicializacion) {
    }

    // TODO: el resultado de tipoDeExpresion(condicion) debe ser "bool"
    public void validarCondicionBooleana(NodoExpr condicion) {
    }

    // TODO: comparar contra funcionActual.tipoRetorno()
    public void validarRetorno(NodoSentencia.Retorno retorno, String tipoRetornoEsperado) {
    }

    // TODO: comparar argumentos contra definicion.tipoParametros()
    public void validarLlamada(NodoExpr.LlamadaFuncion llamada, TablaSimbolos.DefinicionFuncion definicion) {
    }
}