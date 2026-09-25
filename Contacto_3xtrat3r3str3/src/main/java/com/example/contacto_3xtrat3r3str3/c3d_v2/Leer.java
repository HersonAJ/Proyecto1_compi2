package com.example.contacto_3xtrat3r3str3.c3d_v2;


/**
 * scanf con formato según el tipo del destino.
 * Necesita la dirección del destino (&destino) para tipos primitivos.
 * Para cadenas: scanf("%s", destino) sin '&' porque ya es puntero.
 */
public class Leer extends Cuarteta {

    private final AccesoMemoria destino;
    private final String tipo;

    public Leer(AccesoMemoria destino, String tipo) {
        this.destino = destino;
        this.tipo = tipo;
    }

    public AccesoMemoria getDestino() { return destino; }
    public String getTipo()           { return tipo; }

    private String formato() {
        return switch (tipo) {
            case "flotante" -> "%f";
            case "caracter" -> "%c";
            case "cadena"   -> "%s";
            default         -> "%d";
        };
    }

    @Override
    public void aCodigoC(StringBuilder sb) {
        sb.append("    scanf(\"").append(formato()).append("\", ");
        if ("cadena".equals(tipo)) {
            destino.aCodigoC(sb);   // ya es puntero
        } else {
            sb.append('&');
            destino.aCodigoC(sb);   // dirección para primitivos
        }
        sb.append(");\n");
    }
}