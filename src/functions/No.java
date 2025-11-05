package functions;

import java.util.ArrayList;
import java.util.List;

public class No {
    String name; 
    Token token;
    List<No> children = new ArrayList<>();
    No parent = null;
    
    No(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    No(String name, No parent) {
        this.name = name;
        this.parent = parent;
        this.parent.addChild(new No(name));
        this.children = new ArrayList<>();
    }

    No getChildByName(String name) {
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

    
    No getParent() {
        return this.parent;
    }
    void setParent(No parent) {
        this.parent = parent;
    }


    void print(String prefix) {
        IO.println(prefix + (token != null ? token.getLexema() : name));
        for (No c : children) c.print("  "+prefix);
    }

    String write(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix)
        .append(token != null ? token.getLexema() : name)
        .append("\n");
        for (No child : children)
            sb.append(child.write(prefix + "  "));

        return sb.toString();
    }
}
