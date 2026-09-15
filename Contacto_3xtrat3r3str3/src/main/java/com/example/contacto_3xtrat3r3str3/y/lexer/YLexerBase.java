package com.example.contacto_3xtrat3r3str3.y.lexer;


import com.example.y.analizador.gramatica.YLexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Clase base del lexer de Y?. Sobreescribe nextToken() para convertir
 * los tabs que arrastra cada NEWLINE (ver la regla NEWLINE en YLexer.g4)
 * en tokens sintéticos INDENT / DEDENT.
 *
 * Regla confirmada con el encargado: la indentación en Y? es
 * EXCLUSIVAMENTE con tabs. Un tab = un nivel. No se cuentan espacios.
 */
public abstract class YLexerBase extends Lexer {

    private final Deque<Integer> pilaIndentacion = new ArrayDeque<>();
    private final Deque<Token> pendientes = new ArrayDeque<>();
    private boolean finDeArchivoProcesado = false;

    protected YLexerBase(CharStream input) {
        super(input);
        pilaIndentacion.push(0);
    }

    @Override
    public Token nextToken() {
        if (!pendientes.isEmpty()) {
            return pendientes.poll();
        }

        Token token = super.nextToken();

        if (token.getType() == Token.EOF) {
            return manejarFinDeArchivo(token);
        }

        if (token.getType() == YLexer.NEWLINE) {
            return manejarSaltoDeLinea(token);
        }

        return token;
    }

    private Token manejarFinDeArchivo(Token eof) {
        if (finDeArchivoProcesado) {
            return eof;
        }
        finDeArchivoProcesado = true;

        while (pilaIndentacion.peek() > 0) {
            pilaIndentacion.pop();
            pendientes.offer(crearToken(YLexer.DEDENT, eof));
        }
        pendientes.offer(eof);
        return pendientes.poll();
    }

    private Token manejarSaltoDeLinea(Token newline) {
        int tabsDeLaSiguienteLinea = contarTabsFinales(newline.getText());
        int nivelActual = pilaIndentacion.peek();

        pendientes.offer(newline);

        if (tabsDeLaSiguienteLinea > nivelActual) {
            pilaIndentacion.push(tabsDeLaSiguienteLinea);
            pendientes.offer(crearToken(YLexer.INDENT, newline));
        } else if (tabsDeLaSiguienteLinea < nivelActual) {
            while (pilaIndentacion.peek() > tabsDeLaSiguienteLinea) {
                pilaIndentacion.pop();
                pendientes.offer(crearToken(YLexer.DEDENT, newline));
            }
            if (pilaIndentacion.peek() != tabsDeLaSiguienteLinea) {
                throw new IndentacionInvalidaException(
                        "Nivel de indentación no coincide con ningún bloque abierto, línea "
                                + (newline.getLine() + 1));
            }
        }
        // Si son iguales, no se agrega nada más: solo el NEWLINE ya encolado.

        return pendientes.poll();
    }

    private int contarTabsFinales(String textoNewline) {
        int cantidad = 0;
        for (int i = textoNewline.length() - 1; i >= 0; i--) {
            if (textoNewline.charAt(i) == '\t') {
                cantidad++;
            } else {
                break;
            }
        }
        return cantidad;
    }

    private Token crearToken(int tipo, Token base) {
        CommonToken token = new CommonToken(base);
        token.setType(tipo);
        token.setText(tipo == YLexer.INDENT ? "<INDENT>" : "<DEDENT>");
        return token;
    }
}
