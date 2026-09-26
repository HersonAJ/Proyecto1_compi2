package com.example.contacto_3xtrat3r3str3.c3d_v2.c;

import com.example.contacto_3xtrat3r3str3.c3d_v2.CompiladorPig;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PruebaC3DPig {

    public static void main(String[] args) {
        Path carpetaRaiz = Paths.get("src/main/resources");
        Path rutaPig = carpetaRaiz.resolve("ejemplos/Programa.pig");

        try {
            new CompiladorPig(carpetaRaiz).compilar(rutaPig);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
