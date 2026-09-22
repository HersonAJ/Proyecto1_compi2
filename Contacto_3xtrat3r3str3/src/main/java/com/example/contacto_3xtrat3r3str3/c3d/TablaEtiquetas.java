package com.example.contacto_3xtrat3r3str3.c3d;

//genera nombres de etiquetas inicos
public class TablaEtiquetas {

    private int contador = 0;

    //devuelve la siguiente etiqueta
    public String nueva() {
        return "L" + (++contador);
    }

    public void reiniciar() {
        contador = 0;
    }
}
