package functions;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

/**
 * Classe que realiza a análise léxica para a linguagem raiz definida pela tabela de símbolos.
 * <p>
 * - Entrada: String[] (cada elemento é uma linha de código).
 * - Saída: AnaliseResult com sucesso + tokens reconhecidos, ou erro + mensagem.
 * </p>
 */
public class AnaliseLexica {

    /**
     * Tabela de símbolos
     */
    private Map<Integer, List<Token>> tabelaSimbolos = new HashMap<>();
    {
        // Linha 0 = categoria "Literal" inseridos na tabela durante a análise
        tabelaSimbolos.put(0, new ArrayList<>(List.of(
            new Token("true"), new Token("false"))));

        // Palavras reservadas
        tabelaSimbolos.put(1, new ArrayList<>(List.of( 
        new Token("if"), new Token("return"), new Token("else"), new Token("and"),
        new Token("not"), new Token("begin"), new Token("end"), new Token("readln"),
        new Token("write"), new Token("writeln"), new Token("true"), new Token("false"),
        new Token("final")
    )));
        // Tipos primitivos
        tabelaSimbolos.put(2, new ArrayList<>(List.of( 
        new Token("int"), new Token("float"), new Token("string"), new Token("byte"),
        new Token("boolean")
    )));
        // Símbolos especiais
        tabelaSimbolos.put(3, new ArrayList<>(List.of( 
        new Token("=="), new Token("="), new Token("("), new Token(")"), new Token("<"),
        new Token(">"), new Token("<>"), new Token(">="), new Token("<="), new Token("+"),
        new Token("-"), new Token("*"), new Token("/"), new Token(";"), new Token(","),
        new Token("{"), new Token("}")
    )));
        tabelaSimbolos.put(4, new ArrayList<>());
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
                if (c == '{') {
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
                    tokensReconhecidos.add(new Token(lexema, "Literal"));
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
                                tokensReconhecidos.add(new Token("0h" + hexPart, "Literal Byte"));
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
                            int valor = Integer.parseInt(lexema);
                            if (valor >= 0 && valor <= 255) {
                                tokensReconhecidos.add(new Token(lexema, "Literal Byte"));
                            } else {
                                tokensReconhecidos.add(new Token(lexema, "Literal"));
                            }
                        } catch (NumberFormatException e) {
                            return AnaliseResult.error("Número decimal inválido na linha " + linhaNumero + " coluna " + (inicio + 1));
                        }
                    } else {
                        tokensReconhecidos.add(new Token(lexema, "Literal Float"));
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
                        String nomeCat = getCategoriaNome(categoria);
                        tokensReconhecidos.add(new Token(lexema, nomeCat));
            
                    } else {
                        tokensReconhecidos.add(new Token(lexema, "Identificador"));
                        tabelaSimbolos.get(4).add(new Token(lexema, "Identificador")); 
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
            return new Token(lexema, getCategoriaNome(categoria));
        } else {
            // Para segurança, quando chamamos para símbolos conhecidos, deveríamos sempre achar a categoria.
            return new Token(lexema, "Símbolo Especial");
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
        for (Token token : res.tokens) {
            System.out.print(token.getLexema() + ", ");
        }
        if (!res.success) {
            System.err.println("Erro: " + res.errorMessage);
        } else {
            System.out.println("Tokens reconhecidos:");
            for (Token t : res.tokens) {
                System.out.println("  [" + t.getLexema() + "] -> " + t.getClasse());
            }
        }
    }

  

//   /* Exemplo de Programa na linguagem LC */
// "int n;",
// " string nome;",
// "boolean naoTerminou;",
// "final MAXITER=10;",
// "{ Bloco Principal }",
// "begin",
// "    write, "Digite seu nome: ";",
// "    readln, nome;",        
// "    naoTerminou=true;",
// "    n=0;",
// "    while naoTerminou begin",
// "        writeln,"Ola' ",   nome;",
// "        n=n+1;",
// "        naoTerminou=n<MAXITER;",
// "    end",
// "end"
}

/**
 * Representa um símbolo na tabela de símbolos de um compilador.
 * Cada símbolo possui um identificador único, um lexema (nome), e atributos como classe, tipo e endereço.
 */
class Token {

    // Contador estático para gerar IDs únicos automaticamente
    private static int nextid = 0;

    // Atributos imutáveis do símbolo
    private final int id;         // Identificador único do símbolo
    private final String lexema;  // Nome ou representação textual do símbolo
    private final int classe;     // Classe sintática (ex: identificador, palavra reservada), também pode ser usado para categoria
    private int tipo;       // Tipo de dado (ex: inteiro, string, byte)
    private int endereco;   // Endereço de memória ou posição na tabela
    /**
     * Construtor completo que gera ID automaticamente.
     */
    public Token(String lexema, int classe, int tipo, int endereco) {
        this.id = nextid++; // Gera ID único
        this.lexema = lexema;
        this.classe = classe;
        this.tipo = tipo;
        this.endereco = endereco;
    }

    /**
     * Construtor que define a classe com base na categoria textual.
     */
    public Token(String lexema, String categoria, int tipo, int endereco) {
        this.id = nextid++; // Gera ID único
        this.lexema = lexema;
        this.classe = categoria.equals("Palavra Reservada") ? 1 : categoria.equals("Tipo Primitivo") ? 2 : categoria.equals("Símbolo Especial") ? 3 : categoria.equals("Identificador") ? 4 : 0;
        this.tipo = tipo;
        this.endereco = endereco;
    }

    /**
     * Construtor simplificado que assume valores padrão para classe, tipo e endereço.
     */
    public Token(String lexema) {
        this(this.id = nextid++,lexema, 0, 0, 0);
    }
    

    /**
     * Construtor usado ao carregar símbolos do arquivo, com ID já definido.
     */
    public Token(int id, String lexema, int classe, int tipo, int endereco) {
        this.id = id;
        this.lexema = lexema;
        this.classe = classe;
        this.tipo = tipo;
        this.endereco = endereco;
    }

    public Token(String lexema, String classe) {
        this.id = nextid++;
        this.lexema = lexema;
        this.classe = classe.equals("Palavra Reservada") ? 1 : classe.equals("Tipo Primitivo") ? 2 : classe.equals("Símbolo Especial") ? 3 : classe.equals("Identificador") ? 4 : 0;
          
    }

    // Métodos de acesso (getters)
    public int getId() { return id; }
    public String getLexema() { return lexema; }
    public int getClasse() { return classe; }
    public int getTipo() { return tipo; }
    public int getEndereco() { return endereco; }

    /**
     * Converte o símbolo para uma linha CSV (para salvar no arquivo).
     */
    public String toCSV() {
        return id + "," + lexema + "," + classe + "," + tipo + "," + endereco;
    }

    /**
     * Converte uma linha CSV em um objeto Token.
     */
    public static Token fromCSV(String line) {
        String[] parts = line.split(",");
        return new Token(
            Integer.parseInt(parts[0]),
            parts[1],
            Integer.parseInt(parts[2]),
            Integer.parseInt(parts[3]),
            Integer.parseInt(parts[4])
        );
    }
}