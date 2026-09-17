package com.example.contacto_3xtrat3r3str3.y.semantica;

import com.example.contacto_3xtrat3r3str3.y.ast.NodoExpr;
import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;

import java.util.List;

public class ValidadorEstructuras {

    private final TablaSimbolos tabla;
    private final List<ErrorSemantico> errores;

    public ValidadorEstructuras(TablaSimbolos tabla, List<ErrorSemantico> errores) {
        this.tabla = tabla;
        this.errores = errores;
    }

    // TODO: resolver el tipo de acceso.objeto() y confirmar que acceso.atributo() exista ahi
    public void validarAccesoAtributo(NodoExpr.AccesoAtributo acceso) {
    }

    // TODO: el indice debe ser de tipo entero (depende de ValidadorTipos)
    public void validarAccesoArray(NodoExpr.AccesoArray acceso) {
    }
}