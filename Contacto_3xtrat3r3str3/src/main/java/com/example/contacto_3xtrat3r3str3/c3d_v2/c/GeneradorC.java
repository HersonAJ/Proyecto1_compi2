package com.example.contacto_3xtrat3r3str3.c3d_v2.c;

import com.example.contacto_3xtrat3r3str3.c3d_v2.Cuarteta;


import java.util.List;

public class GeneradorC {

    private final StringBuilder sb;

    public GeneradorC() {
        this.sb = new StringBuilder();
    }

    public String generar(List<FuncionC> funciones, boolean incluirMain) {
        sb.setLength(0);

        escribirCabeceraArchivo();
        escribirEstructuras();
        escribirPrototipos(funciones);

        for (FuncionC f : funciones) {
            escribirFuncion(f);
        }

        if (incluirMain) {
            escribirMain(funciones);
        }

        return sb.toString();
    }

    private void escribirCabeceraArchivo() {
        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdlib.h>\n");
        sb.append("#include <string.h>\n\n");
    }

    private void escribirEstructuras() {
        // TODO: se conectará con las estructuras del AST.
    }

    private void escribirPrototipos(List<FuncionC> funciones) {
        for (FuncionC f : funciones) {
            f.escribirCabecera(sb);
            sb.append(";\n");
        }
        sb.append('\n');
    }

    private void escribirFuncion(FuncionC f) {
        f.escribirCabecera(sb);
        sb.append(" {\n");

        // 1. Declaración de variables locales.
        for (VariableLocalC v : f.getVariablesLocales()) {
            sb.append("    ");
            v.aCodigoC(sb);
        }
        if (!f.getVariablesLocales().isEmpty()) {
            sb.append('\n');
        }

        // 2. Declaración de temporales.
        List<String> tipos = f.getTiposTemporales();
        for (int i = 0; i < tipos.size(); i++) {
            sb.append("    ").append(TipoC.primitivoAC(tipos.get(i)))
                    .append(" t").append(i).append(";\n");
        }
        if (!tipos.isEmpty()) {
            sb.append('\n');
        }

        // 3. Cuádruplas.
        for (Cuarteta c : f.getCuartetas()) {
            c.aCodigoC(sb);
        }

        sb.append("}\n\n");
    }

    private void escribirMain(List<FuncionC> funciones) {
        sb.append("int main(void) {\n");
        for (FuncionC f : funciones) {
            sb.append("    ").append(f.getNombre()).append("();\n");
        }
        sb.append("    return 0;\n");
        sb.append("}\n");
    }
}