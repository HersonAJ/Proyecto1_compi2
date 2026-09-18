package com.example.contacto_3xtrat3r3str3.y.semantica;

import com.example.contacto_3xtrat3r3str3.y.ast.NodoExpr;
import com.example.contacto_3xtrat3r3str3.y.ast.NodoSentencia;
import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;

import java.util.List;
import java.util.Optional;

public class ValidadorTipos {

    private final TablaSimbolos tabla;
    private final List<ErrorSemantico> errores;

    public ValidadorTipos(TablaSimbolos tabla, List<ErrorSemantico> errores) {
        this.tabla = tabla;
        this.errores = errores;
    }

    //inferencia de tipos

    //devuelve el nombre del tipo de una expresion, o null si no se puede
    public String tipoDeExpresion(NodoExpr expresion) {
        if (expresion == null) return null;

        return switch (expresion.tipoNodo()) {
            case LITERAL_ENTERO ->  "entero";
            case LITERAL_FLOTANTE -> "flotante";
            case LITERAL_CADENA -> "cadena";
            case LITERAL_CARACTER -> "caracter";
            case LITERAL_BOOL -> "bool";

            case IDENTIFICADOR -> tipoDeIdentificador((NodoExpr.Identificador) expresion);
            case ACCESO_ARRAY -> tipoDeAccesoArray((NodoExpr.AccesoArray) expresion);
            case ACCESO_ATRIBUTO -> tipoDeAccesoAtributo((NodoExpr.AccesoAtributo) expresion);

            case BINARIA -> tipoDeBinaria((NodoExpr.Binaria) expresion);
            case UNARIA -> tipoDeUnaria((NodoExpr.Unaria) expresion);

            case LLAMADA_FUNCION -> tipoDeLlamada((NodoExpr.LlamadaFuncion) expresion);
        };
    }

    private String tipoDeIdentificador(NodoExpr.Identificador id) {
        Optional<TablaSimbolos.SimboloVariable> simbolo = tabla.buscarVariable(id.nombre());
        if (simbolo.isEmpty()) {
            return  null;
        }
        TablaSimbolos.SimboloVariable variable = simbolo.get();
        if (variable.esEstructura()) {
            return  variable.tipoEstructura();
        }
        return variable.tipo();
    }

    private String tipoDeAccesoArray(NodoExpr.AccesoArray acceso) {
        String tipoIndice = tipoDeExpresion(acceso.indice());
        if (tipoIndice != null && !tipoIndice.equals("entero")) {
            errores.add(new ErrorSemantico(acceso.linea(), acceso.columna(), "Topo incompatibla en indice",
                    "El indice de un arreglo debe ser 'entero', se encontro '" + tipoIndice + "'"));
        }

        String tipoArreglo = tipoDeExpresion(acceso.arreglo());
        return tipoArreglo;
    }

    private String tipoDeAccesoAtributo(NodoExpr.AccesoAtributo acceso) {
        String tipoObjeto = tipoDeExpresion(acceso.objeto());
        if (tipoObjeto == null) {
            return null;
        }

        Optional<TablaSimbolos.DefinicionEstructura> def = tabla.buscarEstructura(tipoObjeto);
        if (def.isEmpty()) {
            errores.add(new ErrorSemantico(acceso.linea(), acceso.columna(),
                    "Acceso a atributo inválido",
                    "'" + tipoObjeto + "' no es una estructura"));
            return null;
        }

        String tipoAtrubuto = def.get().atributos().get(acceso.atributo());
        if (tipoAtrubuto == null) {
            errores.add(new ErrorSemantico(acceso.linea(), acceso.columna(), "Atributo no encontrado",
                    "La estructura '" + tipoObjeto + "' no tiene un atributo '" + acceso.atributo() + "'"));
            return null;
        }
        return tipoAtrubuto;
    }

