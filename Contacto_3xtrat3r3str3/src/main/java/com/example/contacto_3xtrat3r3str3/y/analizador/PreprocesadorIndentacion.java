package com.example.contacto_3xtrat3r3str3.y.analizador;

import java.util.ArrayList;
import java.util.List;

public class PreprocesadorIndentacion {

    private final List<Integer> pilaIndentacion = new ArrayList<>();

    public String preprocesar(String codigoFuente) {
        pilaIndentacion.clear();
        pilaIndentacion.add(0); //nivel base

        StringBuilder salida = new StringBuilder();

        //separar por saldos de linea
        String[] lineas = codigoFuente.split("\\r?\\n", -1);

        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i];

            //se ignoran las lineas totalmente vacias o solo con espacios
            if (linea.trim().isEmpty()) {
                continue;
            }

            //ignorar lineas que solo son comentarios
            if(esSoloComentario(linea)) {
                continue;
            }

            //calcular el nivel de indentacion, contar los tabs
            int nivelActual = contarTabsIniciales(linea);

            //validar que no haya espacios al inicio solo tabs
            validarSinEspaciosIniciales(linea);

            //comparar con el nivel anterior de la pila
            int nivelAnterior = pilaIndentacion.get(pilaIndentacion.size() -1);

            if (nivelActual > nivelAnterior) {
                //aunto la indentacion : un INDENT por cada nivel extra
                for (int n = nivelAnterior; n < nivelActual; n++) {
                    salida.append("<INDENT>");
                    pilaIndentacion.add(nivelAnterior + 1);
                }
            } else if ( nivelActual < nivelAnterior) {
                //disminuyo: un DEDENT por cada nivel que se baja
                while (pilaIndentacion.get(pilaIndentacion.size() - 1) > nivelActual) {
                    salida.append("<DEDENT>");
                    pilaIndentacion.remove(pilaIndentacion.size() - 1);
                }
            }

            //quitar la indentacion inicial y agregar el contenido
            String contenido = linea.substring(nivelActual).stripTrailing();
            salida.append(contenido).append("<NEWLINE>");
        }

        //al final del archivo, cerrar todos los niveles abiertos
        while (pilaIndentacion.size() > 1) {
            salida.append("<DEDENT>");
            pilaIndentacion.remove(pilaIndentacion.size() - 1);
        }

        return salida.toString();
    }

    //contador de tabs al inicio
    private int contarTabsIniciales(String linea) {
        int contador = 0;
        while (contador < linea.length() && linea.charAt(contador) == '\t') {
            contador++;
        }
        return contador;
    }

    //verificacion de espacios en blanco antes del primer tab
    private void validarSinEspaciosIniciales(String linea) {
        int i = 0;

        while (i < linea.length() && linea.charAt(i) == '\t') {
            i++;
        }

        //si despues de los tabs viene un espacio, es una mezcla invalida
        if (i < linea.length() && linea.charAt(i) == ' ') {
            throw new RuntimeException(
                    "Indentacion invalida: se encontro espacio despues de tab. " +
                            "Solo se permiten tabulaciones al inicio de linea."
            );
        }
    }

    //determinar si la linea tiene unicamente comentarios
    private boolean esSoloComentario(String linea) {
        String sinIndentacion = linea.stripLeading();
        return sinIndentacion.startsWith("//") || sinIndentacion.startsWith("/*");
    }
}
