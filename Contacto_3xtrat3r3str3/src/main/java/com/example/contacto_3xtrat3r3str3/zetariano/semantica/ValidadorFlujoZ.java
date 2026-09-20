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

    // CODIGO INALCANZABLE
    public void validarCodigoInalcanzable(List<NodoSentencia> bloque) {
        if (bloque == null) return;

        boolean alcanzable = true;

        for (NodoSentencia s : bloque) {
            if (!alcanzable) {
                errores.add(new ErrorSemantico(s.linea(), s.columna(), "Código inalcanzable",
                        "Esta instrucción nunca se ejecutará porque está después de 'return', 'break' o 'continue'"));
                break;
            }

            // Un return, break o continue válido corta el flujo.
            if (s instanceof NodoSentencia.Retorno) {
                alcanzable = false;
            } else if (s instanceof NodoSentencia.Romper && (nivelCiclo > 0 || nivelSwitch > 0)) {
                alcanzable = false;
            } else if (s instanceof NodoSentencia.Continuar && nivelCiclo > 0) {
                alcanzable = false;
            }
        }
    }

    // FALTA DE RETORNO
    public void validarRetornoGarantizado(String tipoRetorno,
                                          List<NodoSentencia> cuerpo,
                                          int linea,
                                          int columna) {
        // Si el método es void, no hay nada que validar.
        if (tipoRetorno == null) return;

        if (cuerpo == null || cuerpo.isEmpty()) {
            errores.add(new ErrorSemantico(linea, columna,
                    "Falta de retorno",
                    "El método declara retorno de tipo '" + tipoRetorno + "' pero no tiene instrucciones"));
            return;
        }

        boolean garantiza = bloqueGarantizaRetorno(cuerpo);

        if (!garantiza) {
            errores.add(new ErrorSemantico(linea, columna,
                    "Falta de retorno",
                    "El método declara retorno de tipo '" + tipoRetorno
                            + "' pero no todos los caminos terminan con 'return'"));
        }
    }

    private boolean bloqueGarantizaRetorno(List<NodoSentencia> bloque) {
        if (bloque == null || bloque.isEmpty()) return false;

        for (NodoSentencia s : bloque) {
            if (instruccionGarantizaRetorno(s)) {
                return true;
            }
        }
        return false;
    }

    private boolean instruccionGarantizaRetorno(NodoSentencia s) {
        // Caso 1: return directo.
        if (s instanceof NodoSentencia.Retorno) {
            return true;
        }

        // Caso 2: condicional con rama 'else' donde ambas ramas retornan.
        if (s instanceof NodoSentencia.Condicional c) {
            boolean siRetorna = bloqueGarantizaRetorno(c.cuerpoSi());

            // Si no hay else, no se garantiza que la rama else retorne.
            if (c.cuerpoSino() == null) {
                return false;
            }
            boolean sinoRetorna = bloqueGarantizaRetorno(c.cuerpoSino());

            return siRetorna && sinoRetorna;
        }

        // Cualquier otra instrucción no garantiza retorno.
        return false;
    }
}