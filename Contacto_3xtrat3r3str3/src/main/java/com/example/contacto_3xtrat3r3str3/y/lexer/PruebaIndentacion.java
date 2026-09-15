package com.example.contacto_3xtrat3r3str3.y.lexer;


import com.example.y.analizador.gramatica.YLexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import java.nio.file.Paths;

/**
 * Programa de un solo uso para esta etapa de validación: NO usa el
 * parser todavía, solo corre el lexer y muestra el stream de tokens
 * completo, con INDENT/DEDENT incluidos, para confirmar visualmente
 * que el mecanismo funciona antes de construir el resto de Y?.
 *
 * Uso: java com.example.y.lexer.PruebaIndentacion ejemplos/ejemplo_indentacion.y
 */
public class PruebaIndentacion {
    public static void main(String[] args) throws Exception {
        String ruta = args.length > 0 ? args[0] : "/home/herson/Descargas/ejemplo_indentacion.y";
        CharStream input = CharStreams.fromPath(Paths.get(ruta));
        YLexer lexer = new YLexer(input);

        Token token;
        while ((token = lexer.nextToken()).getType() != Token.EOF) {
            String nombre = YLexer.VOCABULARY.getSymbolicName(token.getType());
            String texto = token.getText()
                    .replace("\n", "\\n")
                    .replace("\t", "\\t")
                    .replace("\r", "\\r");
            System.out.printf("L%-3d %-18s '%s'%n", token.getLine(), nombre, texto);
        }
        System.out.println("EOF");
    }
}