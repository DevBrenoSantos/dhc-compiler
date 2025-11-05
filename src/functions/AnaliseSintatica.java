package functions;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import functions.AnaliseLexica.AnaliseResult;

public class AnaliseSintatica {

    private List<Token> tokens;
    private int pos = 0;
    private No raiz = new No("S");
    private Set<String> opAritmeticos = Set.of("+", "-", "*", "/");
    private Set<String> opLogicos = Set.of("==", "<", ">", "<>", ">=", "<=");
    private Set<String> andOr = Set.of("and", "or");
    AnaliseSintatica(List<Token> tokens) {
        this.tokens = tokens;
    }

    Token advance() {
        return pos < tokens.size() ? tokens.get(pos++) : null;
    }
    private Token peek() {
        return pos < tokens.size() ? tokens.get(pos) : null;
    }
    private Token peek(int offset) {
        return pos+offset < tokens.size() ? tokens.get(pos+offset) : null;
    }
    
    private boolean accept(String lexeme, No parent) {
        Token tk = peek();
        if (tk != null && tk.getLexema().equals(lexeme)) {
            parent.addChild(new No(advance().getLexema()));
            return true;
        }
        return false;
    }
    private boolean acceptClass(Set<Integer> classes, No parent, String label) {
        Token tk = peek();
        if (tk != null && classes.contains(tk.getClasse())) {
            parent.addChild(new No(label));
            advance();
            return true;
        }
        return false;
    }
    private boolean acceptNo(No expected, No parent) {
        if (expected != null) {
            parent.addChild(expected);
            return true;
        }
        return false;
    }
    
    private void expect(String lexeme, No parent) {
        if (!accept(lexeme, parent)) 
            reportError("Expected '" + lexeme + "' " + "got " + peek().getLexema());
    }
    private void expectClass(Set<Integer> classes, No parent, String label) {
        if (!acceptClass(classes, parent, label)) 
            reportError("Expected one of classes: " + classes.toString() + "got " + peek().getLexema());
    }
    private void expectNo(No expected) {
        if (expected == null) {
            reportError("Expected node not found");
            return;
        }
        //parent.add(expected);
    }
    
    private void reportError(String message) throws RuntimeException {
        throw new RuntimeException("Syntax Error at position " + pos + ": " + message);
    }

    // ÁRVORE SINTÁTICA ABSTRATA

    private No parseS() {
        while (peek() != null) {
            if (parseDecl(raiz) == null && parseAttr(raiz) == null &&
                parseBloco(raiz) == null && parseLinhaNula(raiz) == null) {
                throw new RuntimeException("Erro de sintaxe na posicao: " + pos);
            }
        }
        return raiz;
    }

    private No parseTipo(No pai) {
        No noTipo = new No("TYPE");
        String type = peek().getLexema();
        if (!acceptClass(Set.of(2), noTipo, type)) return null;
        pai.addChild(noTipo);
        return noTipo;
    }

    private No parseId(No pai) {
        No noId = new No("id");
        String id = peek().getLexema();
        if (!acceptClass(Set.of(4), noId, id)) return null;
        pai.addChild(noId);
        return pai;
    }

    private No parseDecl(No pai) {
        No noDecl = new No("DECL");

        if (parseTipo(noDecl) == null)
            if (!accept("final", noDecl)) return null; // não é uma declaração
        
        expectClass(Set.of(4), noDecl, peek().getLexema());

        while (accept(",", noDecl))
            expectClass(Set.of(4), noDecl, peek().getLexema());

        if (accept("=", noDecl))
            if (!acceptClass(Set.of(4, 0), noDecl, peek().getLexema())) return null;

        expect(";", noDecl);
        pai.addChild(noDecl);
        return noDecl;
    }

