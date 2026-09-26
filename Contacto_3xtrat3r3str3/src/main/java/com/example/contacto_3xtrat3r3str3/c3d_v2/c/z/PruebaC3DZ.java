package com.example.contacto_3xtrat3r3str3.c3d_v2.c.z;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.EstructuraC;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.FuncionC;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.GeneradorArchivoC;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.GeneradorC;
import com.example.contacto_3xtrat3r3str3.zetariano.Builder.ASTBuilderZ;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoAST;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoPrograma;
import com.example.contacto_3xtrat3r3str3.zetariano.semantica.ValidadorSemanticoZ;
import com.example.zetariano.analizador.gramatica.ZLexer;
import com.example.zetariano.analizador.gramatica.ZParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PruebaC3DZ {

    public static void main(String[] args) {
        Path ruta = Paths.get("src/main/resources/ejemplos/Persona.z");

        if (!Files.exists(ruta)) {
            System.err.println("No existe: " + ruta.toAbsolutePath());
            return;
        }

        String codigo;
        try {
            codigo = Files.readString(ruta);
        } catch (IOException e) {
            System.err.println("Error al leer: " + e.getMessage());
            return;
        }

        // 1. Lexer
        ZLexer lexer = new ZLexer(CharStreams.fromString(codigo));
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        // 2. Parser
        ZParser parser = new ZParser(tokens);
        ZParser.ProgramaContext tree = parser.programa();

        // 3. AST
        ASTBuilderZ builder = new ASTBuilderZ();
        NodoAST nodo = builder.visit(tree);
        if (!(nodo instanceof NodoPrograma programa)) {
            System.err.println("Error al construir AST");
            return;
        }

        // 4. Semántica
        ValidadorSemanticoZ semantica = new ValidadorSemanticoZ();
        semantica.analizar(programa);

        System.out.println("=== ERRORES SEMÁNTICOS ===");
        for (var e : semantica.getErrores()) {
            System.out.println(e);
        }
        System.out.println("Total errores: " + semantica.getErrores().size());

        // 5. Generar C
        List<EstructuraC> estructurasC = programa.aEstructurasC();
        List<FuncionC> funcionesC = programa.aFuncionesC(semantica.getTabla());

        String codigoC = new GeneradorC().generar(funcionesC, estructurasC,
                new java.util.ArrayList<>(), false);

        System.out.println("\n=== CÓDIGO C GENERADO ===");
        System.out.println(codigoC);

        // 6. Escribir y compilar
        new GeneradorArchivoC().generarYCompilar(codigoC);
    }
}
