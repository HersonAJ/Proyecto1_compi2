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

    //valida que una inicializacion sea compatible con el tipo declarado
    //se una en 'entero x = <expr>
    public void validarInicializacion(String tipoDeclarado, NodoExpr inicializacion) {
        if (inicializacion == null) return;

        String tipoExpr = tipoDeExpresion(inicializacion);
        if (tipoExpr == null) return;

        if (!esAsignable(tipoDeclarado, tipoExpr)) {
            errores.add(new ErrorSemantico(inicializacion.linea(), inicializacion.columna(), "Tipo incompatible en inicializacion",
                    "No se puede asignar '" + tipoExpr + "' a una variable de tipo '" + tipoDeclarado + "'"));
        }
    }

    //valida que una condicion sea de tipo bool
    //se usa en si, sino, mientras, hacer-mientras
    public void validarCondicionBooleana(NodoExpr condicion) {
        if (condicion == null) return;

        String tipo = tipoDeExpresion(condicion);
        if (tipo == null) return;

        if (!tipo.equals("bool")) {
            errores.add(new ErrorSemantico(condicion.linea(), condicion.columna(), "Tipo incompatible en condicion",
                    "La condicion debe ser 'bool', se encontro '" + tipo + "'"));
        }
    }

    //vañoda qie eñ vañpr de un 'retornar' sea compatible con el tipo de retorno declarado en la funcion
    public void validarRetorno(NodoSentencia.Retorno retorno, String tipoRetornoEsperado) {
        //caso 1: funcion sin retorno que no retorna un valor
        if (tipoRetornoEsperado == null) {
            if (retorno.valor() != null) {
                errores.add(new ErrorSemantico(retorno.linea(), retorno.columna(), "Retorno incompatible",
                        "La funcion no declara tipo de retorno, pero se esta retornando un valor"));
            }
            return;
        }

        //caso 2: comparar el tipo del valor con el esperado
        String tipoValor = tipoDeExpresion(retorno.valor());
        if (tipoValor == null)return;

        if (!esAsignable(tipoRetornoEsperado, tipoValor)) {
            errores.add(new ErrorSemantico(retorno.linea(), retorno.columna(), "Tipo incompatible de retorno",
                    "Se esperaba '" + tipoRetornoEsperado + "', se encontro '" + tipoValor + "'"));
        }
    }

    // valida que los argumentos de una llamada coincidan en numero y tipo con los parametros de la funcion
    public void validarLlamada(NodoExpr.LlamadaFuncion llamada, TablaSimbolos.DefinicionFuncion definicion) {
        if (llamada == null || definicion == null) return;

        List<NodoExpr> argumentos = llamada.argumentos();
        List<TablaSimbolos.Parametro> params = definicion.parametros();

        //1 verificar el numero de argumentos
        if (argumentos.size() != params.size()) {
            errores.add(new ErrorSemantico(llamada.linea(), llamada.columna(), "Argumentos incorrectos",
                    "La funcion '" + definicion.nombre() + "' espera  " + params.size() +
                    " arumentos, se econtraron " + argumentos.size()));
            return;
        }

        //2 verificar tipo de aargumentos por argumento
        for (int i = 0; i < argumentos.size(); i++) {
            NodoExpr arg = argumentos.get(i);
            TablaSimbolos.Parametro parametro = params.get(i);

            String tipoArg = tipoDeExpresion(arg);
            if (tipoArg == null) continue;;

            String tipoParam = parametro.tipo();
            if (tipoParam == null) {
                tipoParam = parametro.tipoEstructura();
            }

            if (!esAsignable(tipoParam, tipoArg)) {
                errores.add(new ErrorSemantico(arg.linea(), arg.columna(), "Argumento incompatible",
                        "El argumento " + (i + 1) + " de '" + definicion.nombre() +
                        "' espera '" + tipoParam + "', se encontro '" + tipoArg + "'"));
            }
        }
    }

    /**
     * Devuelve true si un valor de tipo 'tipoOrigen' se puede asignar
     * a una variable de tipo 'tipoDestino', según la tabla de compatibilidad:
     *
     *   destino \ origen | entero | flotante | caracter | cadena | bool
     *   -----------------|--------|----------|----------|--------|------
     *   entero           |  SI    |    NO    |    NO    |   NO   |  NO
     *   flotante         |  SI    |    SI    |    NO    |   NO   |  NO
     *   caracter         |  NO    |    NO    |    SI    |   NO   |  NO
     *   cadena           |  SI    |    SI    |    SI    |   SI   |  SI   (concatenación)
     *   bool             |  NO    |    NO    |    NO    |   NO   |  SI
     */

    private boolean esAsignable(String tipoDestino, String tipoOrigen) {
        if (tipoDestino == null || tipoOrigen == null) return false;

        //mismo tipo: siempre valido
        if (tipoDestino.equals(tipoOrigen)) return true;

        //cadena aceptada cualquier cosa (concatenacion)
        if (tipoDestino.equals("cadena")) return true;

        //entro  -> flotante: converison implicita permitida
        if (tipoDestino.equals("flotante") && tipoOrigen.equals("entero")) return true;

        return false;
    }

    //Valida que un valor de tipo 'tipoValor' se pueda asignar a una variable de tipo 'tipoDestino'. Reporta error si no.

    public void validarAsignacion(String tipoDestino, String tipoValor, int linea, int columna) {
        if (tipoDestino == null || tipoValor == null) return;

        if (!esAsignable(tipoDestino, tipoValor)) {
            errores.add(new ErrorSemantico(linea, columna,
                    "Tipo incompatible en asignación",
                    "No se puede asignar '" + tipoValor + "' a una variable de tipo '" + tipoDestino + "'"));
        }
    }
}