    private String tipoDeBinaria(NodoExpr.Binaria bin) {
        String tipoIzq = tipoDeExpresion(bin.izquierda());
        String tipoDer = tipoDeExpresion(bin.derecha());

        //si alguno no se puede determinar, se devuelve null
        if (tipoIzq == null || tipoDer == null) {
            return null;
        }

        String operador = bin.operador();

        //operadores logicos: ambos deben ser bool, para tener un resultado bool
        if (operador.equals("&&") || operador.equals("||")) {
            if (!tipoIzq.equals("bool") || !tipoDer.equals("bool")) {
                errores.add(new ErrorSemantico(bin.linea(), bin.columna(), "Tipo incompatible en operacion logica",
                        "El operador '" + operador + "' requiere 'bool', se encontro '" + tipoIzq + "' y '" + tipoDer + "'"));
                return null;
            }
            return "bool";
        }

        //operadores relacionales y de igualdad: resultado bool
        if (operador.equals("==") || operador.equals("!=") || operador.equals("<") || operador.equals(">")
        || operador.equals("<=") || operador.equals(">=")) {

            //comparacion entre tipos compatibles
            if (!sonCompatibles(tipoIzq, tipoDer)) {
                errores.add(new ErrorSemantico(bin.linea(), bin.columna(), "Tipo incompatible en comparacion",
                        "No se puede comparar '" + tipoIzq + "' con '" + tipoDer +"'"));
                return null;
            }
            return "bool";
        }

        //operadores aritmeticos
        if(operador.equals("+")) {
            //si alguno es cadena, el resultado es cadena por concatenacion
            if (tipoIzq.equals("cadena") || tipoDer.equals("cadena")) {
                return "cadena";
            }
        }

        //para el respo de operacions arimeticas, ambos deben ser numericos
        if (!esNumerico(tipoIzq) || !esNumerico(tipoDer)) {
            errores.add(new ErrorSemantico(bin.linea(), bin.columna(), "Tipo incompatible en operacion numerica",
                    "El operador '" + operador +"' requiere tipos numericos, se encontro '" + tipoIzq+ "' y '" + tipoDer + "'"));
            return null;
        }

        //si alguno es flotante, el resultado es flotanten
        if (tipoIzq.equals("flotante") || tipoDer.equals("flotante")) {
            return "flotante";
        }
        return "entero";
    }

    private String tipoDeUnaria(NodoExpr.Unaria unaria) {
        String tipoOperando = tipoDeExpresion(unaria.operando());
        if (tipoOperando == null) return null;

        if (unaria.operador().equals("!")) {
            if (!tipoOperando.equals("bool")) {
                errores.add(new ErrorSemantico(unaria.linea(), unaria.columna(), "Tipo incompatible de negacion",
                        "El operador '!' requiere 'bool', se encontro '" + tipoOperando + "'"));
                return null;
            }
            return "bool";
        }

        if (unaria.operador().equals("-")) {
            if (!esNumerico(tipoOperando)) {
                errores.add(new ErrorSemantico(unaria.linea(), unaria.columna(), "Tipo incompatible en negacion aritmetica",
                        "El operador '-' requiere tipo numerico, se encontro '" + tipoOperando + "'"));
                return null;
            }
            return tipoOperando;
        }

        // ++ y -- cuando aparezacon como expresiones
        if (unaria.operador().equals("++") || unaria.operador().equals("--")) {
            if (!esNumerico(tipoOperando)) {
                errores.add(new ErrorSemantico(unaria.linea(), unaria.columna(), "Tipo incompatible en incremento/decremento" ,
                        "El operador '" + unaria.operador() + "' requiere tipo numerico, se encontro" + tipoOperando + "'"));
                return null;
            }
            return tipoOperando;
        }
        return null;
    }

    private String tipoDeLlamada(NodoExpr.LlamadaFuncion llamada) {
        Optional<TablaSimbolos.DefinicionFuncion> definicion = tabla.buscarFuncion(llamada.nombre());
        if (definicion.isEmpty()) {
            return  null;
        }
        return definicion.get().tipoRetorno();
    }

    //helpers
    private boolean esNumerico(String tipo) {
        return tipo != null && (tipo.equals("entero") || tipo.equals("flotante"));
    }

    private boolean sonCompatibles(String tipoA, String tipoB) {
        //numeiroc entre si
        if (esNumerico(tipoA) && esNumerico(tipoB)) return true;
        //mismo tipo
        if (tipoA.equals(tipoB)) return true;
        return false;
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