package com.example.contacto_3xtrat3r3str3.generadorC;

import com.example.contacto_3xtrat3r3str3.c3d.CuartetaV1;

import java.util.HashSet;
import java.util.Set;

/**
 * Traduce cada cuarteta individual a código C.
 */
public class TraductorCuartetas {

    private final EscritorC escritor;
    private final Set<String> temporalesDeclarados = new HashSet<>();
    private int contadorRetornos = 0;
    private String nombreFuncionActual = "L_Pig_main";

    public TraductorCuartetas(EscritorC escritor) {
        this.escritor = escritor;
    }

    public boolean procesar(CuartetaV1 c) {
        String op = c.operador();

        switch (op) {
            case "+", "-", "*", "/", "%", "==", "!=", "<", ">", "<=", ">=", "&&", "||" ->
                    traducirOperacion(c);
            case "=" -> traducirAsignacion(c);
            case "concat" -> traducirConcat(c);
            case "goto" -> traducirGoto(c);
            case "label" -> traducirLabel(c);
            case "if" -> traducirIfSimple(c);
            case "if==", "if!=", "if<", "if>", "if<=", "if>=" -> traducirIfRelacional(c);
            case "print" -> traducirPrint(c);
            case "println" -> traducirPrintln(c);
            case "read" -> traducirRead(c);
            case "return" -> traducirReturn(c);
            case "param" -> traducirParam(c);
            case "call" -> traducirCall(c);
            case "new" -> traducirNew(c);
            default -> {
                return false;
            }
        }
        return true;
    }

    // ============================================================
    // OPERACIONES
    // ============================================================

    private void traducirOperacion(CuartetaV1 c) {
        String opC = c.operador();
        String a1 = c.arg1();
        String a2 = c.arg2();
        String res = c.resultado();

        if (a1.startsWith("stack[")) {
            String dir = a1.substring(6, a1.length() - 1);
            a1 = "stack[" + dir + "].i";
        } else if (esTemporal(a1)) {
            a1 = a1 + ".i";
        }

        if (a2.startsWith("stack[")) {
            String dir = a2.substring(6, a2.length() - 1);
            a2 = "stack[" + dir + "].i";
        } else if (esTemporal(a2)) {
            a2 = a2 + ".i";
        }

        declararTemporal(res);
        escritor.linea(res + ".i = " + a1 + " " + opC + " " + a2 + ";");
    }

    private void traducirAsignacion(CuartetaV1 c) {
        String valor = c.arg1();
        String destino = c.resultado();

        String campo = ".i";
        if (valor.startsWith("\"")) {
            campo = ".s";
        }

        if (valor.startsWith("stack[")) {
            String dir = valor.substring(6, valor.length() - 1);
            valor = "stack[" + dir + "]" + campo;
        } else if (esTemporal(valor)) {
            valor = valor + campo;
        }

        if (destino.startsWith("stack[")) {
            String dir = destino.substring(6, destino.length() - 1);
            escritor.linea("stack[" + dir + "]" + campo + " = " + valor + ";");
        } else {
            declararTemporal(destino);
            escritor.linea(destino + campo + " = " + valor + ";");
        }
    }

    private void traducirConcat(CuartetaV1 c) {
        String resultado = c.resultado();
        String a1 = c.arg1();
        String a2 = c.arg2();

        if (esTemporal(a1)) a1 = a1 + ".s";
        if (esTemporal(a2)) a2 = a2 + ".s";

        declararTemporal(resultado);
        escritor.linea(resultado + ".s = concat(" + a1 + ", " + a2 + ");");
    }

    // ============================================================
    // SALTOS Y ETIQUETAS
    // ============================================================

    private void traducirGoto(CuartetaV1 c) {
        escritor.linea("goto " + c.resultado() + ";");
    }

    private void traducirLabel(CuartetaV1 c) {
        escritor.linea(c.resultado() + ":;");
    }

    private void traducirIfSimple(CuartetaV1 c) {
        escritor.linea("if (" + c.arg1() + ") goto " + c.resultado() + ";");
    }

    private void traducirIfRelacional(CuartetaV1 c) {
        String operador = c.operador().substring(2);
        escritor.linea("if (" + c.arg1() + " " + operador + " " + c.arg2() + ") goto " + c.resultado() + ";");
    }

