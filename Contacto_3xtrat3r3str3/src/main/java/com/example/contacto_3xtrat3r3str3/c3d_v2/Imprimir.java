package com.example.contacto_3xtrat3r3str3.c3d_v2;


/**
 * printf con formato según el tipo:
 *   entero   -> %d
 *   flotante -> %f
 *   caracter -> %c
 *   cadena   -> %s
 *   bool     -> %d
 * Además agrega '\n' al final.
 */
public class Imprimir extends Cuarteta {

    private final AccesoMemoria valor;
    private final String tipo;

    public Imprimir(AccesoMemoria valor, String tipo) {
        this.valor = valor;
        this.tipo = tipo;
    }

    public AccesoMemoria getValor() { return valor; }
    public String getTipo()         { return tipo; }

    private String formato() {
        return switch (tipo) {
            case "flotante" -> "%f\\n";
            case "caracter" -> "%c\\n";
            case "cadena"   -> "%s\\n";
            case "bool"     -> "%d\\n";
            default         -> "%d\\n";
        };
    }

    @Override
    public void aCodigoC(StringBuilder sb) {
        sb.append("    printf(\"").append(formato()).append("\", ");
        valor.aCodigoC(sb);
        sb.append(");\n");
    }
}