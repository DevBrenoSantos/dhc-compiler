package functions;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import functions.SymbolTable;

public class AnaliseLexica {

    private static int estadoAtual = 0;
    private static SymbolTable table = new SymbolTable();
    private static String lex = "";

    public static void read(String[] file) {
        if (file.length != 1) {
            System.err.println("Uso: java App <caminho-do-arquivo>");
            System.exit(2);
        }

        String path = file[0];

        try (InputStream in = new BufferedInputStream(new FileInputStream(path))) {
            int b = in.read();
            int lookahead = in.read();

            init((char) b); // primeiro byte analisado

            while (b != -1) {
                char c = (char) b;
                char nextC = (char) lookahead;
                //abcd{
                switch(estadoAtual) {
                    case 0: {
                        
                    }
                    case 1: { // escopo, bloco
                    }
                    case 2: { // identificador
                        checkId(c, nextC);
                    }
                    case 3: { // String
                        
                    }
                    // ...
                    case 14:  { // fim do lexema
                        endLex(lex);
                    }
                }
                b = lookahead;
                lookahead = in.read();
            }
            System.out.flush();
        } catch (IOException e) {
            System.out.println("Erro de I/O: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void checkId(char c, char nextChar) {
        if ((table.isAlpha(""+nextChar) || table.isDigit(""+nextChar))
        || String.valueOf(nextChar).equals("_")) {
            lex += c;
        }
        else estadoAtual = 14;
    }


    private static void init(char c) {
        String ch = ""+c;
        if      (ch.matches("[ \n]")) estadoAtual = 0;
        else if (ch.equals("{")) estadoAtual = 1;
        else if (ch.matches("[a-zA-Z_]")) estadoAtual = 2;
        else if (ch.matches("[0-9]")) estadoAtual = 3;
        else if (ch.equals("\"")) estadoAtual = 4;

    }

    private static void endLex(String lex) {
        boolean exists = table.hasLex(lex);
        if (!exists && !lex.equals("")) {
            table.addLex(lex);
        }
        lex = "";
        estadoAtual = 0;
    }
}

