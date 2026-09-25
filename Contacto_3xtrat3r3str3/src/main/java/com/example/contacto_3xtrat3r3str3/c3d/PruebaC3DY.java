package com.example.contacto_3xtrat3r3str3.c3d;

import com.example.contacto_3xtrat3r3str3.c3d_v2.c.FuncionC;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.GeneradorArchivoC;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.GeneradorC;
import com.example.contacto_3xtrat3r3str3.y.Builder.ASTBuilder;
import com.example.contacto_3xtrat3r3str3.y.analizador.PreprocesadorIndentacion;
import com.example.contacto_3xtrat3r3str3.y.ast.NodoAST;
import com.example.contacto_3xtrat3r3str3.y.ast.NodoPrograma;
import com.example.contacto_3xtrat3r3str3.y.semantica.ValidadorSemantico;
import com.example.y.analizador.gramatica.YLexer;
import com.example.y.analizador.gramatica.YParser;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PruebaC3DY {

    public static void main(String[] args) {
        Path ruta = Paths.get("src/main/resources/ejemplos/Funciones.y");

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

        // 1. Preprocesador
        String codigoPreprocesado = new PreprocesadorIndentacion().preprocesar(codigo);

        // 2. Lexer
        YLexer lexer = new YLexer(CharStreams.fromString(codigoPreprocesado));
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        // 3. Parser
        YParser parser = new YParser(tokens);
        parser.removeErrorListeners();
        YParser.ProgramaContext tree = parser.programa();

        // 4. AST
        ASTBuilder builder = new ASTBuilder();
        NodoAST nodo = builder.visit(tree);
        if (!(nodo instanceof NodoPrograma.Programa programa)) {
            System.err.println("Error al construir AST");
            return;
        }

        // 5. Semántica
        ValidadorSemantico semantica = new ValidadorSemantico();
        semantica.analizar(programa);

        // 6. Generar cuádruplas + FuncionC
        List<FuncionC> funcionesC = programa.aFuncionesC(semantica.getTabla());

        // 7. Generar C
        String codigoC = new GeneradorC().generar(funcionesC, false);

        // 8. Imprimir C
        System.out.println("=== CÓDIGO C GENERADO ===");
        System.out.println(codigoC);

        // 9. Escribir y compilar
        new GeneradorArchivoC().generarYCompilar(codigoC);
    }
}