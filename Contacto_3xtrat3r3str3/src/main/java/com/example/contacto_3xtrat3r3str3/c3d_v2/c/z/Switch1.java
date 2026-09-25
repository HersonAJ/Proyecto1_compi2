package com.example.contacto_3xtrat3r3str3.c3d_v2.c.z;

import com.example.contacto_3xtrat3r3str3.c3d_v2.AccesoMemoria;
import com.example.contacto_3xtrat3r3str3.c3d_v2.Cuarteta;

import java.util.List;

public class Switch1 extends Cuarteta {

    public record Caso(AccesoMemoria valor, List<Cuarteta> cuerpo) {}

    private final AccesoMemoria expresion;
    private final List<Caso> casos;
    private final List<Cuarteta> cuerpoDefault;


    public Switch1(AccesoMemoria expresion,
                   List<Caso> casos,
                   List<Cuarteta> cuerpoDefault) {
        this.expresion = expresion;
        this.casos = casos;
        this.cuerpoDefault = cuerpoDefault;
    }

    @Override
    public void aCodigoC(StringBuilder sb) {
        sb.append("    switch (");
        expresion.aCodigoC(sb);
        sb.append(") {\n");

        for (Caso c : casos) {
            sb.append("        case ");
            c.valor().aCodigoC(sb);
            sb.append(":\n");
            for (Cuarteta cuarteta : c.cuerpo()) {
                escribirIndentado(sb, cuarteta, "            ");
            }
        }

        if (cuerpoDefault != null) {
            sb.append("        default:\n");
            for (Cuarteta cuarteta : cuerpoDefault) {
                escribirIndentado(sb, cuarteta, "            ");
            }
        }

        sb.append("    }\n");
    }

    private void escribirIndentado(StringBuilder sb, Cuarteta cuarteta, String prefijo) {
        StringBuilder sub = new StringBuilder();
        cuarteta.aCodigoC(sub);
        for (String linea : sub.toString().split("\n", -1)) {
            if (!linea.isEmpty()) {
                sb.append(prefijo).append(linea).append('\n');
            }
        }
    }
}