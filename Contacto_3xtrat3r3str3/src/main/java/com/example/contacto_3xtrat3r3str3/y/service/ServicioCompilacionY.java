package com.example.contacto_3xtrat3r3str3.y.service;

import com.example.contacto_3xtrat3r3str3.y.Builder.ASTBuilder;
import com.example.contacto_3xtrat3r3str3.y.analizador.PreprocesadorIndentacion;
import com.example.contacto_3xtrat3r3str3.y.ast.NodoAST;
import com.example.contacto_3xtrat3r3str3.y.ast.NodoPrograma;
import com.example.contacto_3xtrat3r3str3.y.errores.ErrorPosicional;
import com.example.contacto_3xtrat3r3str3.y.errores.ResultadoCompilacionY;
import com.example.y.analizador.gramatica.YLexer;
import com.example.y.analizador.gramatica.YParser;
import org.antlr.v4.runtime.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquesta el pipeline de compilación para el lenguaje Y?:
 *
 *   código fuente
 *       ↓
 *   PreprocesadorIndentacion  (tabs -> <INDENT>/<DEDENT>/<NEWLINE>)
 *       ↓
 *   YLexer (ANTLR)
 *       ↓
 *   YParser (ANTLR)
 *       ↓
 *   ASTBuilder
 *       ↓
 *   NodoPrograma
 */
public class ServicioCompilacionY {

    private static final boolean DEBUG = true;

    public ResultadoCompilacionY analizar(String codigoFuente) {

        if (DEBUG) {
            System.out.println("=== INICIO ANALISIS Y? ===");
            System.out.println("Longitud del código: " + (codigoFuente != null ? codigoFuente.length() : 0));
        }

        // Validar entrada vacía
        if (codigoFuente == null || codigoFuente.trim().isEmpty()) {
            return new ResultadoCompilacionY(
                    false, null, null,
                    List.of(),
                    List.of(),
                    List.of("El código está vacío")
            );
        }

        try {
            return analizarInterno(codigoFuente);
        } catch (StackOverflowError soe) {
            if (DEBUG) System.err.println("StackOverflowError durante el análisis");
            return new ResultadoCompilacionY(
                    false, null, null,
                    List.of(), List.of(),
                    List.of("El código produjo una estructura demasiado profunda o inválida.")
            );
        } catch (Exception e) {
            if (DEBUG) {
                System.err.println("Error interno inesperado: " + e.getMessage());
                e.printStackTrace();
            }
            return new ResultadoCompilacionY(
                    false, null, null,
                    List.of(), List.of(),
                    List.of("Error interno inesperado: " + e.getMessage())
            );
        }
    }

    private ResultadoCompilacionY analizarInterno(String codigoFuente) {

        // ============================================================
        // 1. PREPROCESADOR DE INDENTACION
        // ============================================================
        if (DEBUG) System.out.println("1. Preprocesando indentación...");

        List<ErrorPosicional> erroresLexicos = new ArrayList<>();
        String codigoPreprocesado;

        try {
            PreprocesadorIndentacion preprocesador = new PreprocesadorIndentacion();
            codigoPreprocesado = preprocesador.preprocesar(codigoFuente);
            if (DEBUG) {
                System.out.println("   Código preprocesado (" + codigoPreprocesado.length() + " caracteres)");
            }
        } catch (RuntimeException e) {
            // El preprocesador falla por espacios mezclados con tabs.
            erroresLexicos.add(new ErrorPosicional(-1, -1, e.getMessage()));
            if (DEBUG) System.err.println("Error en preprocesador: " + e.getMessage());
            return new ResultadoCompilacionY(
                    false, null, null,
                    erroresLexicos,
                    List.of(),
                    List.of()
            );
        }

        // ============================================================
        // 2. LEXER
        // ============================================================
        if (DEBUG) System.out.println("2. Ejecutando lexer...");

        YLexer lexer = new YLexer(CharStreams.fromString(codigoPreprocesado));

        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg,
                                    RecognitionException e) {
                erroresLexicos.add(new ErrorPosicional(line, charPositionInLine, msg));
                if (DEBUG) System.err.println("Error léxico en línea " + line + ":" + charPositionInLine + " - " + msg);
            }
        });

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        if (!erroresLexicos.isEmpty()) {
            if (DEBUG) System.out.println("   Errores léxicos encontrados: " + erroresLexicos.size());
            return new ResultadoCompilacionY(
                    false, null, codigoPreprocesado,
                    erroresLexicos,
                    List.of(),
                    List.of()
            );
        }

        // ============================================================
        // 3. PARSER
        // ============================================================
        if (DEBUG) System.out.println("3. Ejecutando parser...");

        YParser parser = new YParser(tokens);

        List<ErrorPosicional> erroresSintacticos = new ArrayList<>();
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg,
                                    RecognitionException e) {
                erroresSintacticos.add(new ErrorPosicional(line, charPositionInLine, msg));
                if (DEBUG) System.err.println("Error sintáctico en línea " + line + ":" + charPositionInLine + " - " + msg);
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
            if (DEBUG) System.out.println("   Errores sintácticos encontrados: " + erroresSintacticos.size());
            return new ResultadoCompilacionY(
                    false, null, codigoPreprocesado,
                    List.of(),
                    erroresSintacticos,
                    List.of()
            );
        }

        // ============================================================
        // 4. CONSTRUIR AST
        // ============================================================
        if (DEBUG) System.out.println("4. Construyendo AST...");

        NodoPrograma.Programa programa;
        try {
            ASTBuilder builder = new ASTBuilder();
            NodoAST nodo = builder.visit(tree);
            if (nodo instanceof NodoPrograma.Programa np) {
                programa = np;
            } else {
                programa = null;
            }
        } catch (Exception e) {
            if (DEBUG) {
                System.err.println("Error al construir AST: " + e.getMessage());
                e.printStackTrace();
            }
            return new ResultadoCompilacionY(
                    false, null, codigoPreprocesado,
                    List.of(), List.of(),
                    List.of("Error al construir el AST: " + e.getMessage())
            );
        }

        if (programa == null) {
            return new ResultadoCompilacionY(
                    false, null, codigoPreprocesado,
                    List.of(), List.of(),
                    List.of("El AST resultante es nulo")
            );
        }

        // ============================================================
        // 5. RESULTADO EXITOSO
        // ============================================================
        if (DEBUG) {
            System.out.println("5. Análisis completado con éxito.");
            System.out.println("   Estructuras: " + programa.estructuras().size());
            System.out.println("   Funciones: " + programa.funciones().size());
            System.out.println("=== FIN ANALISIS Y? ===");
        }

        return new ResultadoCompilacionY(
                true,
                programa,
                codigoPreprocesado,
                List.of(),
                List.of(),
                List.of()
        );
    }
}
