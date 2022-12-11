import java.io.File;

public class ServerStream {

    public ServerStream(String filename, AddressingTable table) {

        
    }

    public static void execute(String filename, AddressingTable table) {
        File f = new File(filename);
        if (f.exists()) {
            new ServerStream(filename, table);
        } else
            System.out.println("Ficheiro de video não existe: " + filename);
    }

}
