package functions;

/**
 * Representa um símbolo na tabela de símbolos de um compilador.
 * Cada símbolo possui um identificador único, um lexema (nome), e atributos como classe, tipo e endereço.
 */
public class Token {

    // Contador estático para gerar IDs únicos automaticamente
    private static int nextid = 0;

    // Atributos imutáveis do símbolo
    private final int id;         // Identificador único do símbolo
    private final String lexema;  // Nome ou representação textual do símbolo
    private final String classe;     // Classe sintática (ex: identificador, palavra reservada), também pode ser usado para categoria
    private String tipo = "T";       // Tipo de dado (ex: inteiro, string, byte)
    private int endereco;   // Endereço de memória ou posição na tabela



    public Token(String lexema, int classe) {
        this.id = nextid++;
        this.lexema = lexema;
        this.classe = getClasseNome(classe);
          
    }

    public Token(String tipo, String lexema, int classe) {
        this.id = nextid++;
        this.lexema = lexema;
        this.classe = getClasseNome(classe);
        this.tipo = tipo;
    }

    // Métodos de acesso (getters)
    public int getId() { return id; }
    public String getLexema() { return lexema; }
    public String getClasse() { return classe; }
    public int getClasseId() {
        return this.classe.equals("Palavra Reservada") ? 1 : this.classe.equals("Tipo Primitivo") ? 2 : this.classe.equals("Símbolo Especial") ? 3 : this.classe.equals("Identificador") ? 4 :
        this.classe.equals("Literal Byte") ? 5 : this.classe.equals("Literal String") ? 6 : 0;
    }
    public String getTipo() { return tipo; }
    public int getEndereco() { return endereco; }

    public String getClasseNome(int classe) {
    switch (classe) {
        case 1: return"Palavra Reservada";
        case 2: return"Tipo Primitivo";
        case 3: return"Símbolo Especial";
        case 4: return"Identificador";
        case 5: return"Literal Byte";
        case 6: return"Literal String";
        default: return"Literal";
    }
}

}