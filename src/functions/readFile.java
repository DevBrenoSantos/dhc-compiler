package functions;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class readFile {
    public static void read(String[] file) {
        if (file.length != 1) {
            System.err.println("Uso: java App <caminho-do-arquivo>");
            System.exit(2);
        }

        String path = file[0];

        try (InputStream in = new BufferedInputStream(new FileInputStream(path))) {
            int b;
            while ((b = in.read()) != -1) {
                System.out.write(b);
            }
            System.out.flush();
        } catch (IOException e) {
            System.out.println("Erro de I/O: " + e.getMessage());
            System.exit(1);
        }
    }
}