package com.example.contacto_3xtrat3r3str3.piglatin;

import com.example.contacto_3xtrat3r3str3.piglatin.builder.ASTBuilderPig;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoAST;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoPrograma;
import com.example.piglatin.analizador.gramatica.PigLexer;
import com.example.piglatin.analizador.gramatica.PigParser;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PruebaPig {

    public static void main(String[] args) {
        Path ruta = Paths.get("src/main/resources/ejemplos/ejemplo.pig");

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

        // 1. LEXER
        PigLexer lexer = new PigLexer(CharStreams.fromString(codigo));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg,
                                    RecognitionException e) {
                System.err.println("Error lexico [" + line + ":" + charPositionInLine + "] " + msg);
            }
        });

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        // 2. PARSER
        PigParser parser = new PigParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg,
                                    RecognitionException e) {
                System.err.println("Error sintactico [" + line + ":" + charPositionInLine + "] " + msg);
            }
        });

        PigParser.ProgramaContext tree = parser.programa();

        // 3. AST
        ASTBuilderPig builder = new ASTBuilderPig();
        NodoAST nodo = builder.visit(tree);

        if (nodo instanceof NodoPrograma programa) {
            System.out.println("=== AST CONSTRUIDO ===");
            System.out.println("Importaciones: " + programa.importaciones().size());
            System.out.println("Variables globales: " + programa.variablesGlobales().size());
            System.out.println("Sentencias en MAIOR: " + programa.cuerpoMain().size());
        } else {
            System.out.println("Error al construir AST");
        }
    }
}