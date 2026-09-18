package com.example.contacto_3xtrat3r3str3.y.semantica;

import com.example.contacto_3xtrat3r3str3.y.ast.NodoSentencia;
import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;

import java.util.List;

public class ValidadorFlujo {

    private final List<ErrorSemantico> errores;
    private int nivelCiclo = 0;

    public ValidadorFlujo(List<ErrorSemantico> errores) {
        this.errores = errores;
    }

    public void entrarCiclo() {
        nivelCiclo++;
    }

    public void salirCiclo() {
        nivelCiclo--;
    }

    public void validarRomper(NodoSentencia.Romper r) {
        if (nivelCiclo == 0) {
            errores.add(new ErrorSemantico(r.linea(), r.columna(),
                    "Corrupcion de flujo", "'romper' usado fuera de un ciclo"));
        }
    }

    public void validarContinuar(NodoSentencia.Continuar c) {
        if (nivelCiclo == 0) {
            errores.add(new ErrorSemantico(c.linea(), c.columna(),
                    "Corrupcion de flujo", "'continuar' usado fuera de un ciclo"));
        }
    }
}
