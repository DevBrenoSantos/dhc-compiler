package functions;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
            detectarEstado((char) b); // primeiro byte analisado, e para qual estado ele irá

            while (b != -1) {
                char c = (char) b;
                switch(estadoAtual) {
                    case 0: {
                        detectarEstado(c);
                    }
                    case 1: { // espaço, \n
                    }
                    case 2: { // identificador
                        identifier(c);
                        if (estadoAtual == 14) continue; // reanalisar o byte atual
                    }
                    case 3: { // String
                        
                    }
                    // ...
                    case 14:  { // fim do lexema
                        endLex(lex);
                    }
                }
                b = in.read();
            }
            System.out.flush();
        } catch (IOException e) {
            System.out.println("Erro de I/O: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void identifier(char c) {
        String ch = String.valueOf(c);
        
        if ((table.isLetter(ch) || table.isDigit(ch)) || c == '_') {
            lex += c;
        }
        else estadoAtual = 14;
    }


    private static void detectarEstado(char c) {
        String ch = String.valueOf(c);
        if      (ch.matches("[ \n]")) estadoAtual = 0;
        else if (ch.matches("{|/\\*")) estadoAtual = 1;
        else if (ch.matches("[a-zA-Z_]")) estadoAtual = 2;
        else if (ch.matches("[0-9]")) estadoAtual = 3;
        else if (ch.equals("\"")) estadoAtual = 4;
    }

    private static void endLex(String lex) {
        boolean exists = table.hasSymbol(lex);
        if (!exists && !lex.equals("")) {
            table.createSymbol(lex);
        }
        lex = "";
        estadoAtual = 0;
    }
}

