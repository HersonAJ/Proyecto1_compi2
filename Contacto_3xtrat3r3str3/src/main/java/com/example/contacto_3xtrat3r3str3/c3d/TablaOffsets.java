package com.example.contacto_3xtrat3r3str3.c3d;

import java.util.LinkedHashMap;
import java.util.Map;

public class TablaOffsets {

    private final Map<String, Integer> offsets = new LinkedHashMap<>();
            private int siguienteOffset = 0;

    //registra una variable como un offset, si la variable ya existe, devuelve su offset
    public int registrar(String nombre) {
        if (offsets.containsKey(nombre)) {
            return offsets.get(nombre);
        }
        int offset = siguienteOffset++;
        offsets.put(nombre, offset);
        return offset;
    }

    // obtiene el offset se una variable, devuelve -1 si no existe
    public int obtener(String nombre) {
        return offsets.getOrDefault(nombre, -1);
    }

    //devuelce el numero total de offsets asignados
    public int total() {
        return siguienteOffset;
    }

    //reinicia la tabla
    public void reiniciar() {
        offsets.clear();
        siguienteOffset = 0;
    }
}
