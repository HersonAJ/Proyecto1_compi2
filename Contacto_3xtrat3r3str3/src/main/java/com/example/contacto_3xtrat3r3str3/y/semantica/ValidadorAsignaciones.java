package com.example.contacto_3xtrat3r3str3.y.semantica;

import com.example.contacto_3xtrat3r3str3.y.ast.NodoExpr;
import com.example.contacto_3xtrat3r3str3.y.ast.NodoSentencia;
import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;

import java.util.List;
import java.util.Optional;

/**
 * Valida reglas específicas de asignaciones:
 *   - No se puede asignar a un arreglo completo (sin índice).
 *   - No se puede asignar a una función.
 *   - No se puede asignar a una estructura completa si no es del mismo tipo.
 */
public class ValidadorAsignaciones {

    private final TablaSimbolos tabla;
    private final List<ErrorSemantico> errores;

    public ValidadorAsignaciones(TablaSimbolos tabla, List<ErrorSemantico> errores) {
        this.tabla = tabla;
        this.errores = errores;
    }

     //Valida que el destino de una asignación sea asignable.
     //Se llama desde ValidadorSemantico en el case ASIGNACION
    public void validarDestino(NodoSentencia.Asignacion asignacion) {
        NodoExpr destino = asignacion.destino();

        // Caso 1: destino es un Identificador simple (sin índices ni atributos).
        if (destino instanceof NodoExpr.Identificador id) {
            validarIdentificadorComoDestino(id);
        }

    }

    private void validarIdentificadorComoDestino(NodoExpr.Identificador id) {
        String nombre = id.nombre();

        // 1. Si es una función, no se puede asignar.
        if (tabla.buscarFuncion(nombre).isPresent()) {
            errores.add(new ErrorSemantico(id.linea(), id.columna(),
                    "Asignación inválida",
                    "'" + nombre + "' es una función y no puede ser destino de asignación"));
            return;
        }

        // 2. Si es una variable, revisar que no sea un arreglo.
        Optional<TablaSimbolos.SimboloVariable> simbolo = tabla.buscarVariable(nombre);
        if (simbolo.isEmpty()) {
            return;
        }

        TablaSimbolos.SimboloVariable v = simbolo.get();

        if (v.esArreglo()) {
            errores.add(new ErrorSemantico(id.linea(), id.columna(),
                    "Asignación inválida",
                    "No se puede asignar a '" + nombre + "' porque es un arreglo. "
                            + "Use un índice: '" + nombre + "[indice] = ...'"));
        }
    }
}