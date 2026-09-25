package com.example.contacto_3xtrat3r3str3.c3d;

/**
 * Genera nombres de etiquetas únicos.
 * Permite un prefijo para distinguir etiquetas entre lenguajes
 * (Y_, Z_, Pig_) y evitar choques al unificar el código C.
 */
public class TablaEtiquetas {

    private int contador = 0;
    private String prefijo = "";

    public void setPrefijo(String prefijo) {
        this.prefijo = prefijo != null ? prefijo : "";
    }

    /** Devuelve la siguiente etiqueta: <prefijo>L1, <prefijo>L2, ... */
    public String nueva() {
        return prefijo + "L" + (++contador);
    }

    public void reiniciar() {
        contador = 0;
    }
}