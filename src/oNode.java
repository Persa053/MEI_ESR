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

    public static void nodo(String ip, String ipBootstrapper, ServerSocket ss, PacketQueue pq) throws IOException {

        /*
        * TIpos de packets
        * 1 - Nodo -> Server para saber os nodos vizinhos
        * 2 - Server -> Nodo para indicar vizinhos
        *
        *
        * */

        // Nodo pergunta ao server (que vai ser o bootstraper) os vizihnos
        Packet p = new Packet(ip, ipBootstrapper, 1, " ".getBytes(StandardCharsets.UTF_8));
        Socket s = new Socket(p.getDest(), 8080);

        DataOutputStream out = new DataOutputStream(s.getOutputStream());
        DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

        out.write(p.serialize());
        out.flush();

        byte[] arr = new byte[4096];
        int size = in.read(arr, 0, 4096);
        byte[] content = new byte[size];
        System.arraycopy(arr, 0, content, 0, size);

        Packet rp = new Packet(content);

        in.close();
        out.close();
        s.close();

        String dados = new String(rp.getDados(), StandardCharsets.UTF_8);
        Set<String> vizinhos = new TreeSet<>(List.of(dados.split(",")));

        for (String st : vizinhos)
            System.out.println("-" + st + "-");


    }

    public static void cliente(String ip, ServerSocket ss, Bootstrapper bs, PacketQueue pq){

    }
}
