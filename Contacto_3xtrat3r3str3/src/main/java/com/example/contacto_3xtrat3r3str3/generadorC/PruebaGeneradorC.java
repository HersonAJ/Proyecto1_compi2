package com.example.contacto_3xtrat3r3str3.generadorC;

import com.example.contacto_3xtrat3r3str3.c3d.Cuarteta;
import com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorPig.GeneradorC3DPig;
import com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorY.GeneradorC3DY;
import com.example.contacto_3xtrat3r3str3.c3d.generadores.generadorZ.GeneradorC3DZ;
import com.example.contacto_3xtrat3r3str3.piglatin.builder.ASTBuilderPig;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoPrograma;
import com.example.contacto_3xtrat3r3str3.piglatin.semantica.ValidadorSemanticoPig;
import com.example.contacto_3xtrat3r3str3.y.Builder.ASTBuilder;
import com.example.contacto_3xtrat3r3str3.y.analizador.PreprocesadorIndentacion;
import com.example.contacto_3xtrat3r3str3.y.ast.NodoAST;
import com.example.y.analizador.gramatica.YLexer;
import com.example.y.analizador.gramatica.YParser;
import com.example.piglatin.analizador.gramatica.PigLexer;
import com.example.piglatin.analizador.gramatica.PigParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Prueba del pipeline completo:
 *   1. Compila Funciones.y con Y?.
 *   2. Compila Persona.z con Z.
 *   3. Compila programa.pig con PigLatin.
 *   4. Genera el archivo .c con GeneradorC.
 *   5. Compila el .c con gcc.
 *   6. Ejecuta el binario.
 */
public class PruebaGeneradorC {

    public static void main(String[] args) {
        Path carpetaRaiz = Paths.get("src/main/resources");

        try {
            // 1. Compilar Funciones.y con Y?
            System.out.println("=== Compilando Funciones.y ===");
            List<Cuarteta> cuartetasY = compilarY(
                    carpetaRaiz.resolve("ejemplos/Funciones.y"));
            System.out.println("Cuartetas de Y?: " + cuartetasY.size());

            // 2. Compilar Persona.z con Z
            System.out.println("\n=== Compilando Persona.z ===");
            List<Cuarteta> cuartetasZ = compilarZ(
                    carpetaRaiz.resolve("ejemplos/EjemploZ.z"));
            System.out.println("Cuartetas de Z: " + cuartetasZ.size());

            // 3. Compilar programa.pig con PigLatin
            System.out.println("\n=== Compilando programa.pig ===");
            List<Cuarteta> cuartetasPig = compilarPig(
                    carpetaRaiz.resolve("ejemplos/EjemploPig.pig"),
                    carpetaRaiz);
            System.out.println("Cuartetas de PigLatin: " + cuartetasPig.size());

            // 4. Generar el archivo .c
            System.out.println("\n=== Generando archivo .c ===");
            Path rutaC = Paths.get("output/programa.c");
            Files.createDirectories(rutaC.getParent());

            GeneradorC generadorC = new GeneradorC();
            generadorC.generar(cuartetasY, cuartetasZ, cuartetasPig, rutaC);
            System.out.println("Archivo generado: " + rutaC.toAbsolutePath());

            // 5. Compilar con gcc
            System.out.println("\n=== Compilando con gcc ===");
            compilarConGcc(rutaC);

            // 6. Ejecutar el binario
            System.out.println("\n=== Ejecutando binario ===");
            ejecutarBinario();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================================
    // COMPILAR Y?
    // ============================================================

    private static List<Cuarteta> compilarY(Path ruta) throws IOException {
        String codigo = Files.readString(ruta);

        // Preprocesador
        String codigoPre = new PreprocesadorIndentacion().preprocesar(codigo);

        // Lexer
        YLexer lexer = new YLexer(CharStreams.fromString(codigoPre));
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        // Parser
        YParser parser = new YParser(tokens);
        parser.removeErrorListeners();
        YParser.ProgramaContext tree = parser.programa();

        // AST
        ASTBuilder builder = new ASTBuilder();
        NodoAST nodo = builder.visit(tree);
        if (!(nodo instanceof com.example.contacto_3xtrat3r3str3.y.ast.NodoPrograma.Programa programa)) {
            throw new RuntimeException("Error al construir AST de Y?");
        }

        // Generar cuartetas
        GeneradorC3DY generador = new GeneradorC3DY();
        return generador.generar(programa);
    }

    // ============================================================
    // COMPILAR Z
    // ============================================================

    private static List<Cuarteta> compilarZ(Path ruta) throws IOException {
        String codigo = Files.readString(ruta);

        // Lexer
        com.example.zetariano.analizador.gramatica.ZLexer lexer =
                new com.example.zetariano.analizador.gramatica.ZLexer(CharStreams.fromString(codigo));
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        // Parser
        com.example.zetariano.analizador.gramatica.ZParser parser =
                new com.example.zetariano.analizador.gramatica.ZParser(tokens);
        parser.removeErrorListeners();
        com.example.zetariano.analizador.gramatica.ZParser.ProgramaContext tree = parser.programa();

        // AST
        com.example.contacto_3xtrat3r3str3.zetariano.Builder.ASTBuilderZ builder =
                new com.example.contacto_3xtrat3r3str3.zetariano.Builder.ASTBuilderZ();
        com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoAST nodo = builder.visit(tree);
        if (!(nodo instanceof com.example.contacto_3xtrat3r3str3.zetariano.nodo.NodoPrograma programa)) {
            throw new RuntimeException("Error al construir AST de Z");
        }

        // Generar cuartetas
        GeneradorC3DZ generador = new GeneradorC3DZ();
        return generador.generar(programa);
    }

    // ============================================================
    // COMPILAR PIGLATIN
    // ============================================================

    private static List<Cuarteta> compilarPig(Path ruta, Path carpetaRaiz) throws IOException {
        String codigo = Files.readString(ruta);

        // Lexer
        PigLexer lexer = new PigLexer(CharStreams.fromString(codigo));
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        // Parser
        PigParser parser = new PigParser(tokens);
        parser.removeErrorListeners();
        PigParser.ProgramaContext tree = parser.programa();

        // AST
        ASTBuilderPig builder = new ASTBuilderPig();
        com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoAST nodo = builder.visit(tree);
        if (!(nodo instanceof NodoPrograma programa)) {
            throw new RuntimeException("Error al construir AST de PigLatin");
        }

        // Análisis semántico (necesario para poblar la tabla)
        ValidadorSemanticoPig semantica = new ValidadorSemanticoPig(carpetaRaiz);
        semantica.analizar(programa);

        // Generar cuartetas
        GeneradorC3DPig generador = new GeneradorC3DPig();
        return generador.generar(programa);
    }

    // ============================================================
    // COMPILAR CON GCC
    // ============================================================

    private static void compilarConGcc(Path rutaC) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "gcc", "-std=gnu99", "-o", "output/programa", rutaC.toString()
        );
        pb.inheritIO();
        Process proceso = pb.start();
        int exitCode = proceso.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Error al compilar con gcc (exit code " + exitCode + ")");
        }
        System.out.println("Compilacion exitosa.");
    }

    // ============================================================
    // EJECUTAR BINARIO
    // ============================================================

    private static void ejecutarBinario() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("./output/programa");
        pb.inheritIO();
        Process proceso = pb.start();
        proceso.waitFor();
    }
}