package functions;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    Map<Integer, Symbol> idToSymbol = new HashMap<>(Map.of(
        0, new Symbol("id"),
        1, new Symbol("byte"),
        2, new Symbol("string"),
        3, new Symbol("const")
    ));

    Map<String, Integer> lexToId = new HashMap<>(Map.of(
        "id", 0,
        "byte", 1,
        "string", 2,
        "const", 3
    ));

    public boolean hasLex(String lex) {
        if (lexToId.containsKey(lex)) return true;
        return false;
    }

    public void addLex(String lexema) {
        idToSymbol.put(idToSymbol.size(), new Symbol(lexema));
    }
    
    public void addLex(String lexema, int classe, int tipo, int endereco) {
        idToSymbol.put(idToSymbol.size(), new Symbol(lexema, classe | 0, tipo, endereco));
    }

    public boolean isAlpha(String text) {
        return text.matches("[a-zA-Z]");
    }

    public boolean isDigit(String text) {
        return text.matches("[0-9]");
    }
    public boolean isString(String text) {
        return text.matches("\"[a-zA-Z_0-9]*[^\"\n]*\"$");
    }
}

class Regex {
    String stringRegex = "\"[a-zA-Z_0-9]*[^\"\n]*\"$";
    String digitRegex = "[0-9]";
}


class Symbol {

    private int id;
    private String lexema;
    private int classe;
    private int tipo;
    private int endereco;

    public Symbol(String lexema, int classe, int tipo, int endereco) {
        this.lexema = lexema;
        this.classe = classe;
        this.tipo = tipo;
        this.endereco = endereco;
    }

    public Symbol(String lexema) {
        this.lexema = lexema;
    }
    public Symbol(int id, String lexema, int classe, int tipo, int endereco) {
        this.lexema = lexema;
        this.classe = classe;
        this.tipo = tipo;
        this.endereco = endereco;
    }
}