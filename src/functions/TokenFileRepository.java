package functions;

import java.io.*;
import java.util.*;

/**
 * Gerencia operações de CRUD (Create, Read, Update, Delete) sobre um arquivo .txt
 * que funciona como banco de dados para os símbolos do compilador.
 */
public class TokenFileRepository {

    private final File file; // Arquivo onde os símbolos são armazenados

    /**
     * Construtor que inicializa o repositório e cria o arquivo se ele não existir.
     */
    public TokenFileRepository(String filePath) {
        this.file = new File(filePath);
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar arquivo: " + e.getMessage());
        }
    }

    /**
     * Adiciona um novo símbolo ao final do arquivo.
     */
    public void saveToken(Token Token) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(Token.toCSV());
            bw.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar símbolo: " + e.getMessage());
        }
    }

    /**
     * Lê todos os símbolos do arquivo e retorna como lista.
     */
    public List<Token> getAllTokens() {
        List<Token> Tokens = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Tokens.add(Token.fromCSV(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler símbolos: " + e.getMessage());
        }
        return Tokens;
    }

    /**
     * Atualiza um símbolo existente com base no ID.
     */
    public void updateToken(int id, Token updatedToken) {
        List<Token> Tokens = getAllTokens();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Token s : Tokens) {
                if (s.getId() == id) {
                    bw.write(updatedToken.toCSV());
                } else {
                    bw.write(s.toCSV());
                }
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao atualizar símbolo: " + e.getMessage());
        }
    }

    /**
     * Remove um símbolo do arquivo com base no ID.
     */
    public void deleteToken(int id) {
        List<Token> Tokens = getAllTokens();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Token s : Tokens) {
                if (s.getId() != id) {
                    bw.write(s.toCSV());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao deletar símbolo: " + e.getMessage());
        }
    }
}