package com.example.contacto_3xtrat3r3str3.generadorC;

import com.example.contacto_3xtrat3r3str3.c3d.Cuarteta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Genera el archivo .c a partir de las cuartetas de Y, Z y PigLatin.
 */
public class GeneradorC {

    private final EscritorC escritor;
    private final TraductorEstructuras traductorEstructuras;
    private final TraductorFunciones traductorFunciones;
    private final TraductorCuartetas traductorCuartetas;

    public GeneradorC() {
        this.escritor = new EscritorC();
        this.traductorEstructuras = new TraductorEstructuras(escritor);
        this.traductorCuartetas = new TraductorCuartetas(escritor);
        this.traductorFunciones = new TraductorFunciones(escritor, traductorCuartetas);
    }

    public void generar(List<Cuarteta> cuartetasY,
                        List<Cuarteta> cuartetasZ,
                        List<Cuarteta> cuartetasPig,
                        Path rutaSalida) throws IOException {

        escritor.reiniciar();

        // 1. Preámbulo
        escribirPreambulo();

        // 2. Estructuras (fuera del main, son declaraciones)
        traducirEstructuras(cuartetasY);
        traducirEstructuras(cuartetasZ);

        // 3. Todo el código va DENTRO de int main()
        escritor.linea("int main() {");
        escritor.indentar();

        // 3.1. Salto al inicio del main
        escritor.linea("goto L_Pig_main;");
        escritor.vacia();

        // 3.2. Funciones de Y? y Z
        escritor.linea("// ===== FUNCIONES Y METODOS =====");
        traducirFunciones(cuartetasY);
        traducirFunciones(cuartetasZ);

        // 3.3. Main de PigLatin
        escritor.linea("// ===== MAIN =====");
        traducirCuartetas(cuartetasPig);

        escritor.desindentar();
        escritor.linea("}");
        escritor.vacia();

        // 4. Escribir el archivo
        Files.writeString(rutaSalida, escritor.getCodigo());
    }

    private void escribirPreambulo() {
        escritor.linea("#include <stdio.h>");
        escritor.linea("#include <stdlib.h>");
        escritor.linea("#include <string.h>");
        escritor.vacia();

        escritor.linea("// ============================================================");
        escritor.linea("// STACK Y HEAP");
        escritor.linea("// ============================================================");
        escritor.vacia();

        escritor.linea("typedef union {");
        escritor.indentar();
        escritor.linea("int i;");
        escritor.linea("double d;");
        escritor.linea("char c;");
        escritor.linea("char* s;");
        escritor.linea("void* p;");
        escritor.desindentar();
        escritor.linea("} Valor;");
        escritor.vacia();

        escritor.linea("#define STACK_SIZE 4096");
        escritor.linea("#define HEAP_SIZE 4096");
        escritor.vacia();

        escritor.linea("Valor stack[STACK_SIZE];");
        escritor.linea("int BP = 0;");
        escritor.linea("int SP = 0;");
        escritor.vacia();

        escritor.linea("void* heap[HEAP_SIZE];");
        escritor.linea("int HP = 0;");
        escritor.vacia();

        escritor.linea("int alloc_heap(int size) {");
        escritor.indentar();
        escritor.linea("int dir = HP;");
        escritor.linea("HP += size;");
        escritor.linea("return dir;");
        escritor.desindentar();
        escritor.linea("}");
        escritor.vacia();

        escritor.linea("char* concat(char* a, char* b) {");
        escritor.indentar();
        escritor.linea("char* r = malloc(strlen(a) + strlen(b) + 1);");
        escritor.linea("strcpy(r, a);");
        escritor.linea("strcat(r, b);");
        escritor.linea("return r;");
        escritor.desindentar();
        escritor.linea("}");
        escritor.vacia();

        escritor.linea("// Stack de retornos");
        escritor.linea("void* pilaRetorno[100];");
        escritor.linea("int ptrRetorno = 0;");
        escritor.vacia();

        escritor.linea("// Valor de retorno");
        escritor.linea("int valorRetorno = 0;");
        escritor.vacia();
    }

    private void traducirEstructuras(List<Cuarteta> cuartetas) {
        for (Cuarteta c : cuartetas) {
            traductorEstructuras.procesar(c);
        }
    }

    private void traducirFunciones(List<Cuarteta> cuartetas) {
        for (Cuarteta c : cuartetas) {
            if (traductorFunciones.procesar(c)) {
                continue;
            }
            traductorCuartetas.procesar(c);
        }
    }

    private void traducirCuartetas(List<Cuarteta> cuartetas) {
        for (Cuarteta c : cuartetas) {
            if (traductorFunciones.procesar(c)) {
                continue;
            }
            traductorCuartetas.procesar(c);
        }
    }
}