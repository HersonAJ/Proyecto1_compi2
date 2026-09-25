package com.example.contacto_3xtrat3r3str3.generadorC;

public class EscritorC {

    private final StringBuilder sb = new StringBuilder();
    private int nivelIdentacion = 0;

    //escribe una linea con identacion
    public void linea(String contenido) {
        for (int i = 0; i < nivelIdentacion; i++) {
            sb.append("    ");
        }

        sb.append(contenido).append("\n");
    }

    //escribe una linea vacia
    public void vacia() {
        sb.append("\n");
    }

    //aumenta la identacion
    public void indentar() {
        nivelIdentacion++;
    }

    //disminuye la indentacion
    public void desindentar() {
        if (nivelIdentacion > 0) nivelIdentacion--;
    }

    //devuelve el codigo completo
    public String getCodigo() {
        return sb.toString();
    }

    //reinicia el escritor
    public void reiniciar() {
        sb.setLength(0);
        nivelIdentacion = 0;
    }
}
