package com.example.contacto_3xtrat3r3str3.piglatin.service;

import com.example.contacto_3xtrat3r3str3.c3d_v2.c.EstructuraC;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.GeneradorArchivoC;
import com.example.contacto_3xtrat3r3str3.c3d_v2.c.GeneradorC;
import com.example.contacto_3xtrat3r3str3.piglatin.builder.ASTBuilderPig;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoAST;
import com.example.contacto_3xtrat3r3str3.piglatin.nodo.NodoPrograma;
import com.example.contacto_3xtrat3r3str3.piglatin.semantica.ValidadorSemanticoPig;
import com.example.contacto_3xtrat3r3str3.y.errores.ErrorPosicional;
import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;
import com.example.piglatin.analizador.gramatica.PigLexer;
import com.example.piglatin.analizador.gramatica.PigParser;
import org.antlr.v4.runtime.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ServicioCompilacionPig {


    public ResultadoCompilacionPig analizar(String codigoFuente, Path carpetaRaiz) {

        if (codigoFuente == null || codigoFuente.trim().isEmpty()) {
            return new ResultadoCompilacionPig(
                    false, null,
                    List.of(), List.of(), List.of(),
                    List.of("El código está vacío"),
                    null, false, null
            );
        }

        try {
            return analizarInterno(codigoFuente, carpetaRaiz);
        } catch (StackOverflowError soe) {
            return new ResultadoCompilacionPig(
                    false, null,
                    List.of(), List.of(), List.of(),
                    List.of("Estructura demasiado profunda o inválida."),
                    null, false, null
            );
        } catch (Exception e) {
            return new ResultadoCompilacionPig(
                    false, null,
                    List.of(), List.of(), List.of(),
                    List.of("Error interno inesperado: " + e.getMessage()),
                    null, false, null
            );
        }
    }

    private ResultadoCompilacionPig analizarInterno(String codigoFuente, Path carpetaRaiz) {

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
                    erroresLexicos, List.of(), List.of(), List.of(),
                    null, false, null
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
                    List.of(), erroresSintacticos, List.of(), List.of(),
                    null, false, null
            );
        }

        // 3. AST
        NodoPrograma programa;
        try {
            ASTBuilderPig builder = new ASTBuilderPig();
            NodoAST nodo = builder.visit(tree);
            programa = (nodo instanceof NodoPrograma np) ? np : null;
        } catch (Exception e) {
            return new ResultadoCompilacionPig(
                    false, null,
                    List.of(), List.of(), List.of(),
                    List.of("Error al construir el AST: " + e.getMessage()),
                    null, false, null
            );
        }

        if (programa == null) {
            return new ResultadoCompilacionPig(
                    false, null,
                    List.of(), List.of(), List.of(),
                    List.of("El AST resultante es nulo"),
                    null, false, null
            );
        }

        // 4. VALIDACIÓN SEMÁNTICA
        List<ErrorSemantico> erroresSemanticos = new ArrayList<>();
        ValidadorSemanticoPig validador = null;
        try {
            validador = new ValidadorSemanticoPig(carpetaRaiz);
            erroresSemanticos = validador.analizar(programa);
        } catch (Exception e) {
            return new ResultadoCompilacionPig(
                    false, programa,
                    List.of(), List.of(), List.of(),
                    List.of("Error en validación semántica: " + e.getMessage()),
                    null, false, null
            );
        }

        boolean exitoso = erroresSemanticos.isEmpty();

        // 5. GENERACIÓN DE C (solo si no hay errores semánticos)
        String codigoC = null;
        boolean compilacionOk = false;
        String rutaExe = null;

        if (exitoso) {
            try {
                codigoC = generarCodigoC(programa, validador);

                GeneradorArchivoC generadorArchivo = new GeneradorArchivoC();
                compilacionOk = generadorArchivo.generarYCompilar(codigoC);

                if (compilacionOk) {
                    rutaExe = Paths.get(System.getProperty("user.dir"), "programa")
                            .toAbsolutePath().toString();
                }
            } catch (Exception e) {
                erroresSemanticos.add(new ErrorSemantico(
                        -1, -1, "Generación C",
                        "Error al generar/compilar el código C: " + e.getMessage()));
                exitoso = false;
            }
        }

        return new ResultadoCompilacionPig(
                exitoso,
                programa,
                List.of(),
                List.of(),
                erroresSemanticos,
                List.of(),
                codigoC,
                compilacionOk,
                rutaExe
        );
    }

    // Genera el código C combinando imports (.y, .z) con el .pig
    private String generarCodigoC(NodoPrograma programa, ValidadorSemanticoPig validador) {
        // Recoger imports (FuncionC, EstructuraC) del semántico
        var importaciones = validador.getImportaciones();
        var funcionesImportadas = importaciones.getFuncionesImportadas();
        var estructurasImportadas = importaciones.getEstructurasImportadas();

        // Tabla del .pig
        var tabla = validador.getTabla();

        // Variables globales y main del .pig
        var variablesGlobales = programa.aVariablesGlobalesC(tabla);
        var mainC = programa.aMainC(tabla);

        // Combinar
        var funciones = new ArrayList<>(funcionesImportadas);
        funciones.add(mainC);
        var estructuras = new ArrayList<EstructuraC>();
        var nombresVistos = new java.util.HashSet<String>();
        for (var e : estructurasImportadas) {
            if (nombresVistos.add(e.getNombre())) {
                estructuras.add(e);
            }
        }

        // Generar
        return new GeneradorC().generar(funciones, estructuras, variablesGlobales, false);
    }
}