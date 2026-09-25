package com.example.contacto_3xtrat3r3str3.generadorC;

import com.example.contacto_3xtrat3r3str3.c3d.CuartetaV1;

/**
 * Maneja las cuartetas que delimitan funciones/bloques:
 *   - func / end-func
 *   - constructor / end-constructor
 *   - method / end-method
 *   - main / end-main
 */
public class TraductorFunciones {

    private final EscritorC escritor;
    private final TraductorCuartetas traductorCuartetas;
    private String nombreBloqueActual;

    public TraductorFunciones(EscritorC escritor, TraductorCuartetas traductorCuartetas) {
        this.escritor = escritor;
        this.traductorCuartetas = traductorCuartetas;
    }

    public boolean procesar(CuartetaV1 c) {
        String op = c.operador();

        switch (op) {
            case "func":
                return iniciarFuncion(c);
            case "constructor":
                return iniciarConstructor(c);
            case "method":
                return iniciarMetodo(c);
            case "main":
                return iniciarMain();
            case "end-func", "end-constructor", "end-method":
                return cerrarBloque(c);
            case "end-main":
                return cerrarMain();
            default:
                return false;
        }
    }

    // ============================================================
    // INICIOS DE BLOQUE
    // ============================================================

    private boolean iniciarFuncion(CuartetaV1 c) {
        String etiqueta = c.arg1();
        nombreBloqueActual = etiqueta;

        traductorCuartetas.setNombreFuncionActual(etiqueta);

        escritor.vacia();
        escritor.linea("// ===== Funcion " + c.resultado() + " =====");
        escritor.linea(etiqueta + ":;");
        return true;
    }

    private boolean iniciarConstructor(CuartetaV1 c) {
        String etiqueta = c.arg1();
        nombreBloqueActual = etiqueta;

        traductorCuartetas.setNombreFuncionActual(etiqueta);

        escritor.vacia();
        escritor.linea("// ===== Constructor " + c.resultado() + " =====");
        escritor.linea(etiqueta + ":;");
        return true;
    }

    private boolean iniciarMetodo(CuartetaV1 c) {
        String etiqueta = c.arg1();
        nombreBloqueActual = etiqueta;

        traductorCuartetas.setNombreFuncionActual(etiqueta);

        escritor.vacia();
        escritor.linea("// ===== Metodo " + c.resultado() + " =====");
        escritor.linea(etiqueta + ":;");
        return true;
    }

    private boolean iniciarMain() {
        traductorCuartetas.setNombreFuncionActual("L_Pig_main");

        escritor.vacia();
        escritor.linea("// ===== MAIN =====");
        escritor.linea("L_Pig_main:;");
        return true;
    }

    // ============================================================
    // CIERRES DE BLOQUE
    // ============================================================

    private boolean cerrarBloque(CuartetaV1 c) {
        // Etiqueta única por función
        escritor.linea(nombreBloqueActual + "_retorno_final:;");
        escritor.linea("goto *pilaRetorno[--ptrRetorno];");
        nombreBloqueActual = null;
        return true;
    }

    private boolean cerrarMain() {
        escritor.linea("L_Pig_main_retorno:;");
        escritor.linea("return 0;");
        return true;
    }
}