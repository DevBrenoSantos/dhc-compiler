package functions;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import functions.AnaliseLexica.AnaliseResult;

public class AnaliseSemantica {
    
    private No arvore;
    private List<Symbol> symbolTable;
    AnaliseSemantica(No arvore) {
        this.arvore = arvore;
        this.symbolTable = new ArrayList<>();
    }

    public void analisar(No arvore) {
        for (No child : arvore.children) {

            
        switch (child.name) {
            case "DECL": checkDeclaration(child); break; // TYPE
            case "ATTR": checkAttribution(child); break;
            //case "EXPA": checkMathExpression(child); break;
            //case "EXPL": checkLogicExpression(child); break;
        }
            analisar(child);
        }
    }
    // TODO: implementar verificações tipo -> id, procurar um jeito eficiente de fazer isso
    public void checkDeclaration(No child) {

        int pos = 0;
        String type;
        String varName;
        if (child.get("final") != null) {
            varName = child.get(1).name;
            type = "final";
        }
        else if (child.get("TYPE") != null) {
            type = child.get("TYPE").get(0).name;
            varName = child.get(1).name;
        }
        else {
            IO.println("Erro: Declaracao sem tipo");
            return;
        }

        

        switch (type) {
            case "int":
                symbolTable.add(new Symbol(varName, "int", true, child.get(1).getToken()));
                break;
            case "byte":
                symbolTable.add(new Symbol(varName, "byte", true, child.get(1).getToken()));
                break;
            case "boolean":
                symbolTable.add(new Symbol(varName, "boolean", true, child.get(1).getToken()));
                break;
            case "string":
                symbolTable.add(new Symbol(varName, "string", true, child.get(1).getToken()));
                break;
            case "final":
                symbolTable.add(new Symbol(varName, "final", true, child.get(1).getToken()));
                break;
            default:
                IO.println("Erro: Tipo nao reconhecido: " + type);
        }
    }

    public void checkAttribution(No child) { // TODO: criar metodos de EXPR  e terminar checkAttribution

        No id = child.get("id").get(0); // variavel antes do '='
        for (Symbol sym : symbolTable) {
            if (sym.getName().equals(id.name)) { // id encontrado

                String type = sym.getType();
                if (child.get("EXPA") != null) {
                    checkMathExpression(child.get("EXPA"));
                    return;
                }
                else if (child.get("EXPL") != null) {
                    // verificar se é logic expression
                    return;
                }
                else if (child.get(0).token.getId() == 4) {
                    String varType = getVarType(child.get(0));
                    if (!varType.equals(type)) {
                        IO.println("Erro: Atribuicao de variavel do tipo " + varType + " para variavel do tipo " + type);
                        return;
                    }
                    return;
                }
                else if (child.get(0).token.getId() == 6) { // literal string
                    if (!type.equals("string")) {
                        IO.println("Erro: Atribuicao de literal string para variavel do tipo " + type);
                        return;
                    }
                    return;

                }
                else if (child.get(0).token.getId() == 5) { // literal int
                    if (!type.equals("int") && !type.equals("byte")) {
                        IO.println("Erro: Atribuicao de literal int para variavel do tipo " + type);
                        return;
                    }
                    return;
                }

                else {
                    IO.println("Erro: Expressao invalida na atribuicao para " + id.name);
                    return;
                }
            }
        }

        IO.println("Erro: id não declarado: " + id);
    }

    public String getVarType(No child) {
        for (Symbol sym : symbolTable) {
            if (sym.getName().equals(child.name)) {
                return sym.getType();
            }
        }
        return null;
    }
    public void checkMathExpression(No expa) { // EXPA
        int length = expa.getSize();
        String tipoEsperado = null;
        for (int i=0; i<length; i+=2) {

            if (expa.get(i).getToken().getClasse() == 4) { // identificador
                tipoEsperado = getVarType(expa.get(i));
                if (tipoEsperado == null) {
                    IO.println("Erro: Variavel nao declarada na expressao matematica: " + expa.get(i).name);
                    continue;
                }
                if (!Set.of("int", "string").contains(tipoEsperado)) {
                    IO.println("Erro: Tipo não suportado: " + tipoEsperado + " na expressao matematica");
                }
            }

            else if (expa.get(i).getToken().getClasse() == 5) { // constante numerica
                if (tipoEsperado != "int") {
                    IO.println("Erro: Tipo esperado " + tipoEsperado + " mas constante numerica encontrada na expressao matematica");
                    return;
                }
                tipoEsperado = "int";
            }
            else if (expa.get(i).getToken().getClasse() == 6) { // constante string
                if (tipoEsperado != "string") {
                    IO.println("Erro: Tipo esperado " + tipoEsperado + " mas constante string encontrada na expressao matematica");
                    return;
                }

                if (!expa.get(i+1).name.equals("+") || (i+1 < length)) {
                    IO.println("Erro: Operador invalido " + expa.get(i-1).getToken().getLexema() + " para concatenacao de strings");
                    return;
                }
                

                tipoEsperado = "string";

            }
            else {
                IO.println("Erro: Valor com tipo invalido detectado na expressão");
                return;
            }
        }
    }

