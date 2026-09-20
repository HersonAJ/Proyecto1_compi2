package com.example.contacto_3xtrat3r3str3.zetariano.semantica;

import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoExpr;

import java.util.List;

public class ValidadorAlcanceZ {

    private final TablaSimbolosZ tabla;
    private final List<ErrorSemantico> errores;

    public ValidadorAlcanceZ(TablaSimbolosZ tabla, List<ErrorSemantico> errores) {
        this.tabla = tabla;
        this.errores = errores;
    }

    public void resolverExpresion(NodoExpr expr) {
        if (expr == null) return;

        switch (expr.tipoNodo()) {
            case IDENTIFICADOR -> resolverIdentificador((NodoExpr.Identificador) expr);

            case ACCESO_ARRAY -> {
                NodoExpr.AccesoArray a = (NodoExpr.AccesoArray) expr;
                resolverExpresion(a.arreglo());
                resolverExpresion(a.indice());
            }

            case ACCESO_ATRIBUTO -> {
                NodoExpr.AccesoAtributo a = (NodoExpr.AccesoAtributo) expr;
                resolverExpresion(a.objeto());
            }

            case BINARIA -> {
                NodoExpr.Binaria b = (NodoExpr.Binaria) expr;
                resolverExpresion(b.izquierda());
                resolverExpresion(b.derecha());
            }

            case UNARIA -> resolverExpresion(((NodoExpr.Unaria) expr).operando());

            case INCREMENTO_DECREMENTO -> resolverExpresion(((NodoExpr.IncrementoDecremento) expr).operando());

            case TERNARIA -> {
                NodoExpr.Ternaria t = (NodoExpr.Ternaria) expr;
                resolverExpresion(t.condicion());
                resolverExpresion(t.siVerdadero());
                resolverExpresion(t.siFalso());
            }

            case LLAMADA_FUNCION -> {
                NodoExpr.LlamadaFuncion llamada = (NodoExpr.LlamadaFuncion) expr;
                for (NodoExpr arg : llamada.argumentos()) {

                }
            }

            case LLAMADA_METODO -> {
                NodoExpr.LlamadaMetodo llamada = (NodoExpr.LlamadaMetodo) expr;
                resolverExpresion(llamada.objeto());
                for (NodoExpr arg : llamada.argumentos()) resolverExpresion(arg);
            }

            case INSTANCIA_OBJETO -> {
                NodoExpr.InstanciaObjeto instancia = (NodoExpr.InstanciaObjeto) expr;
                if (!instancia.tipoClase().equals(tabla.getNombreClase())) {
                    errores.add(new ErrorSemantico(instancia.linea(), instancia.columna(), "Clase no declarada",
                            "'" + instancia.tipoClase() + "' no corresponde a ninguna clase conocida"));
                }

                for (NodoExpr arg : instancia.argumentos()) resolverExpresion(arg);
            }

            case ARREGLO_NUEVO -> {
                NodoExpr.ArregloNuevo arreglo = (NodoExpr.ArregloNuevo) expr;
                for (NodoExpr dim : arreglo.dimensiones()) resolverExpresion(dim);
            }

            case LISTA_LITERAL -> {
                for (NodoExpr elem : ((NodoExpr.ListaLiteral) expr).elementos()) resolverExpresion(elem);
            }

            case LITERAL_ENTERO, LITERAL_DECIMAL, LITERAL_CADENA, LITERAL_CARACTER, LITERAL_BOOL, LITERAL_NULO -> {}
        }
    }

    public void resolverIdentificador(NodoExpr.Identificador id) {
        resolverNombre(id.nombre(), id.linea(), id.columna());
    }

    public void resolverNombre(String nombre, int linea, int columna) {
        if (tabla.buscarVariable(nombre).isEmpty()) {
            errores.add(new ErrorSemantico(linea, columna, "Identificador no declarado",
                    "'" + nombre + "' no existe en el ambito actual ni es un atributo de la clase"));
        }
    }
}
