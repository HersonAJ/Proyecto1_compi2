package com.example.contacto_3xtrat3r3str3.c3d_v2.cuartetas.cuartetasY;


/**
 * Decide el tipo resultado de una operación binaria y qué conversiones
 * hacen falta en cada operando, según las reglas de promoción implícita.
 *
 * Jerarquía (de menor a mayor): caracter < entero < flotante
 * bool se trata como entero.
 * cadena solo admite == y != (se maneja fuera de esta clase).
 */
public final class PromocionTipos {

    private PromocionTipos() {}

    /**
     * Resultado de promover dos tipos.
     *   tipoResultado: el tipo del resultado de la operación.
     *   conversionIzq: tipo al que hay que convertir el izquierdo, o null si no hace falta.
     *   conversionDer: tipo al que hay que convertir el derecho, o null si no hace falta.
     */
    public record Resultado(String tipoResultado, String conversionIzq, String conversionDer) {}

    public static Resultado promover(String tipoIzq, String tipoDer) {
        // Normalizamos bool a entero para el cálculo de promociones.
        String izq = normalizar(tipoIzq);
        String der = normalizar(tipoDer);

        // Caso ideal: mismos tipos, sin conversiones.
        if (izq.equals(der)) {
            return new Resultado(tipoResultadoBase(izq), null, null);
        }

        // Si alguno es flotante, resultado flotante.
        if (izq.equals("flotante") || der.equals("flotante")) {
            String convIzq = izq.equals("flotante") ? null : "flotante";
            String convDer = der.equals("flotante") ? null : "flotante";
            return new Resultado("flotante", convIzq, convDer);
        }

        // Si alguno es entero (y el otro caracter), resultado entero.
        if (izq.equals("entero") || der.equals("entero")) {
            String convIzq = izq.equals("entero") ? null : "entero";
            String convDer = der.equals("entero") ? null : "entero";
            return new Resultado("entero", convIzq, convDer);
        }

        // Solo quedan caracter + caracter (ya cubierto arriba) o casos raros.
        return new Resultado(tipoResultadoBase(izq), null, null);
    }

    private static String normalizar(String tipo) {
        return "bool".equals(tipo) ? "entero" : tipo;
    }

    private static String tipoResultadoBase(String tipo) {
        return "bool".equals(tipo) ? "entero" : tipo;
    }
}