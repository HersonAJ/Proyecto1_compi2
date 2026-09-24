package com.example.contacto_3xtrat3r3str3.c3d;

import com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorPig.GeneradorC3DPig;
import com.example.contacto_3xtrat3r3str3.piglatin.builder.ASTBuilderPig;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoAST;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoPrograma;
import com.example.contacto_3xtrat3r3str3.piglatin.semantica.ValidadorSemanticoPig;
import com.example.piglatin.analizador.gramatica.PigLexer;
import com.example.piglatin.analizador.gramatica.PigParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PruebaC3DPig {

    public static void main(String[] args) {
        Path ruta = Paths.get("src/main/resources/ejemplos/EjemploPig.pig");

        if (!Files.exists(ruta)) {
            System.out.println("No existe: " + ruta.toAbsolutePath());
            return;
        }

        String codigo;
        try {
            codigo = Files.readString(ruta);
        } catch (IOException e) {
            System.out.println("Error al leer: " + e.getMessage());
            return;
        }

        //1. lexer
        PigLexer lexer = new PigLexer(CharStreams.fromString(codigo));
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        //2 parser
        PigParser parser = new PigParser(tokens);
        parser.removeErrorListeners();
        PigParser.ProgramaContext tree = parser.programa();

        //3. AST
        ASTBuilderPig builder = new ASTBuilderPig();
        NodoAST nodo = builder.visit(tree);
        if (!(nodo instanceof NodoPrograma programa)) {
            System.out.println("Error al construir el AST: ");
            return;
        }

        Path carpetaRaiz = Paths.get("src/main/resources");
        ValidadorSemanticoPig semantica = new ValidadorSemanticoPig(carpetaRaiz);
        semantica.analizar(programa);

        GeneradorC3DPig generador = new GeneradorC3DPig();
        List<Cuarteta> cuartetas = generador.generar(programa);

        //6 imprimir
        System.out.println("=== CUARTETAS GENERADAS ===");
        for (int i = 0; i < cuartetas.size(); i++) {
            System.out.printf("%3d: %s%n", i + 1, cuartetas.get(i));
        }
        System.out.println("Total: " + cuartetas.size());
    }
}
