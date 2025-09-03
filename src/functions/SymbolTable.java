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

    public boolean hasSymbol(String lex) {
        if (lexToId.containsKey(lex)) return true;
        return false;
    }

    public void createSymbol(String lexema) {
        idToSymbol.put(idToSymbol.size(), new Symbol(lexema));
        lexToId.put(lexema, idToSymbol.size());
    }
    
    public void createSymbol(String lexema, int classe, int tipo, int endereco) {
        idToSymbol.put(idToSymbol.size(), new Symbol(lexema, classe, tipo, endereco));
        lexToId.put(lexema, idToSymbol.size());
    }

    public boolean isLetter(String ch) {
        return ch.matches("[a-zA-Z]");
    }
    public boolean isDigit(String ch) {
        return ch.matches("[0-9]");
    }
    public boolean isString(String ch) {
        return ch.matches("\"[a-zA-Z_0-9]*[^\"\n]*\"|\"\"$");
    }
}

class Symbol {

    private int id;
    private String lexema;
    private int classe = 0;
    private int tipo = 0;
    private int endereco = 0;

    public Symbol(String lexema, int classe, int tipo, int endereco) {
        this.lexema = lexema;
        this.classe = classe;
        this.tipo = tipo;
        this.endereco = endereco;
    }

    public Symbol(String lexema) {
        this.lexema = lexema;
    }

}