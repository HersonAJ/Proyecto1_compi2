package com.example.contacto_3xtrat3r3str3.zetariano.semantica;


import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoSentencia;

import java.util.List;

public class ValidadorFlujoZ {

    private final List<ErrorSemantico> errores;
    private int nivelCiclo = 0;
    private int nivelSwitch = 0;

    public ValidadorFlujoZ(List<ErrorSemantico> errores) {
        this.errores = errores;
    }

    public void entrarCiclo() { nivelCiclo++;}
    public void salirCiclo() { nivelCiclo--; }

    public void entrarSwitch() {nivelSwitch++; }
    public void salirSwitch() { nivelSwitch--;}

    public void validarRomper(NodoSentencia.Romper r) {
        if (nivelCiclo == 0 && nivelSwitch == 0) {
            errores.add(new ErrorSemantico(r.linea(), r.columna(), "Flujo invalido",
                    "'break' usado fuera de un ciclo o de un switch"));
        }
    }

    public void validarContinuar(NodoSentencia.Continuar c) {
        if (nivelCiclo == 0) {
            errores.add(new ErrorSemantico(c.linea(), c.columna(), "Flujo invalido",
                    "'continue' usado fuera de un ciclo"));
        }
    }
}
