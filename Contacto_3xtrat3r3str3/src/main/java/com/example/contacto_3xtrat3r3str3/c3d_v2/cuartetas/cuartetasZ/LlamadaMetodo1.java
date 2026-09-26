package com.example.contacto_3xtrat3r3str3.c3d_v2.cuartetas.cuartetasZ;

import com.example.contacto_3xtrat3r3str3.c3d_v2.cuartetas.genericas.AccesoMemoria;
import com.example.contacto_3xtrat3r3str3.c3d_v2.cuartetas.genericas.Cuarteta;

import java.util.List;

/**
 * Llamada a metodo con receptor explícito:
 *   obj.metodo(args)
 * Se traduce a:
 *   Clase_metodo_<tipos>(obj, args)
 * donde 'obj' es el receptor, que va como primer argumento.
 */
public class LlamadaMetodo1 extends Cuarteta {

    private final AccesoMemoria destino;      // puede ser null (metodo void)
    private final AccesoMemoria receptor;
    private final String nombreMetodo;
    private final List<AccesoMemoria> argumentos;

    public LlamadaMetodo1(AccesoMemoria destino,
                          AccesoMemoria receptor,
                          String nombreMetodo,
                          List<AccesoMemoria> argumentos) {
        this.destino = destino;
        this.receptor = receptor;
        this.nombreMetodo = nombreMetodo;
        this.argumentos = argumentos;
    }

    public AccesoMemoria getDestino()             { return destino; }
    public AccesoMemoria getReceptor()            { return receptor; }
    public String getNombreMetodo()               { return nombreMetodo; }
    public List<AccesoMemoria> getArgumentos()    { return argumentos; }

    @Override
    public void aCodigoC(StringBuilder sb) {
        sb.append("    ");
        if (destino != null) {
            destino.aCodigoC(sb);
            sb.append(" = ");
        }
        sb.append(nombreMetodo).append('(');
        receptor.aCodigoC(sb);
        for (AccesoMemoria arg : argumentos) {
            sb.append(", ");
            arg.aCodigoC(sb);
        }
        sb.append(");\n");
    }
}