package com.example.contacto_3xtrat3r3str3.c3d_v2.c;


import com.example.contacto_3xtrat3r3str3.c3d_v2.GestorCodigoIntermedio;
import com.example.contacto_3xtrat3r3str3.y.semantica.TablaSimbolos;

/**
 * Agrupa todo lo que un nodo del AST necesita para traducirse:
 *   - el buffer de cuádruplas + contador + pila de ciclos
 *   - la tabla de símbolos para consultar tipos
 *   - (futuro) info de la función actual, structs definidos, etc.
 */
public class ContextoTraduccion {

    private final GestorCodigoIntermedio gestor;
    private final TablaSimbolos tabla;

    // Tipo de retorno de la función actual (null si void).
    // Lo usará Retorno.toIntermediateCode para validaciones tardías.
    private String tipoRetornoFuncionActual;

    public ContextoTraduccion(GestorCodigoIntermedio gestor, TablaSimbolos tabla) {
        this.gestor = gestor;
        this.tabla = tabla;
    }

    public GestorCodigoIntermedio getGestor() { return gestor; }
    public TablaSimbolos getTabla()           { return tabla; }

    public String getTipoRetornoFuncionActual() {
        return tipoRetornoFuncionActual;
    }

    public void setTipoRetornoFuncionActual(String tipoRetornoFuncionActual) {
        this.tipoRetornoFuncionActual = tipoRetornoFuncionActual;
    }
}
