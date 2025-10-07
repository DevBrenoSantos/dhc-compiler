package functions;

import java.util.ArrayList;
import java.util.List;
import functions.AnaliseLexica.AnaliseResult;

public class AnaliseSintatica {

    private List<Token> tokens;
    private int i = 0;
    private List<Token> stack = new ArrayList<>(); // pilha vai ser usada para blocos
    private String msgPadrao = "Erro de sintaxe";
    public AnaliseSintatica(List<Token> tokens) {
        this.tokens = tokens;
    }

    private int START() throws Exception {
        String lexToken = tokens.get(i).getLexema();
        if (TYPE() == 0 || lexToken.equals("final")) {
            i++;
            DECL(); 
            return 0;
        }
        //else if (tokens.get(i).getLexema().equals("begin")) BLOCO();
        else if (lexToken.equals(";")) {
            i++;
            NULO(); 
            return 0;
        }
        throw new Exception(msgPadrao);

    }

    private int TYPE() throws Exception {
        if (tokens.get(i).getClasse() == 2) {
            return 0;
        };
        return 1;
    }

    private int DECL() throws Exception {
        int estadoAtual = 1;

        while (true)
        switch (estadoAtual) {
            case 1: // MAXITER
                if (tokens.get(i++).getClasse() == 0) { // i++: =
                    if (tokens.get(i).getLexema().equals(",")) break; // outro id
                    else if (tokens.get(i).getLexema().equals("=")) estadoAtual = 3;
                    else if (tokens.get(i).getLexema().equals(";")) estadoAtual = 5;
                    else estadoAtual = 6;
                } break;
            case 3: // =
                i++; // i++: 10
                if (tokens.get(i++).getClasse() == 0) estadoAtual = 4; // i++: ;
                else estadoAtual = 6;
                break;
            case 4: // ;
                if (tokens.get(i++).getLexema().equals(";")) estadoAtual = 5; // i++: begin
                else estadoAtual = 6;
                break;
            case 5: {
                START();
                return 0;
            }
            default: throw new Exception(msgPadrao);
        }
    }

    private void NULO() throws Exception {
        START();
    }

    private int OPMATH() throws Exception {
        if (tokens.get(i++).getLexema().matches("(+|-|*|/)")) return 0;
        throw new Exception(msgPadrao);
    }
    private int AND_OR() throws Exception {
        if (tokens.get(i++).getLexema().matches("^(and|or)$")) return 0; // apenas and, apenas or
        throw new Exception(msgPadrao);
    }

    private void validate() {
        try {
            START();
            System.out.println("nenhum erro");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Programa interrompido no token: " + tokens.get(i).getLexema());
            System.out.println("Indice " + i);
        }
        
        if (!stack.isEmpty())  {
            // falha, há algum bloco não fechado
        }
    }

    public static void main(String[] args) {
        String[] entrada = new String[] {
           "int n;",
            "string nome;",
            "boolean naoTerminou;",
            "final MAXITER = 10;",
            "{ Bloco Principal }",
            "begin",
            "    write, \"Digite seu nome: \";",
            "    readln, nome;",
            "    naoTerminou = true;",
            "    n = 0;",
            "    while naoTerminou begin",
            "        writeln, \"Ola' \", nome;",
            "        n = n + 1;",
            "        naoTerminou = n < MAXITER;",
            "    end",
            "end"

        };
        AnaliseLexica lexica = new AnaliseLexica();
        AnaliseResult res = lexica.analisar(entrada);
        AnaliseSintatica sintatica = new AnaliseSintatica(res.tokens);
        for (Token token : sintatica.tokens) {
            System.out.print(token.getLexema() + " ");
        }
        for (Token token : sintatica.tokens) {
            System.out.println("  [" + token.getLexema() + "] -> " + token.getClasse());
        }
        sintatica.validate();
    }
}
