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
            PacketQueue pq = new PacketQueue()
;            server(ip, ss, bs, pq);

        } else if (args[0].equals("node") && args.length == 2)
                    nodo(ip, args[0], ss, new PacketQueue());

        else if (args[0].equals("cliente") && args.length == 2) ;



        else System.out.println("Número de argumentos errrado");
    }

    public static void server(String ip, ServerSocket ss, Bootstrapper bs, PacketQueue pq){

        Thread tr = new Thread(new Thread_Server_Writer(pq));
        Thread tw = new Thread(new Thread_Server_Reader(ss, bs, pq));

        tr.start();
        tw.start();

    }

    public static void nodo(String ip, String ipBootstrapper, ServerSocket ss, PacketQueue pq){


    }

    public static void cliente(String ip, ServerSocket ss, Bootstrapper bs, PacketQueue pq){

    }
}
