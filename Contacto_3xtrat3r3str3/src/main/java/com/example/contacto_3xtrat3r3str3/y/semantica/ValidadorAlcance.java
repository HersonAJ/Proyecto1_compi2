package com.example.contacto_3xtrat3r3str3.y.semantica;

import com.example.contacto_3xtrat3r3str3.y.ast.NodoExpr;
import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;

import java.util.List;

public class ValidadorAlcance {

    private final TablaSimbolos tabla;
    private final List<ErrorSemantico> errores;

    public ValidadorAlcance(TablaSimbolos tabla, List<ErrorSemantico> errores) {
        this.tabla = tabla;
        this.errores = errores;
    }

    public void resolverIdentificador(NodoExpr.Identificador id) {
        if (!tabla.buscarVariable(id.nombre()).isEmpty()) {
            errores.add(new ErrorSemantico(id.linea(), id.columna(),
                    "Identificador no declarado",
                    "'" + id.nombre() + "' no existe en el ambito actual"));
        }
    }

    public void resolverLlamadaFuncion(NodoExpr.LlamadaFuncion llamada) {
        if (tabla.buscarFuncion(llamada.nombre()).isEmpty()) {
            errores.add(new ErrorSemantico(llamada.linea(), llamada.columna(),
                    "Funcion no declarada" ,
                    "La funcion '" + llamada.nombre() + "' no existe"));
        }
    }

    public void resolverTipoEstructura(String nombreTipo, int linea, int columna) {
        if (nombreTipo != null && tabla.buscarEstructura(nombreTipo).isEmpty()) {
            errores.add(new ErrorSemantico(linea, columna,
                    "Estructura no declarada",
                    "El tipo '" + nombreTipo + "' no corresponde a ninguna estructura"));
        }
    }
}