    // ============================================================
    // PRINT / READ
    // ============================================================

    private void traducirPrint(CuartetaV1 c) {
        String valor = c.arg1();

        if (valor.startsWith("\"")) {
            String texto = valor.substring(1, valor.length() - 1);
            escritor.linea("printf(\"" + texto + "\");");
        } else if (valor.startsWith("stack[")) {
            String dir = valor.substring(6, valor.length() - 1);
            escritor.linea("printf(\"%d\", stack[" + dir + "].i);");
        } else if (esTemporal(valor)) {
            escritor.linea("printf(\"%d\", " + valor + ".i);");
        } else {
            escritor.linea("printf(\"%d\", " + valor + ");");
        }
    }

    private void traducirPrintln(CuartetaV1 c) {
        String valor = c.arg1();

        if (valor.startsWith("\"")) {
            String texto = valor.substring(1, valor.length() - 1);
            escritor.linea("printf(\"" + texto + "\\n\");");
        } else if (valor.startsWith("stack[")) {
            String dir = valor.substring(6, valor.length() - 1);
            escritor.linea("printf(\"%d\\n\", stack[" + dir + "].i);");
        } else if (esTemporal(valor)) {
            escritor.linea("printf(\"%d\\n\", " + valor + ".i);");
        } else {
            escritor.linea("printf(\"%d\\n\", " + valor + ");");
        }
    }

    private void traducirRead(CuartetaV1 c) {
        String destino = c.resultado();
        if (!destino.equals("-")) {
            escritor.linea("scanf(\"%d\", &" + destino + ");");
        } else {
            escritor.linea("scanf(\"%*d\");");
        }
    }

    // ============================================================
    // RETURN
    // ============================================================

    private void traducirReturn(CuartetaV1 c) {
        String valor = c.arg1();

        if (valor.equals("-")) {
            escritor.linea("goto " + nombreFuncionActual + "_retorno_final;");
        } else {
            if (valor.startsWith("stack[")) {
                String dir = valor.substring(6, valor.length() - 1);
                valor = "stack[" + dir + "].i";
            } else if (esTemporal(valor)) {
                valor = valor + ".i";
            }
            escritor.linea("valorRetorno = " + valor + ";");
            escritor.linea("goto " + nombreFuncionActual + "_retorno_final;");
        }
    }

    // ============================================================
    // PARAM / CALL / NEW
    // ============================================================

    private void traducirParam(CuartetaV1 c) {
        String valor = c.arg1();

        if (valor.startsWith("stack[")) {
            String dir = valor.substring(6, valor.length() - 1);
            valor = "stack[" + dir + "].i";
        } else if (esTemporal(valor)) {
            valor = valor + ".i";
        }

        String campo = valor.startsWith("\"") ? ".s" : ".i";
        escritor.linea("stack[SP++]" + campo + " = " + valor + ";");
    }

    private void traducirCall(CuartetaV1 c) {
        String nombre = c.arg1();
        String resultado = c.resultado();

        int idRetorno = ++contadorRetornos;
        String etiquetaRetorno = "L_continuar_" + idRetorno;

        String labelFuncion;
        if (nombre.contains(".")) {
            labelFuncion = "L_Z_" + nombre.replace(".", "_");
        } else {
            labelFuncion = "L_Y_" + nombre;
        }

        escritor.linea("pilaRetorno[ptrRetorno++] = &&" + etiquetaRetorno + ";");
        escritor.linea("goto " + labelFuncion + ";");
        escritor.linea(etiquetaRetorno + ":;");
        declararTemporal(resultado);
        escritor.linea(resultado + ".i = valorRetorno;");
    }

    private void traducirNew(CuartetaV1 c) {
        String tipo = c.arg1();
        String resultado = c.resultado();

        declararTemporal(resultado);
        escritor.linea(resultado + " = alloc_heap(sizeof(" + tipo + "));");
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private void declararTemporal(String nombre) {
        if (temporalesDeclarados.contains(nombre)) return;
        temporalesDeclarados.add(nombre);
        escritor.linea("Valor " + nombre + ";");
    }
    private boolean esTemporal(String s) {
        return s != null && s.startsWith("t") && s.length() > 1
                && Character.isDigit(s.charAt(1));
    }

    public void setNombreFuncionActual(String nombre) {
        this.nombreFuncionActual = nombre;
    }
}