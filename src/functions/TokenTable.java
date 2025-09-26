package functions;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa a tabela de símbolos em memória, usada pelo compilador.
 * Integra com o repositório de arquivo para persistência dos dados.
 */
public class TokenTable {

    private final Map<Integer, Token> Ttable = new HashMap<>(); // Mapeia ID para símbolo
    private final TokenFileRepository repository;                   // Repositório de persistência

    /**
     * Construtor que carrega os símbolos do arquivo para a memória.
     */
    public TokenTable(String filePath) {
        this.repository = new TokenFileRepository(filePath);
        for (Token s : repository.getAllTokens()) {
            Ttable.put(s.getId(), s);
        }
    }

    /**
     * Cria um novo símbolo com valores padrão e salva no arquivo.
     */
    public void createToken(String lexema) {
        Token Token = new Token(lexema);
        Ttable.put(Token.getId(), Token);
        repository.saveToken(Token);
    }

    /**
     * Cria um novo símbolo com atributos completos e salva no arquivo.
     */
    public void createToken(String lexema, int classe, int tipo, int endereco) {
        Token Token = new Token(lexema, classe, tipo, endereco);
        Ttable.put(Token.getId(), Token);
        repository.saveToken(Token);
    }

    /**
     * Verifica se um caractere é uma letra (A-Z ou a-z).
     */
    public boolean isLetter(String ch) {
        return ch.matches("[a-zA-Z]");
    }

    /**
     * Verifica se um caractere é um dígito (0-9).
     */
    public boolean isDigit(String ch) {
        return ch.matches("[0-9]");
    }

    /**
     * Verifica se uma cadeia de caracteres representa uma string válida.
     */
    public boolean isString(String ch) {
        return ch.matches("\"[a-zA-Z_0-9]*[^\"\n]*\"|\"\"$");
    }
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
        this.classe = categoria.equals("Palavra Reservada") ? 1 : categoria.equals("Tipo Primitivo") ? 2 : categoria.equals("Símbolo Especial") ? 3 : 0;
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
        this.classe = classe.equals("Palavra Reservada") ? 1 : classe.equals("Tipo Primitivo") ? 2 : classe.equals("Símbolo Especial") ? 3 : 0;
          
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