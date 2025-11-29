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
    private Set<String> opLogicos = Set.of("==", "<", ">", "<>", ">=", "<=", "not", "and", "or");

    AnaliseSintatica(List<Token> tokens) {
        this.tokens = tokens;
    }

    Token advance() {
        return pos < tokens.size() ? tokens.get(pos++) : null;
    }
    public No getRaiz() {
        return this.raiz;
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
            parent.addChild(new No(advance().getLexema(), tk));
            return true;
        }
        return false;
    }

    private boolean acceptClass(Set<Integer> classes, No parent, String label) {
        Token tk = peek();
        if (tk != null && classes.contains(tk.getClasseId())) {
            parent.addChild(new No(label, tk));
            advance();
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
    }
    // accept é opcional , expect é obrigatório
    
    private void reportError(String message) throws RuntimeException {
        throw new RuntimeException("Syntax Error at position " + pos + ": " + message);
    }




    // ============ ÁRVORE SINTÁTICA ABSTRATA ==============

    private No parseS() {
        while (peek() != null) {
            if (parseDecl(raiz) == null) break;
        }
        if (parseBloco(raiz) == null) {
            reportError("Expected a block 'begin ... end'");
        }
        return raiz;
    }

    private No parseTipo(No pai) {
        No noTipo = new No("TYPE");
        String type = peek().getLexema();
        if (!acceptClass(Set.of(2), noTipo, type)) return null; // 2 -> tipo primitivo
        pai.addChild(noTipo);
        return noTipo;
    }

    private No parseId(No pai) {
        No noId = new No("id");
        String id = peek().getLexema();
        if (!acceptClass(Set.of(4), noId, id)) return null; // 4-> id
        pai.addChild(noId);
        return pai;
    }

    private No parseDecl(No pai) {
        No noDecl = new No("DECL");

        if (parseTipo(noDecl) == null)
            if (!accept("final", noDecl)) return null; // não é uma declaração
        
        do {
            expectClass(Set.of(4), noDecl, peek().getLexema());
            if (accept("=", noDecl)) {
                if (chooseExp(noDecl) == null) {
                    expectClass(Set.of(4, 5, 6, 0), noDecl, peek().getLexema()); 
                    // 0 -> literal ou boolean, 4 -> id, 5 -> inteiro, 6 -> string
                }
                else break;
            }
        }
        while (accept(",", noDecl));

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
        if (chooseExp(noAttr) == null) {
            noAttr.addChild(new No(peek().getLexema(), peek()));
            advance();
        }
        expect(";", noAttr);

        pai.addChild(noAttr);
        return noAttr;
    }
    private No chooseExp(No pai) {
        if (isExp(pai, "MATH")) {
            return parseExpMath(pai);
        }
        else if (isExp(pai, "LOGIC")) {
            return parseExpLogica(pai);
        }
        return null;
    }
    private boolean isExp(No pai, String expType) {
        int pos = this.pos;
        while (!peek().getLexema().equals(";")) {
            if (opAritmeticos.contains(peek().getLexema())) {
                if (expType.equals("MATH")) {
                    this.pos = pos;
                    return true;
                }
                else break;
            }
            else if (opLogicos.contains(peek().getLexema())) {
                if (expType.equals("LOGIC")) {
                    this.pos = pos;
                    return true;
                }
                else break;
            }
            advance();
        }
        this.pos = pos;
        return false;
    }


    // ============ EXPRESSÃO ARITMÉTICA ==============

    private No parseExpMath(No pai) {

        if (!isExp(pai, "MATH")) return null;
        No expMath = pai;
        while (expMath != null) { // não repetir desnecessariamente EXPA para recursão
            if (expMath.name.equals("EXPA")) {
                break;
            }
            expMath = expMath.getParent();
        }
        if (expMath == null) {
            expMath = new No("EXPA");
            pai.addChild(expMath);
        }
        if (EXPA_SUM(expMath) == null) return null;


        
        return expMath;
    }

    // === PRECEDÊNCIAS (menor para maior) ===
    private No EXPA_SUM(No pai) { // menor precedência
        if (EXPA_TERM(pai) == null) return null;

        while (parseSymbolExp(pai, "OPMATH", Set.of("+", "-")) != null) {
            expectNo(EXPA_TERM(pai));
        }
        return pai;
    }
    private No EXPA_TERM(No pai) {
        if (EXPA_UNARY(pai) == null) return null;

        while (parseSymbolExp(pai, "OPMATH", Set.of("*", "/")) != null) {
            expectNo(EXPA_UNARY(pai));
        }
        return pai;
    }
    private No EXPA_UNARY(No pai) {
        if (accept("+", pai) || accept("-", pai)) {
            expectNo(EXPA_NUMBER(pai));
            return pai;
        }
        return EXPA_NUMBER(pai);
    }
    private No EXPA_NUMBER(No pai) {
        if (acceptClass(Set.of(5, 6, 0), pai, peek().getLexema())) {
            return pai;
        }
        if (peek().getClasseId() == 4) { // identificador
            parseId(pai);
            return pai;
        }
        else if (accept("(", pai)) { // parênteses têm a maior precedência
            expectNo(parseExpMath(pai));
            expect(")", pai);
            return pai;
        }
        return null;
    }


    // ============ EXPRESSÃO LÓGICA ==============

    private No parseExpLogica(No pai) {

        No expl = new No("EXPL");
        pai.addChild(expl);

        if (EXPL_REL(expl) == null)
            return null;

        return expl;
    }
    private No EXPL_REL(No pai) {

        if (EXPL_OR(pai) == null)
            return null;

        while (parseSymbolExp(pai, "OPLOG", opLogicos) != null) {
            expectNo(EXPL_OR(pai));
        }

        return pai;
    }
    private No EXPL_OR(No pai) {
        if (EXPL_AND(pai) == null) return null;

        while (parseSymbolExp(pai, "OR", Set.of("or")) != null) {
            expectNo(EXPL_AND(pai));
        }

        return pai;
    }

    private No EXPL_AND(No pai) {
        if (EXPL_NOT(pai) == null) return null;
        while (parseSymbolExp(pai, "AND", Set.of("and")) != null) {
            expectNo(EXPL_NOT(pai));
        }
        return pai;
    }

    private No EXPL_NOT(No pai) {
        if (accept("not", pai)) {
            expectNo(EXPL_NOT(pai));
            return pai;
        }
        return EXPL_TERM(pai);
    }

    private No EXPL_TERM(No pai) {
        if (accept("(", pai)) {
            expectNo(EXPL_REL(pai));
            expect(")", pai);
            return pai;
        }
        int posBackup = pos; 
        if (parseExpMath(pai) != null) {
            return pai;
        }
        pos = posBackup; // se não for uma EXPA, a posição antiga é resgatada
        if (acceptClass(Set.of(0,5,6), pai, peek().getLexema())) {
            return pai;
        }
        if (peek().getClasseId() == 4) {
            parseId(pai);
            return pai;
        }
        return null;
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
            bloco.get("begin").addChild(cmd);
            while (!peek().getLexema().equals("end")) { // encerra no último end
                parseCmd(cmd);
            }
        }
        expect("end", bloco);
        pai.addChild(bloco);
        return bloco;
    }

    private No parseCmd(No cmd) {
        if (parseAttr(cmd) != null) return cmd;
        if (parseLinhaNula(cmd) != null) return cmd;
        if (accept("while", cmd)) return parseWhile(cmd);
        if (accept("if", cmd)) return parseIf(cmd);
        if (accept("readln", cmd)) return parseRead(cmd);
        if (accept("write", cmd) || accept("writeln", cmd)) return parseWrite(cmd);
        return null;
    }

    private No parseWhile(No cmd) {
        No child = cmd.get("while");
        expectNo(parseExpLogica(child));
        expectNo(parseBloco(child));
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
        No child = cmd.get("readln");
        expect(",", child);
        expectClass(Set.of(4), child, peek().getLexema());
        expect(";", child);
        return cmd;
    }    
    private No parseWrite(No cmd) {
        No child = cmd.get("writeln") != null ?
            cmd.get("writeln") : cmd.get("write");
        expect(",", child);
        
        expectClass(Set.of(4, 5, 6, 0), child, peek().getLexema());
        while (accept(",", child)) {
            expectClass(Set.of(4, 5, 6, 0), child, peek().getLexema());
        }
        expect(";", child);
        return cmd;
    }


    private No parseSymbolExp(No parent, String label, Set<String> symbols) {
        No node = new No(label);
        if (!symbols.contains(peek().getLexema())) return null;
        node.addChild(new No(advance().getLexema(), peek(-1))); // uma posição antes (operador)
        parent.addChild(node);
        return node;
    }

        private static int prec(No no) { // método para visualização na arvore.txt
        if (no == null) return 0;
        String name = no.name != null ? no.name : "";

        // precedência de 1 (menor) a 9 (maior)

        // Folhas (ids, consts) -> máxima precedência para evitar parênteses
        if (no.children == null || no.children.isEmpty()) return 9;

        // Operadores armazenados em nós específicos
        switch (name) {
            case "NOT": return 8;
            case "OPMATH":
                // operador aritmético está no filho 0
                if (!no.children.isEmpty()) {
                    String op = no.children.get(0).token.getLexema();
                    if ("*".equals(op) || "/".equals(op)) return 7;
                    if ("+".equals(op) || "-".equals(op)) return 6;
                }
                return 6;
            case "EXPA": return 6;
            case "OPLOG": return 5;
            case "EXPL_REL": return 5;
            case "and": return 4;
            case "or": return 2;
            case "EXPL": return 3;
            default: return 1;
        }
    }

    private static String toExpr(No no, No parent) { // método para visualização na arvore.txt
        if (no == null) return "";

        // Folha terminal (id, número, literal, parêntese token individual etc.)
        if (no.children == null || no.children.isEmpty()) {
            return no.token != null ? no.token.getLexema() : no.name;
        }

        List<No> f = no.children;

        // Detectar padrão de parênteses: primeiro filho '(' e último filho ')'
        if (f.size() >= 3 &&
            f.get(0).token != null && "(".equals(f.get(0).token.getLexema()) &&
            f.get(f.size()-1).token != null && ")".equals(f.get(f.size()-1).token.getLexema()) &&
            f.size() == 3) {
            // caso comum: '(' EXPR ')'
            return "(" + toExpr(f.get(1), no) + ")";
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        // Percorre filhos em ordem; se for nó operador (OPMATH/OPLOG) extrai o token do seu filho[0]
        for (No child : f) {

            String part;

            if (child.name != null && (child.name.equals("OPMATH") || child.name.equals("OPLOG"))) {
                // operador real está em child.children.get(0)
                if (child.children != null && !child.children.isEmpty() && child.children.get(0).token != null) {
                    part = child.children.get(0).token.getLexema();
                } else {
                    part = ""; // fallback
                }
            } else if (child.children != null && child.children.isEmpty()) {
                // token terminal
                part = child.token != null ? child.token.getLexema() : child.name;
            } else {
                // recursão normal (subexpressão)
                part = toExpr(child, no);
            }

            if (part == null) part = "";

            if (!first) sb.append(" ");
            sb.append(part);
            first = false;
        }

        String expr = sb.toString().trim();

        // aplicar parênteses se necessário (baseado em precedência)
        if (parent != null && prec(no) < prec(parent)) {
            return "(" + expr + ")";
        }
        return expr;
    }


    public static String reconstruir(No no) { // reconstrução de uma expressão a partir do Nó
        return toExpr(no, null);
    }

    public void printTree() {
        analisar();
        if (raiz != null && pos == tokens.size()) {
            raiz.print("");
        } else {
            System.out.println("Erro de sintaxe na posicao: " + pos);
        }
    }

    public void writeTree(String fileName) {

        String output = "";
        analisar();
        if (raiz != null && pos == tokens.size()) {
            output = raiz.write("");
        }
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream("src/output/arvore_" + fileName));
            os.write(output.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public No analisar() {
        pos = 0;
        parseS();
        return this.raiz;
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

    }

}