    public void checkLogicExpression(No expl) { // EXPL
        int length = expl.getSize();
        String tipoEsperado = null;
        Set<String> opLogicos = Set.of("==", "<", ">", "<>", ">=", "<=");
        Set<String> opAndOr = Set.of("and", "or");
        // a < b and b < c ...
        String opExpected = "logic";
        for (int i=0; i<length; i+=2) {

            

            if (expl.get(i).getToken().getClasse() == 4) { // identificador
                tipoEsperado = getVarType(expl.get(i));
                if (tipoEsperado == null) {
                    IO.println("Erro: Variavel nao declarada na expressao logica: " + expl.get(i).name);
                    continue;
                }
                if (!Set.of("int", "string").contains(tipoEsperado)) {
                    IO.println("Erro: Tipo não suportado: " + tipoEsperado + " na expressao logica");
                }
            }

            else if (expl.get(i).getToken().getClasse() == 5) { // constante numerica
                if (tipoEsperado != "int") {
                    IO.println("Erro: Tipo esperado " + tipoEsperado + " mas constante numerica encontrada na expressao matematica");
                    return;
                }
                tipoEsperado = "int";
            }
            else if (expl.get(i).getToken().getClasse() == 6) { // constante string
                if (tipoEsperado != "string") {
                    IO.println("Erro: Tipo esperado " + tipoEsperado + " mas constante string encontrada na expressao matematica");
                    return;
                }
                if (!expl.get(i+1).getToken().getLexema().equals("==") || (i+1 < length)) {
                    IO.println("Erro: Operador invalido " + expl.get(i-1).getToken().getLexema() + " para manipulacao de strings");
                    return;
                }
                tipoEsperado = "string";

            }
            else {
                IO.println("Erro: Valor com tipo invalido detectado na expressão");
                return;
            }
        }
    }

    public String getClasseStr(No child) {
        switch (child.getToken().getClasse()) {
            case 0: return "Boolean";
            case 1: return "Palavra reservada";
            case 2: return "Tipo";
            case 3: return "Operador";
            case 4: return "Identificador";
            case 5: return "Numero";
            case 6: return "String";
            default: return "Desconhecido";
        }

    }

    public List<Symbol> getSymbolTable() {
        return this.symbolTable;
    }

    public static void main(String[] args) {
                AnaliseLexica analise = new AnaliseLexica();
        InputStream in;
        String entrada = null;
        try {
            in = new BufferedInputStream(new FileInputStream("docs/codigo_fonte_LC.txt"));
            entrada = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            in.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        AnaliseResult res = analise.analisar(entrada);
        AnaliseSintatica sintatica = new AnaliseSintatica(res.tokens);

        sintatica.writeTree();

        AnaliseSemantica semantica = new AnaliseSemantica(sintatica.getRaiz());
        semantica.analisar(semantica.arvore);

        IO.println();

/*         for (Symbol sym : semantica.getSymbolTable()) {
            IO.println("Declaracao: " + sym.getName() + " Tipo: " + sym.getType());
            IO.println("Token id: " + sym.getToken().getId() + " Lexema: " + sym.getToken().getLexema());
            IO.println("-----");
        } */
    }

}


class Symbol {
    private String name;
    private String type;
    private boolean initialized;
    private String scope;
    private Token token;

    public Symbol(String name, String type) {
        this.name = name;
        this.type = type;
        this.initialized = false;
        this.scope = "global";
    }

    public Symbol(String name, String type, boolean initialized, Token token) {
        this.name = name;
        this.type = type;
        this.initialized = initialized;
        this.scope = "global";
        this.token = token;
    }

    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }
    public Token getToken() {
        return token;
    }
}