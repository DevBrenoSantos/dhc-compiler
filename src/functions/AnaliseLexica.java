package functions;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
/**
 * Classe que realiza a análise léxica para a linguagem raiz definida pela tabela de símbolos.
 * - Entrada: String[] (cada elemento é uma linha de código).
 * - Saída: AnaliseResult com sucesso + tokens reconhecidos, ou erro + mensagem.
 */
public class AnaliseLexica {

    /**
     * Tabela de símbolos
     */
    private Map<Integer, List<Token>> tabelaSimbolos = new HashMap<>();
    {
        // Linha 0 = categoria "Literal" inseridos na tabela durante a análise
        tabelaSimbolos.put(0, new ArrayList<>(List.of(
            new Token("true", 0), new Token("false", 0))));

        // linha 1 = Palavras reservadas
        tabelaSimbolos.put(1, new ArrayList<>(List.of( 
        new Token("if", 1), new Token("return", 1), new Token("else", 1), new Token("and", 1),
        new Token("not", 1), new Token("begin", 1), new Token("end", 1), new Token("readln", 1),
        new Token("write", 1), new Token(" writeln", 1), new Token("true", 1), new Token("false", 1),
        new Token("final", 1)
    )));
        // linha 2 = Tipos primitivos
        tabelaSimbolos.put(2, new ArrayList<>(List.of( 
        new Token("int", 2), new Token("float", 2), new Token("string", 2), new Token("byte", 2),
        new Token("boolean", 2)
    )));
        // linha 3 = Símbolos especiais
        tabelaSimbolos.put(3, new ArrayList<>(List.of( 
        new Token("==", 3), new Token("=", 3), new Token("(", 3), new Token(")", 3), new Token("<", 3),
        new Token(">", 3), new Token("<>", 3), new Token(">=", 3), new Token("<=", 3), new Token("+", 3),
        new Token("-", 3), new Token("*", 3), new Token("/", 3), new Token(";", 3), new Token(",", 3),
        new Token("{", 3), new Token("}", 3)
    )));
        
        tabelaSimbolos.put(4, new ArrayList<>()); // Identificadores
        tabelaSimbolos.put(5, new ArrayList<>()); // Literal byte
        tabelaSimbolos.put(6, new ArrayList<>()); // literal string
    }

    /**
     * Conjunto de símbolos especiais (Strings). Inicializado a partir da tabelaSimbolos
     * para permitir lookup rápido, incluindo símbolos compostos (==, <=, >=, <>).
     */
    private final Set<String> simbolosEspeciaisSet = new HashSet<>();

    /**
     * Mapa rápido lexema -> nome da categoria (ex: "if" -> "Palavra Reservada").
     * Construído a partir da tabelaSimbolos.
     */
    private final Map<String, String> mapaLexemaParaCategoria = new HashMap<>();

    /**
     * Construtor: prepara estruturas auxiliares (set de símbolos e mapa de categorias).
     */
    public AnaliseLexica() {
        for (Map.Entry<Integer, List<Token>> entry : tabelaSimbolos.entrySet()) {
            int categoria = entry.getKey();
            for (Token t : entry.getValue()) {
                String lex = t.getLexema();
                mapaLexemaParaCategoria.put(lex.toLowerCase(), getCategoriaNome(categoria));
                if (categoria == 3) {
                    simbolosEspeciaisSet.add(lex);
                }
            }
        }
    }

    /**
     * Obtém o nome de categoria a partir da chave (1,2,3...).
     * Mantido privado para uso interno e fácil alteração de nomenclaturas.
     */
    private String getCategoriaNome(int categoria) {
        switch (categoria) {
            case 0: return "Literal";
            case 1: return "Palavra Reservada";
            case 2: return "Tipo Primitivo";
            case 3: return "Símbolo Especial";
            case 4: return "Identificador";
            case 5: return "Literal Byte";
            case 6: return "Literal String";
            default: return "Desconhecido";
        }
    }

    /**
     * Busca na tabelaSimbolos a chave (categoria) pelo lexema fornecido.
     * Retorna a chave (Integer) ou null se não encontrado.
     */
    private Integer getKeyByValue(String value) {
        if (value == null) return null;
        String procura = value.toLowerCase();
        for (Entry<Integer, List<Token>> entry : tabelaSimbolos.entrySet()) {
            Integer key = entry.getKey();
            List<Token> tokens = entry.getValue();
            for (Token token : tokens) {
                if (token.getLexema().equalsIgnoreCase(procura)) {
                    return key;
                }
            }
        }
        return null;
    }

    /**
     * Verifica se a string dada é um símbolo especial conhecido.
     */
    private boolean isSimboloEspecial(String s) {
        return s != null && simbolosEspeciaisSet.contains(s);
    }


