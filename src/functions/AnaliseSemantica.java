package functions;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import functions.AnaliseLexica.AnaliseResult;

public class AnaliseSemantica {
    
    private No arvore;

    private HashMap<String, Symbol> symbolTable;
    AnaliseSemantica(No arvore) {
        this.arvore = arvore;
        this.symbolTable = new HashMap<>(Map.of()); // hashmap mutavel
    }

    public void analisar(No arvore) {
        for (No child : arvore.children) {
            //System.out.println(child.name);
            
            checkType(child, symbolTable);
            analisar(child);
        }
    }
    public Symbol checkDeclaration(No child) {

        String type;
        String varName;
        Symbol sym;
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
            return null;
        }
        switch (type) {
            case "int":
                sym = new Symbol(child.get(1).getToken());
                symbolTable.put(varName, sym);
                break;
            case "byte":
                sym = new Symbol(child.get(1).getToken());
                symbolTable.put(varName, sym);
                break;
            case "boolean":
                sym = new Symbol(child.get(1).getToken());
                symbolTable.put(varName, sym);
                break;
            case "string":
                sym = new Symbol(child.get(1).getToken());
                symbolTable.put(varName, sym);
                break;
            case "final":
                if (child.getSize() > 1) {
                    sym = new Symbol(child.get(3).getToken());
                    type = child.get(3).token.getClasse();

                    if (type.equals("Literal Byte")) type = "int";

                    symbolTable.put(varName, new Symbol(child.get(3).getToken())); 
                    break;         
                }
            default:
                IO.println("Erro: Tipo nao reconhecido: " + type);
                return null;
        }
        if (child.getSize() > 3) {
            System.out.println(child.get(3).getToken().getTipo());
            System.out.println(child.get(3).getToken().getClasse());
            sym.setValue(child.get(3).name);
        }
        return symbolTable.get(varName);
    }

