package com.example.contacto_3xtrat3r3str3.piglatin.service;

import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoPrograma;
import com.example.contacto_3xtrat3r3str3.y.errores.ErrorPosicional;
import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;

import java.util.List;

public class ResultadoCompilacionPig {

    private final boolean exitoso;
    private final NodoPrograma programa;
    private final List<ErrorPosicional> erroresLexicos;
    private final List<ErrorPosicional> erroresSintacticos;
    private final List<ErrorSemantico> erroresSemanticos;
    private final List<String> mensajesInternos;

    public ResultadoCompilacionPig(boolean exitoso,
                                   NodoPrograma programa,
                                   List<ErrorPosicional> erroresLexico,
                                   List<ErrorPosicional> erroresSintacticos,
                                   List<ErrorSemantico> erroresSemanticos,
                                   List<String> mensajesInterno) {
        this.exitoso = exitoso;
        this.programa = programa;
        this.erroresLexicos = erroresLexico;
        this.erroresSintacticos = erroresSintacticos;
        this.erroresSemanticos = erroresSemanticos;
        this.mensajesInternos = mensajesInterno;
    }

    public boolean isExitoso() { return exitoso; }
    public NodoPrograma getPrograma() { return programa; }
    public List<ErrorPosicional> getErroresLexicos() { return erroresLexicos; }
    public List<ErrorPosicional> getErroresSintacticos() { return erroresSintacticos; }
    public List<ErrorSemantico> getErroresSemanticos() { return erroresSemanticos; }
    public List<String> getMensajesInternos() { return mensajesInternos; }

    public boolean hayErrores() {
        return !erroresLexicos.isEmpty()
                || !erroresSintacticos.isEmpty()
                || !erroresSemanticos.isEmpty()
                || !mensajesInternos.isEmpty();
    }
}
