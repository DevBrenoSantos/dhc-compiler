package functions;

import java.util.ArrayList;
import java.util.List;

public class No {
    String name; 
    Token token;
    List<No> children = new ArrayList<>();
    No parent = null;
    
    // Construtor para nós NÃO-TERMINAIS (sem token válido)
    No(String name) {
        this.name = name;
        this.children = new ArrayList<>();
        this.token = new Token(name, 0);  // Token dummy com classe = 0
    }

    // Construtor para nós TERMINAIS (com token da análise léxica)
    No(String name, Token token) {
        this.name = name;
        this.children = new ArrayList<>();
        this.token = token;  // Usa o token original da análise léxica
    }
    
    No get(String name) {
        List<No> rev = children.reversed();
        for (No c : rev) {
            if (c.name.equals(name)) return c;
        }
        return null;
    }
    void addChild(No child) {
        child.setParent(this);
        children.add(child);
    }
    void removeChild(No child) {
        children.remove(child);
    }

    No getParent() {
        return this.parent;
    }
    
    void setParent(No parent) {
        this.parent = parent;
    }

    No get(int pos) {
        return children.get(pos);
    }
    
    Token getToken() {
        return this.token;
    }

    int getSize() {
        return children.size();
    }

    void print(String prefix) {
        IO.println(prefix + (token != null ? token.getLexema() : name));
        for (No c : children) c.print("  "+prefix);
    }

    String write(String prefix) {
        StringBuilder sb = new StringBuilder();
            // Se for EXPL ou EXPA → reconstruir expressão
        if (name.equals("EXPL") || name.equals("EXPA")) {
            
            sb.append(prefix)
            .append((token != null ? token.getLexema() : name) + " ")
            .append(AnaliseSintatica.reconstruir(this))
            .append("\n");
            return sb.toString();
        }

        sb.append(prefix)
        .append(token != null ? token.getLexema() : name)
        .append("\n");
        for (No child : children) {
            sb.append(child.write(prefix + "  "));
        }
        return sb.toString();
    }
}