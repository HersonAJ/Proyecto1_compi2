package com.example.contacto_3xtrat3r3str3.generadorC;

import com.example.contacto_3xtrat3r3str3.c3d.CuartetaV1;

import java.util.HashSet;
import java.util.Set;

public class TraductorEstructuras {

    private final EscritorC escritor;
    private final Set<String> nombresYaTraducidos = new HashSet<>();
    private String nombreStructActual;
    private boolean saltandoStruct = false;

    public TraductorEstructuras(EscritorC escritor) {
        this.escritor = escritor;
    }

    public boolean procesar(CuartetaV1 c) {
        String op = c.operador();

        // Si estamos saltando un struct, ignoramos todo hasta end-struct
        if (saltandoStruct) {
            if (op.equals("end-struct") || op.equals("end-class")) {
                saltandoStruct = false;
            }
            return true;
        }

        switch (op) {
            case "struct":
            case "class":
                String nombre = c.arg1();
                if (nombresYaTraducidos.contains(nombre)) {
                    // Ya existe, saltar hasta end-struct
                    saltandoStruct = true;
                    return true;
                }
                nombresYaTraducidos.add(nombre);
                nombreStructActual = nombre;
                escritor.linea("typedef struct " + nombre + " {");
                escritor.indentar();
                return true;

            case "campo":
                String[] partes = c.arg1().split(" ", 2);
                if (partes.length == 2) {
                    String tipoC = mapearTipo(partes[0]);
                    String resto = partes[1];
                    escritor.linea(tipoC + " " + resto + ";");
                }
                return true;

            case "end-struct":
            case "end-class":
                escritor.desindentar();
                escritor.linea("} " + nombreStructActual + ";");
                nombreStructActual = null;
                return true;
        }

        return false;
    }

    public static String mapearTipo(String tipo) {
        return switch (tipo) {
            case "entero", "int", "numerus" -> "int";
            case "flotante", "double", "decimalis" -> "double";
            case "caracter", "char", "littera" -> "char";
            case "cadena", "String", "textum" -> "char*";
            case "bool", "boolean" -> "int";
            default -> tipo;
        };
    }
}