    private No parseAttr(No pai) {
        No noAttr = new No("ATTR");
        if (parseId(noAttr) == null) return null; // não é uma atribuição

        if (!peek().getLexema().equals("=") ) {
            pos--; // volta o ponteiro se não encontrar '='
            return null;
        }

        expect("=", noAttr);
        chooseExp(noAttr);
        expect(";", noAttr);

        pai.addChild(noAttr);
        return noAttr;
    }
    private No chooseExp(No pai) {

        if(parseTipo(pai) != null) {
            parseExpMath(pai);
            return pai;
        }
        advance();

        if (opAritmeticos.contains(peek().getLexema())) {
            pos--;
            parseExpMath(pai);
            return pai;
        }
        else pos--;
        parseExpLogica(pai);
        return pai;
    }
    private No parseExpLogica(No pai) { // (id | const) OPLOG (id | const) [ANDOR EXPL]*
        No expLogica = new No("EXPL");

        expectClass(Set.of(4, 0), expLogica, peek().getLexema()); // (id | const)
        No oplog = parseSymbolExp(expLogica, "OPLOG", opLogicos);
        if (oplog != null) {
            expectClass(Set.of(4, 0), expLogica, peek().getLexema());
        }

        No andor = parseSymbolExp(expLogica, "ANDOR", andOr);
        if (andor != null) {
            expectNo(parseExpLogica(expLogica));
        }

        //pai.add(expLogica);
        return expLogica;
    }
    private No parseExpMath(No pai) {
        No expMath = new No("EXPA");
        expectClass(Set.of(4, 0), expMath, peek().getLexema());

        parseSymbolExp(expMath, "OPMATH", opAritmeticos);

        do {
            expectClass(Set.of(4, 0), expMath, peek().getLexema());
        } while ((parseSymbolExp(expMath, "OPMATH", opAritmeticos)) != null);

        pai.addChild(expMath);
        return expMath;
    }
    private No parseLinhaNula(No pai) {
        No linhaNula = new No("NULO");
        if (!accept(";", linhaNula)) return null; // não é uma linha nula
        pai.addChild(linhaNula);

        return linhaNula;
    }

    private No parseBloco(No pai) {
        No bloco = new No("BLOCO");
        if (!accept("begin", bloco)) return null; // não é um bloco
        if (!peek().getLexema().equals("end")) {
            No cmd = new No("CMD");
            bloco.getChildByName("begin").addChild(cmd);
            while (!peek().getLexema().equals("end")) {
                parseCmd(cmd);
            }
        }

        expect("end", bloco);

        pai.addChild(bloco);
        return bloco;
    }

    private No parseCmd(No cmd) {
        // adicionar No para cada comando: writeln etc., e então adiciona-los para o No CMD
        if (parseAttr(cmd) != null) return cmd;
        if (accept("while", cmd)) return parseWhile(cmd);
        if (accept("if", cmd)) return parseIf(cmd);
        if (accept("readln", cmd)) return parseRead(cmd);
        if (accept("write", cmd) || accept("writeln", cmd)) return parseWrite(cmd);
        return null;
    }

    private No parseWhile(No cmd) {
        No child = cmd.getChildByName("while");
        expectNo(parseExpLogica(child));
        expectNo(parseBloco(child));
        cmd.addChild(child);
        return child;
    }

    private No parseIf(No cmd) {
        expectNo(parseExpLogica(cmd));
        expectNo(parseBloco(cmd));
        while (accept("else", cmd)) {
            if (accept("if", cmd)) {
                expectNo(parseExpLogica(cmd));
                expectNo(parseBloco(cmd));
            } 
            else return null;
        }
        if (accept("else", cmd)) {
            expectNo(parseBloco(cmd));
        }
        return cmd;      
    }
    private No parseRead(No cmd) {
        No child = cmd.getChildByName("readln");
        expect(",", child);
        expectClass(Set.of(4), child, peek().getLexema());
        expect(";", child);
        return cmd;
    }    
    private No parseWrite(No cmd) {
        No child = cmd.getChildByName("writeln") != null ?
            cmd.getChildByName("writeln") : cmd.getChildByName("write");
        expect(",", child);
        expectClass(Set.of(4, 0), child, peek().getLexema());
        while (accept(",", child)) {
            expectClass(Set.of(4, 0), child, peek().getLexema());
        }
        expect(";", child);
        return cmd;
    }


    private No parseSymbolExp(No parent, String label, Set<String> symbols) {
        No node = new No(label);
        if (!symbols.contains(peek().getLexema())) return null;
        node.addChild(new No(advance().getLexema()));
        parent.addChild(node);
        return node;
    }

    public void printTree() {
        pos = 0;
        No raiz = parseS();
        if (raiz != null && pos == tokens.size()) {
            raiz.print("");
        } else {
            System.out.println("Erro de sintaxe na posicao: " + pos);
        }
    }

    public void writeTree() {

        pos = 0;
        String output = "";
        No raiz = parseS();
        if (raiz != null && pos == tokens.size()) {
            output = raiz.write("");
        }
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream("src/output/arvore.txt"));
            os.write(output.getBytes());
            os.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    }

}

