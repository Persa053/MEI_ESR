import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;



public class oNode {

    public static void main(String[] args) throws IOException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        ServerSocket ss = new ServerSocket(8080);

        if (args[0].equals("server")) {

            Bootstrapper bs = new Bootstrapper("../config/bootstrapper");
            server(ip, ss, bs);

        } else if (args[0].equals("node")) nodo(ip, ss, args[0], );

        else System.out.println("Número de argumentos errrado");
    }

    public static void server(String ip, ServerSocket ss, Bootstrapper bs){

    }

    public static void nodo(String ip, String ipBootstrapper, ){

    }
}
