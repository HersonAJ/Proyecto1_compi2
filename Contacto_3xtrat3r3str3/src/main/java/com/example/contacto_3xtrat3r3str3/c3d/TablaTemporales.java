package com.example.contacto_3xtrat3r3str3.c3d;

//Genera nombres de temporales unicos
public class TablaTemporales {

    private int contador = 0;

    //Devuelve el siguiente temporal: t1, t2, t3, ...
    public String nuevo() {
        return "t" + (++contador);
    }

    //Devuelve el temporal actual sin avanzar
    public String actual() {
        return "t" + contador;
    }

    //reinicia el contador
    public void reiniciar() {
        contador = 0;
    }
}
