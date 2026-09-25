package com.example.contacto_3xtrat3r3str3.c3d_v2.c.z;

import com.example.contacto_3xtrat3r3str3.c3d_v2.AccesoMemoria;
import com.example.contacto_3xtrat3r3str3.c3d_v2.Cuarteta;

import java.util.List;

/**
 * Instanciación de objeto: 'new Persona("Carlos", 25)'.
 *   struct Persona* t0 = malloc(sizeof(struct Persona));
 *   Persona_constructor_String_int(t0, "Carlos", 25);
 */
public class NewObjeto extends Cuarteta {

    private final AccesoMemoria destino;         // el temporal que recibe el puntero
    private final String nombreClase;            // "Persona"
    private final String nombreConstructorC;     // "Persona_constructor_String_int"
    private final List<AccesoMemoria> argumentos;

    public NewObjeto(AccesoMemoria destino,
                      String nombreClase,
                      String nombreConstructorC,
                      List<AccesoMemoria> argumentos) {
        this.destino = destino;
        this.nombreClase = nombreClase;
        this.nombreConstructorC = nombreConstructorC;
        this.argumentos = argumentos;
    }

    public AccesoMemoria getDestino()             { return destino; }
    public String getNombreClase()                { return nombreClase; }
    public String getNombreConstructorC()         { return nombreConstructorC; }
    public List<AccesoMemoria> getArgumentos()    { return argumentos; }

    @Override
    public void aCodigoC(StringBuilder sb) {
        // 1. Reservar memoria
        sb.append("    ");
        destino.aCodigoC(sb);
        sb.append(" = malloc(sizeof(struct ").append(nombreClase).append("));\n");

        // 2. Llamar al constructor
        sb.append("    ").append(nombreConstructorC).append('(');
        destino.aCodigoC(sb);
        for (AccesoMemoria arg : argumentos) {
            sb.append(", ");
            arg.aCodigoC(sb);
        }
        sb.append(");\n");
    }
}