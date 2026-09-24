package com.example.contacto_3xtrat3r3str3.c3d;

//Genera nombres de etiquetas unicos
public class TablaEtiquetas {

    private int contador = 0;

    //Devuelve la siguiente etiqueta: L1, L2, L3, ...
    public String nueva() {
        return "L" + (++contador);
    }

    //reinicia el contador
    public void reiniciar() {
        contador = 0;
    }
}