private void checkAttribution(No atrib, HashMap<String, Symbol> tabela) {

    // 1) id da atribuição (antes do '=')
    No id = atrib.get("id");
    if (id == null) return;

    // valida id de destino
    checkType(id, tabela);

    // 2) expressão do lado direito da atribuição
    No expr = null;

    if (atrib.get("EXPA") != null) {
        expr = atrib.get("EXPA");
    }
    else if (atrib.get("EXPL") != null) {
        expr = atrib.get("EXPL");
    }
    else if (atrib.children.size() > 1) {
        // casos como:
        //   result = 5;
        //   result = "oi";
        //   result = true;
        // pega o nó logo após o "id"
        expr = atrib.children.get(2);
    }

    if (expr == null) return;

    // 3) valida TODA a expressão (todos os ids internos)
    checkAllExpr(expr, tabela);

    // 4) valida tipos (usa o seu checkType principal)
    Symbol tExpr = checkType(expr, tabela);
    Symbol tId   = tabela.get(id.name);

    if (tExpr == null || tId == null) return;

    if (!tExpr.getType().equals(tId.getType())) {
        System.err.println(
            "Erro de tipo: atribuição de " + tExpr.getType() +
            " para variável " + id.name + " (" + tId.getType() + ")"
        );
    }
}

    public String getVarType(No child) {
        return symbolTable.get(child.name).getType();

    }
    private Symbol checkTypeExpMath(No no, HashMap<String, Symbol> tabelaSimbolos) {
        if (no.children.isEmpty()) return null;
        checkAllExpr(no, tabelaSimbolos);
        Symbol left = checkType(no.children.get(0), tabelaSimbolos);
        Symbol right = no.children.size() > 2 ? checkType(no.children.get(2), tabelaSimbolos) : left;

        if (left == null || right == null) return null;
        if (left.getType().equals(right.getType()) && right.getType().equals(left.getType())) {
            return new Symbol(new Token("int", 2));
        }
        System.err.println("Operação aritmética inválida: " + left.getType() + " e " + right.getType());
        return null;
    }

    private Symbol checkType(No no, HashMap<String, Symbol> tabelaSimbolos) {
        if (no == null) return null;

        switch (no.name) {

            case "DECL": return checkDeclaration(no); // TYPE
            case "ATTR": 
                checkAttribution(no, symbolTable);
                return null;
            // Identificador
            case "id":
                No id = no;
                if (no.name.equals("id")) {
                    id = no.get(0);
                }
                if (!tabelaSimbolos.containsKey(id.name)) {
                    System.err.println("Variável não declarada: " + id.name);
                }
                return tabelaSimbolos.get(id.name);


            case "true":
            case "false":
                return new Symbol(no.token);

            // Expressões aritméticas
            case "EXPA":
            case "OPMATH":
                return checkTypeExpMath(no, tabelaSimbolos);

            // Expressões lógicas
            case "EXPL":
            case "OPLOG":
            case "EXPL_REL":
            case "AND":
            case "and":
            case "OR":
            case "or":
            case "NOT":
                return checkTypeExpLogic(no, tabelaSimbolos);

            // Parênteses e agrupamentos
            case "(":
            case ")":
                if (!no.children.isEmpty()) {
                    return checkType(no.children.get(0), tabelaSimbolos);
                }
                return null;

            default:
                // Se não for um nó terminal ou conhecido, percorre os filhos
                if (!no.children.isEmpty()) {
                    Symbol tipo = null;
                    for (No child : no.children) {
                        tipo = checkType(child, tabelaSimbolos);
                        if (tipo != null) break; // pega o tipo do primeiro filho válido
                    }
                    return tipo;
                }
                // Não é terminal nem possui filhos → ignora
                return null;
        }
    }
    private void checkAllExpr(No no, HashMap<String, Symbol> tabela) {
        if (no == null) return;

        if (no.name.equals("id")) {
            String lex = no.get(0).token.getLexema();
            if (!tabela.containsKey(lex)) {
                System.err.println("Variável não declarada: " + lex);
            }

            // outros terminais (literals) ignoram
            return;
        }

        for (No child : no.children) {
            checkAllExpr(child, tabela);
        }
    }
    private Symbol checkTypeExpLogic(No no, HashMap<String, Symbol> tabelaSimbolos) {

        if (no.children.isEmpty()) return null;
        checkAllExpr(no, tabelaSimbolos);
        if (no.name.equals("NOT")) {
            Symbol t = checkType(no.children.get(0), tabelaSimbolos);
            if (!t.getType().equals("boolean")) {
                System.err.println("'not' aplicado a tipo não-booleano: " + t.getType());
                return null;
            }
            return new Symbol(new Token("boolean", 0));
        } else if (no.name.equals("AND") || no.name.equals("OR")) {
            if (no.children.size() >= 2) {
                Symbol t1 = checkType(no.children.get(0), tabelaSimbolos);
                Symbol t2 = checkType(no.children.get(1), tabelaSimbolos);
                if (t1 == null || t2 == null) return null;
                if (!"bool".equals(t1.getType()) || !"bool".equals(t2.getType())) {
                    System.err.println("Operador lógico '" + no.name + "' aplicado a tipos não-booleanos: " 
                        + (t1!=null?t1.getType():"null") + " e " + (t2!=null?t2.getType():"null"));
                    return null;
                }
                return new Symbol(new Token("boolean", 0));
            }
            return null;

        } else if (no.name.equals("EXPL_REL")) {
            Symbol left = checkType(no.children.get(0), tabelaSimbolos);
            Symbol right = checkType(no.children.get(2), tabelaSimbolos);
            if (!left.getType().equals(right.getType())) {
                System.err.println("Comparação inválida entre " + left.getType() + " e " + right.getType());
                return null;
            }
            return new Symbol(new Token("boolean", 0));

        } else {
            return checkType(no.children.get(0), tabelaSimbolos);
        }
    }

    public static void main(String[] args, String fileName) {
        AnaliseLexica analise = new AnaliseLexica();
        InputStream in;
        String entrada = null;
        try {
            in = new BufferedInputStream(new FileInputStream("src/codes/" + fileName));
            entrada = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        AnaliseResult res = analise.analisar(entrada);
        AnaliseSintatica sintatica = new AnaliseSintatica(res.tokens);
        sintatica.writeTree(fileName);

        AnaliseSemantica semantica = new AnaliseSemantica(sintatica.getRaiz());
        semantica.analisar(semantica.arvore);

    }

}


class Symbol {
    private String id;
    private String type;
    private String value;
    private Token token;

    public Symbol(String value, Token token) {
        this.id = token.getLexema();
        this.type = token.getTipo();
        this.value = value;
        this.token = token;
    }
    public Symbol(Token token) {
        this.id = token.getLexema();
        this.type = token.getTipo();
        this.token = token;
    }

    public void setValue(String value) { this.value = value; }
    public String getId() { return id; }
    public String getType() { return type; }
    public Token getToken() { return token; }
    public String getValue() { return value; }
}