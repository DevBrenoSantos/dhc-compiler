// Luigi Fernando
// Ulisses Santana


// AVISO: foi utiilzada a última versão do Java (25). Caso esteja utilizando uma versão anterior, favor atualizar para evitar erros de compilação.

import java.util.Scanner;
import functions.AnaliseLexica;
import functions.AnaliseSemantica;
import functions.AnaliseSintatica;

public class Compilador {

    private static Scanner input = new Scanner(System.in);    
    private static String fileName = "";
    public static void main(String[] args) {
        IO.println("\n============ COMPILADOR LC1 - Por Luigi Fernando e Ulisses Santana ============\n");
        menu(args);
    }

    public static void selectFile() {
        String resp = input.nextLine();
        switch (resp) {
            case "1":
                fileName = "codigo_fonte_LC.txt"; break;
            case "2":
                fileName = "codigo_fonte_2.txt"; break;
            case "3":
                fileName = "codigo_fonte_3.txt"; break;
            case "4":
                fileName = "codigo_fonte_4.txt"; break;
            case "5":
                IO.print("Digite o nome do arquivo (com extensão): ");
                fileName = input.nextLine();
                break;
            default:
                IO.println("Opção inválida. Selecione entre 1, 2 e 3.");
                resp = input.nextLine();
                selectFile();
                break;
        }

    }

    public static String selectAnalysis(String[] args) {
        String resp = input.nextLine();
        switch (resp) {
            case "1":
                AnaliseLexica.main(args, fileName);
                break;
            case "2":
                try {
                    AnaliseSintatica.main(args, fileName);
                } catch (Exception e) {
                    IO.println(e.getMessage());
                }
                break;
            case "3":
                try {
                    AnaliseSemantica.main(args, fileName);
                } catch (Exception e) {
                    IO.println(e.getMessage());
                }
                break;
            default:
                IO.println("Opção inválida. Selecione entre 1, 2 e 3.");
                resp = input.nextLine();
                return selectAnalysis(args);
        }
        return resp;
    }


    public static void menu(String[] args) {

        IO.print("""           
            Arquivos de Entrada
            [1]. codigo_fonte_LC.txt
            [2]. codigo_fonte2.txt
            [3]. codigo_fonte3.txt
            [4]. codigo_fonte4.txt
            [5]. Outro (deve estar presente no diretório src/codes)\n
            Escolha uma opção de arquivo de entrada:
            """
        );
        selectFile();

        IO.print("""            
            Tipos de Análise
            [1]. Análise léxica
            [2]. Análise léxica sintática
            [3]. Análise léxica, sintática e semântica
            Escolha qual análise deseja executar:
                """
            );
        String choice = selectAnalysis(args);

        if (!choice.equals("1")) {
            IO.println("\nFim da compilação! Você pode checar uma visualização da árvore sintática no arquivo src/output/arvore_" + fileName);
        }
        else IO.println("\nFim da compilação!");
        
        IO.println("Pressione \"r\" para voltar ao início, ou qualquer tecla para encerrar.\n");

        String again = input.nextLine();

        if (again.equals("r")) {
            menu(args);
        }
        else {
            IO.println("Compilador LC1 encerrado!");
            input.close();
        }


    }
    
}
