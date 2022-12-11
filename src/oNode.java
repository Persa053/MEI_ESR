import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
         * TIpos de packets
         * 1 - Nodo -> Server para saber os nodos vizinhos
         * 2 - Server -> Nodo para indicar vizinhos
         * 3 - Cliente -> Server para começar Stream
         *
         */

public class oNode {

    public static void main(String[] args) throws IOException {

        String ip = InetAddress.getLocalHost().getHostAddress();
        System.out.println(ip);
        ServerSocket ss = new ServerSocket(8080);

        if (args[0].equals("server")) {

            // Bootstrapper bs = new Bootstrapper("../config/teste1_bootstrapper");
            Bootstrapper bs = new Bootstrapper("../config/server_client_bootstrapper");
            PacketQueue pq = new PacketQueue();
            server(ip, ss, bs, pq);

        } else if (args[0].equals("node") && args.length == 2)
            nodo(ip, args[1], ss, new PacketQueue());

        else if (args[0].equals("cliente") && args.length == 2)
            cliente(ip, args[1], ss, new PacketQueue());

        else
            System.out.println("Número de argumentos errrado");
    }

    public static void server(String ip, ServerSocket ss, Bootstrapper bs, PacketQueue pq) {

        Thread tr = new Thread(new Thread_Server_Writer(pq));
        Thread tw = new Thread(new Thread_Server_Reader(ss, bs, pq));

        tr.start();
        tw.start();

    }

    public static void nodo(String ip, String ipBootstrapper, ServerSocket ss, PacketQueue pq) throws IOException {

        // Nodo pergunta ao server (que vai ser o bootstraper) os vizihnos
        AddressingTable table = recebeViz(ip, ipBootstrapper, pq);

    }

    public static void cliente(String ip, String ipBootstrapper, ServerSocket ss, PacketQueue queue)
            throws IOException {

        // Cliente pergunta ao server (que vai ser o bootstraper) os vizihnos
        AddressingTable table = recebeViz(ip, ipBootstrapper, queue);
        Map<Integer, RTPpacketQueue> queueMap = new HashMap<>();

        Thread tn_reader = new Thread(new Thread_Node_Reader(queue));
        Thread tn_writer = new Thread(new Thread_Node_Writer(queue));
    
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;

        while ((line = in.readLine()) != null) {

            queue.add(new Packet(ip, table.getSender(), 3,
                    String.valueOf(1).getBytes(StandardCharsets.UTF_8)));

            Thread display = new Thread(new ClientDisplay(at, queueMap.get(streamID), queueTCP, streamID, ip));
            display.start();

        }

    }

    public static AddressingTable recebeViz(String ip, String ipBootstrapper, PacketQueue pq) throws IOException {

        Packet p = new Packet(ipBootstrapper, ip, 1, " ".getBytes(StandardCharsets.UTF_8));
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

        System.out.println("[" + Thread.currentThread().getId() + "] Recebi o pacote de " + rp.getOrigem() +
                " tipo " + rp.getTipo() + "\n");

        in.close();
        out.close();
        s.close();

        String dados = new String(rp.getDados(), StandardCharsets.UTF_8);
        Set<String> vizinhos = new TreeSet<>(List.of(dados.split(",")));
        for (String st : vizinhos)
            System.out.println("-" + st + "-");

        return new AddressingTable(vizinhos, ipBootstrapper);
    }
}
