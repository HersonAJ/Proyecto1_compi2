package com.example.contacto_3xtrat3r3str3.c3d;

//genera nombres de temporales unicos
public class TablaTemporales {

    private int contador = 0;

    //devuelve el siguiente valor de temporal
    public String nuevo() {
        return "t" + (++contador);
    }

    //reinicia el contador
    public void reiniciar() {
        contador = 0;
    }
}