    /**
     * Função pública principal: recebe um array de linhas (String[]) e realiza a análise léxica.
     * Retorna AnaliseResult com tokens em caso de sucesso ou erro descritivo em caso de falha.
     *
     * Regras básicas:
     * - Reconhece símbolos compostos (==, <=, >=, <>).
     * - Reconhece literais string entre aspas duplas (ex: "texto").
     * - Reconhece números inteiros e floats como Literais (categoria "Literal").
     * - Reconhece identificadores (alfanuméricos que não são palavras reservadas).
     */
    public AnaliseResult analisar(String linha) {
        if (linha == null) {
            return AnaliseResult.error("Entrada nula.");
        }

        List<Token> tokensReconhecidos = new ArrayList<>();
        int linhaNumero = 0;


            int i = 0;
            int len = linha.length();
            while (i < len) {
                char c = linha.charAt(i);

                // Ignora espaços e tabs
                if (Character.isWhitespace(c)) {
                    i++;
                    continue;
                }

                // Ignora comentários { ... }
                if (c == '{' ) {
                    int inicio = i;
                    i++; // pula o '{'
                    boolean fechado = false;
                    while (i < len) {
                        char nc = linha.charAt(i);
                        if (nc == '}') {
                            fechado = true;
                            i++; // consome o '}'
                            break;
                        } else {
                            i++;
                        }
                    }
                    if (!fechado) {
                        return AnaliseResult.error("Erro léxico: comentário não terminado na linha " + linhaNumero + " começando em coluna " + (inicio+1));
                    }
                    continue; // pula o comentário
                }

                // ignora comentários /* ... */
                if (c == '/' && i + 1 < len && linha.charAt(i + 1) == '*'){
                    int inicio = i;
                    i += 2;
                    boolean fechado = false;
                    while (i + 1 < len){
                        char nc = linha.charAt(i);
                        char nc2 = linha.charAt(i + 1);
                        if (nc =='*' && nc2 == '/'){
                            fechado = true;
                            i += 2; // consome o '*/'
                            break;

                        }
                        else{
                            i++;
                        }
                    }
                    if (!fechado){
                        return AnaliseResult.error("Erro léxico: comentário não terminado na linha " + linhaNumero + " começando em coluna " + (inicio+1));
                    }
                    else{
                        continue; // pula o comentário
                    }
                }

                // parenteses para expressões

                if (c == '(') {
                    boolean fechado;
                    int inicio = i;
                    i++; // pula o '('
                    tokensReconhecidos.add(new Token("(", 3));
                    fechado = false;
                    int pos = i;
                    while (i < len) {
                        char nc = linha.charAt(i);
                        if (nc == ')') {
                            fechado = true;
                            i++; // consome o ')'
                            break;
                        } else {
                            i++;
                        }
                    }

                    if (!fechado) {
                        return AnaliseResult.error("Erro léxico: comentário não terminado na linha " + linhaNumero + " começando em coluna " + (inicio+1));
                    }
                    else i = pos;
                    continue; // pula o parênteses
                }
                                
                // 1) Literais de string entre aspas duplas
                if (c == '"' ) {
                    int inicio = i;
                    i++; // pula a primeira aspas
                    StringBuilder sb = new StringBuilder();
                    boolean fechado = false;
                    while (i < len) {
                        char nc = linha.charAt(i);
                        if (nc == '\\' && i + 1 < len) { // suporta escape simples \" ou \\ 
                            char esc = linha.charAt(i + 1);
                            sb.append(nc).append(esc);
                            i += 2;
                            continue;
                        }
                        if (nc == '"') {
                            fechado = true;
                            i++; // consome a aspas final
                            break;
                        } else {
                            sb.append(nc);
                            i++;
                        }
                    }
                    if (!fechado) {
                        return AnaliseResult.error("Erro léxico: literal não terminado na linha " + linhaNumero + " começando em coluna " + (inicio+1));
                    }
                    String lexema = "\"" + sb.toString() + "\"";
                    tokensReconhecidos.add(new Token("string", lexema, 6));
                    continue;
                }

                // 2) Símbolos especiais (tentar combinar o maior primeiro: 2-char then 1-char)
                // olha 2 caracteres à frente se possível
                if (i + 1 < len) {
                    String dois = linha.substring(i, i + 2);
                    if (isSimboloEspecial(dois)) {
                        tokensReconhecidos.add(criarTokenClassificado(dois));
                        i += 2;
                        continue;
                    }
                }
                // 1-char símbolo
                String um = String.valueOf(c);
                if (isSimboloEspecial(um)) {
                    tokensReconhecidos.add(criarTokenClassificado(um));
                    i++;
                    continue;
                }

                // 3) Números: inteiro, float ou byte (decimal ou hexadecimal)
                if (Character.isDigit(c) || (c == '0' && i + 2 < len && linha.charAt(i + 1) == 'h')) {
                    int inicio = i;

                    // Hexadecimal byte: formato 0hHH
                    if (linha.charAt(i) == '0' && linha.charAt(i + 1) == 'h') {
                        String hexPart = linha.substring(i + 2, Math.min(i + 4, len));
                        if (hexPart.length() == 2 && hexPart.matches("[0-9A-F]{2}")) {
                            int valorDecimal = Integer.parseInt(hexPart, 16);
                            if (valorDecimal <= 255) {
                                tokensReconhecidos.add(new Token("byte", "0h" + hexPart, 5));
                                i += 4;
                                continue;
                            } else {
                                return AnaliseResult.error("Valor hexadecimal fora do intervalo byte (0h" + hexPart + ") na linha " + linhaNumero);
                            }
                        } else {
                            return AnaliseResult.error("Formato hexadecimal inválido na linha " + linhaNumero + " coluna " + (i + 1));
                        }
                    }

                    // Decimal ou float
                    boolean temPonto = false;
                    StringBuilder sb = new StringBuilder();
                    while (i < len) {
                        char nc = linha.charAt(i);
                        if (Character.isDigit(nc)) {
                            sb.append(nc);
                            i++;
                        } else if (nc == '.' && !temPonto && i + 1 < len && Character.isDigit(linha.charAt(i + 1))) {
                            temPonto = true;
                            sb.append(nc);
                            i++;
                        } else {
                            break;
                        }
                    }

                    String lexema = sb.toString();
                    if (!temPonto) {
                        try {
                            tokensReconhecidos.add(new Token("int", lexema, 5)); // int
                        } catch (NumberFormatException e) {
                            return AnaliseResult.error("Número decimal inválido na linha " + linhaNumero + " coluna " + (inicio + 1));
                        }
                    } else {
                        return AnaliseResult.error("Compilador não suporta float.");
                    }
                    continue;
                }

                // 4) Identificadores ou palavras reservadas: letra ou underscore seguido de letras/digitos/underscore
                if (Character.isLetter(c) || c == '_') {
                    StringBuilder sb = new StringBuilder();
                    while (i < len) {
                        char nc = linha.charAt(i);
                        if (Character.isLetterOrDigit(nc) || nc == '_') {
                            sb.append(nc);
                            i++;
                        } else {
                            break;
                        }
                    }
                    String lexema = sb.toString();

                    Integer categoria = getKeyByValue(lexema);
                    if (categoria != null) {
                        tokensReconhecidos.add(new Token(lexema, categoria));
            
                    } else {
                        tokensReconhecidos.add(new Token(lexema, 4));
                        tabelaSimbolos.get(4).add(new Token(lexema, 4)); 
                    }
                    continue;
                }

                // 5) Qualquer outro caractere: caractere inválido (erro léxico)
                return AnaliseResult.error("Erro léxico: caractere inesperado '" + c + "' na linha " + linhaNumero + " coluna " + (i + 1));
            
        }

        return AnaliseResult.ok(tokensReconhecidos);
    }

