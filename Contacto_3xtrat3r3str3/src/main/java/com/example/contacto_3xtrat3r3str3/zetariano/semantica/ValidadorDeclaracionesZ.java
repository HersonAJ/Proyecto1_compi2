package com.example.contacto_3xtrat3r3str3.zetariano.semantica;

import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.*;

import java.util.ArrayList;
import java.util.List;

public class ValidadorDeclaracionesZ {

    private final TablaSimbolosZ tabla;
    private final List<ErrorSemantico> errores;

    public ValidadorDeclaracionesZ(TablaSimbolosZ tabla, List<ErrorSemantico> errores) {
        this.tabla = tabla;
        this.errores = errores;
    }

    public void declararClase(NodoClase clase) {
        tabla.declararClase(clase.nombre());
    }

    public void declararAtributos(NodoClase clase) {
        for (NodoAtributo a: clase.atributos()) {
            if (!tabla.declararAtributo(a.nombre(), a.tipo(), 0)) {
                errores.add(new ErrorSemantico(a.linea(), a.columna(), "Declaracion duplicada",
                        "El atributo '" + a.nombre() + "' ya fue declarado en la clase"));
            }
        }
    }

    public void declararConstructores(NodoClase clase) {
        for (NodoConstructor c : clase.constructores()) {
            //el nombre del constructor debe coincidir con el nombre de la clase
            if (!c.nombre().equals(clase.nombre())) {
                errores.add(new ErrorSemantico(c.linea(), c.columna(), "Constructor invalido",
                        "'" + c.nombre() + "' no coincide con el nombre de la clase '" + clase.nombre() + "'"));
                continue;
            }

            List<TablaSimbolosZ.Parametro> parametros = construirParametros(c.parametros(), c.nombre());
            if (!tabla.declararConstructor(c.nombre(), parametros)) {
                errores.add(new ErrorSemantico(c.linea(), c.columna(), "Declaracion duplicada",
                        "Ya existe un constructor de '" + c.nombre() + "' con esa misma firma"));
            }
        }
    }

    public void declararMetodos(NodoClase clase) {
        for (NodoMetodo m : clase.metodos()) {
            List<TablaSimbolosZ.Parametro> parametros = construirParametros(m.parametros(), m.nombre());
            if (!tabla.declararMetodo(m.nombre(), parametros, m.tipoRetorno())) {
                errores.add(new ErrorSemantico(m.linea(), m.columna(), "Declaracion ducplicada",
                        "Ya existe un metodo '" +  m.nombre() + "' con esa misma firma"));
            }

        }
    }

    private List<TablaSimbolosZ.Parametro> construirParametros(List<NodoParametro> nodos, String nombreDueño) {
        List<TablaSimbolosZ.Parametro> parametros = new ArrayList<>();
        for (NodoParametro p : nodos) {
            boolean yaExiste = parametros.stream().anyMatch(x -> x.nombre().equals(p.nombre()));
            if (yaExiste) {
                errores.add(new ErrorSemantico(p.linea(), p.columna(), "Declaracion duplicada",
                        "El parametro '" + p.nombre() + "' ya fue declarado en '" + nombreDueño + "'"));
                continue;
            }
            parametros.add(new TablaSimbolosZ.Parametro(p.nombre(), p.tipo(), 0));
        }
        return parametros;
    }

    //registrar los parametros como variables del scope local, al entrar al cuerpo de un constructor/metod
    public void declararParametrosEnScope(List<NodoParametro> parametros) {
        for (NodoParametro p : parametros) {
            tabla.declararVariable(p.nombre(), p.tipo());
        }
    }

    public void declararVariable(NodoSentencia.DeclaracionVariable d) {
        if (!tabla.declararVariable(d.nombre(), d.tipo(), d.dimensiones())) {
            errores.add(new ErrorSemantico(d.linea(), d.columna(), "Declaracion duplicada",
                    "'" + d.nombre() + "' ya fue declarado en este ambito"));
        }
    }
}
