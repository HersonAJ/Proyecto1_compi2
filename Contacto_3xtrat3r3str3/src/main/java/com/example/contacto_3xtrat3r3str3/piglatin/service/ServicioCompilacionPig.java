package com.example.contacto_3xtrat3r3str3.piglatin.service;

import com.example.contacto_3xtrat3r3str3.piglatin.builder.ASTBuilderPig;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoAST;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoPrograma;
import com.example.contacto_3xtrat3r3str3.y.errores.ErrorPosicional;
import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;
import com.example.piglatin.analizador.gramatica.PigLexer;
import com.example.piglatin.analizador.gramatica.PigParser;
import org.antlr.v4.runtime.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquesta el pipeline de compilación para PigLatin:
 *   1. Lexer
 *   2. Parser
 *   3. ASTBuilder
 *   4. Análisis semántico (pendiente)
 *   5. Carga de importaciones (pendiente)
 */
public class ServicioCompilacionPig {

    private static final boolean DEBUG = true;

    public ResultadoCompilacionPig analizar(String codigoFuente) {

        if (DEBUG) {
            System.out.println("=== INICIO ANALISIS PIGLATIN ===");
        }

        if (codigoFuente == null || codigoFuente.trim().isEmpty()) {
            return new ResultadoCompilacionPig(
                    false, null,
                    List.of(), List.of(), List.of(),
                    List.of("El código está vacío")
            );
        }

        try {
            return analizarInterno(codigoFuente);
        } catch (StackOverflowError soe) {
            return new ResultadoCompilacionPig(
                    false, null,
                    List.of(), List.of(), List.of(),
                    List.of("Estructura demasiado profunda o inválida.")
            );
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
            return new ResultadoCompilacionPig(
                    false, null,
                    List.of(), List.of(), List.of(),
                    List.of("Error interno inesperado: " + e.getMessage())
            );
        }
    }

    private ResultadoCompilacionPig analizarInterno(String codigoFuente) {

        // 1. LEXER
        List<ErrorPosicional> erroresLexicos = new ArrayList<>();

        PigLexer lexer = new PigLexer(CharStreams.fromString(codigoFuente));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg,
                                    RecognitionException e) {
                erroresLexicos.add(new ErrorPosicional(line, charPositionInLine, msg));
            }
        });

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        if (!erroresLexicos.isEmpty()) {
            return new ResultadoCompilacionPig(
                    false, null,
                    erroresLexicos, List.of(), List.of(), List.of()
            );
        }

        // 2. PARSER
        PigParser parser = new PigParser(tokens);
        List<ErrorPosicional> erroresSintacticos = new ArrayList<>();
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg,
                                    RecognitionException e) {
                erroresSintacticos.add(new ErrorPosicional(line, charPositionInLine, msg));
            }
        });

        parser.setErrorHandler(new DefaultErrorStrategy());

        ParserRuleContext tree = null;
        try {
            tree = parser.programa();
        } catch (RecognitionException re) {
            erroresSintacticos.add(new ErrorPosicional(-1, -1,
                    "Error sintáctico no recuperable: " + re.getMessage()));
        }

        if (!erroresSintacticos.isEmpty()) {
            return new ResultadoCompilacionPig(
                    false, null,
                    List.of(), erroresSintacticos, List.of(), List.of()
            );
        }

        // 3. AST
        NodoPrograma programa;
        try {
            ASTBuilderPig builder = new ASTBuilderPig();
            NodoAST nodo = builder.visit(tree);
            programa = (nodo instanceof NodoPrograma np) ? np : null;
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
            return new ResultadoCompilacionPig(
                    false, null,
                    List.of(), List.of(), List.of(),
                    List.of("Error al construir el AST: " + e.getMessage())
            );
        }

        if (programa == null) {
            return new ResultadoCompilacionPig(
                    false, null,
                    List.of(), List.of(), List.of(),
                    List.of("El AST resultante es nulo")
            );
        }

        // 4. VALIDACIÓN SEMÁNTICA (pendiente)
        List<ErrorSemantico> erroresSemanticos = new ArrayList<>();

        // 5. RESULTADO
        boolean exitoso = erroresSemanticos.isEmpty();

        if (DEBUG) {
            System.out.println("Análisis completado. Exitoso: " + exitoso);
            System.out.println("=== FIN ANALISIS PIGLATIN ===");
        }

        return new ResultadoCompilacionPig(
                exitoso,
                programa,
                List.of(),
                List.of(),
                erroresSemanticos,
                List.of()
        );
    }
}