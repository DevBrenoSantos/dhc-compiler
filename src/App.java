import functions.readFile;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        readFile.read(args);
    }
}
// java -cp bin App test.txt