    /**
     * Cria um Token já classificado por categoria (Palavra Reservada, Tipo Primitivo, Símbolo Especial)
     * a partir do lexema. Se não estiver na tabela, marca como Símbolo Especial por padrão para
     * os símbolos passados aqui (chamada apenas quando já foi detectado como símbolo).
     */
    private Token criarTokenClassificado(String lexema) {
        Integer categoria = getKeyByValue(lexema);
        if (categoria != null) {
            return new Token(lexema, categoria);
        } else {
            // Para segurança, quando chamamos para símbolos conhecidos, deveríamos sempre achar a categoria.
            return new Token(lexema, 3);
        }
    }

     /**
     * Resultado da análise léxica.
     * - success: true se não houve erro e tokens foram extraídos.
     * - tokens: lista de tokens reconhecidos (null se erro).
     * - errorMessage: mensagem descritiva em caso de erro (null se sucesso).
     */
    public static class AnaliseResult {
        public final boolean success;
        public final List<Token> tokens;
        public final String errorMessage;

        private AnaliseResult(boolean success, List<Token> tokens, String errorMessage) {
            this.success = success;
            this.tokens = tokens;
            this.errorMessage = errorMessage;
        }

        public static AnaliseResult ok(List<Token> tokens) {
            return new AnaliseResult(true, tokens, null);
        }

        public static AnaliseResult error(String msg) {
            return new AnaliseResult(false, null, msg);
        }
    }

    

    /* -----------------------------
       Exemplo de teste 
       -----------------------------
    */
    public static void main(String[] args, String fileName) {
        AnaliseLexica analise = new AnaliseLexica();
        InputStream in;
        String entrada = null;
        try {
            in = new BufferedInputStream(new FileInputStream("src/codes/" + fileName));
            entrada = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            in.close();
        } catch (Exception e) {
            IO.println(e.getMessage());
        }


        AnaliseResult res = analise.analisar(entrada);
        for (Token token : res.tokens) {
            System.out.print(token.getLexema() + ", ");
        }
        if (!res.success) {
            System.err.println("Erro: " + res.errorMessage);
        } else {
            System.out.println("Tokens reconhecidos:");
            for (Token t : res.tokens) {
                System.out.println("  [" + t.getLexema() + "] -> " + t.getClasseId());
            }
        }
    }

}

