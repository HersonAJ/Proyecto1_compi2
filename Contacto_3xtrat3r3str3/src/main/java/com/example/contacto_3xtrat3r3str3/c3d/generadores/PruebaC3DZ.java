package com.example.contacto_3xtrat3r3str3.c3d.generadores;

import com.example.contacto_3xtrat3r3str3.c3d.Cuarteta;
import com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorZ.GeneradorC3DZ;
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
        Path ruta = Paths.get("src/main/resources/ejemplos/EjemploZ.z");

        if (!Files.exists(ruta)) {
            System.out.println("No existe:  " + ruta.toAbsolutePath());
            return;
        }

        String codigo;
        try {
            codigo = Files.readString(ruta);
        } catch (IOException e) {
            System.out.println("Error al leer: "  + e.getMessage());
            return;
        }

        //1. lexer
        ZLexer lexer = new ZLexer(CharStreams.fromString(codigo));
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        //2.parser
        ZParser parser = new ZParser(tokens);
        parser.removeErrorListeners();
        ZParser.ProgramaContext tree = parser.programa();

        //3. AST

        ASTBuilderZ builder  = new ASTBuilderZ();
        NodoAST nodo = builder.visit(tree);
        if (!(nodo instanceof NodoPrograma programa)) {
            System.out.println("Error al construir AST");
            return;
        }

        ValidadorSemanticoZ semantica = new ValidadorSemanticoZ();
        semantica.analizar(programa);
        //5.
        GeneradorC3DZ generador = new GeneradorC3DZ();
        List<Cuarteta> cuartetas = generador.generar(programa);

        //6 imprimir
        System.out.println("=== CUARTETAS GENERADAS ===");
        for (int i = 0; i < cuartetas.size(); i++) {
            System.out.printf("%3d: %s%n", i + 1, cuartetas.get(i));
        }
        System.out.println("Total: " + cuartetas.size());
    }
}
