import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
         * TIpos de packets
         * 1 - Nodo -> Server para saber os nodos vizinhos
         * 2 - Server -> Nodo para indicar vizinhos
         * 3 - Cliente -> Server para começar Stream
         * 4 - Cliente -> Server para parar Stream
         * 5 - Server Flood 
         * 10 - Beacon 
         */

public class oNode {

    public static void main(String[] args) throws IOException {

        String ip = InetAddress.getLocalHost().getHostAddress();
        System.out.println("ip = " + ip);
        ServerSocket ss = new ServerSocket(8080);

        if (args[0].equals("server")) {
            Bootstrapper bs = new Bootstrapper("../config/teste1_bootstrapper");
            // Bootstrapper bs = new Bootstrapper("../config/server_client_bootstrapper",
            // ip);
            PacketQueue pq = new PacketQueue();
            server(ip, ss, bs, pq);

        } else if (args.length == 2) {

            if (args[0].equals("node")) {
                nodo(ip, args[1], ss, new PacketQueue());

            } else if (args[0].equals("cliente"))
                cliente(ip, args[1], ss, new PacketQueue());

        } else
            System.out.println("Número de argumentos errrado");
    }

    public static void server(String ip, ServerSocket ss, Bootstrapper bs, PacketQueue pq) {

        Map<String, String> viz = new HashMap<>();
            // Split the input string using the ";" delimiter
        String[] substrings = bs.getVizinhos(ip).split(";");

            // Iterate over the substrings and add them to the Map
        for (String substring : substrings) {
            // Split each substring using the "=" delimiter
            String[] keyValue = substring.split("=");
            // Add the key and value to the Map
            viz.put(keyValue[0], keyValue[1]);
        }
        AddressingTable table = new AddressingTable(viz);

        System.out.println(table.getVizinhos().toString());

        Thread tw = new Thread(new Thread_Server_Writer(pq));
        Thread tr = new Thread(new Thread_Server_Reader(ss, bs, pq, table));
        Thread stream = new Thread(new SenderUDP("default", table));
        Thread flood = new Thread(new ServerMonitoring(table, pq, ip));

        tr.start();
        tw.start();
        stream.start();
        flood.start();
        // beacon.start();

    }

    public static void nodo(String ip, String ipBootstrapper, ServerSocket ss, PacketQueue pq) throws IOException {

        // Nodo pergunta ao server (que vai ser o bootstraper) os vizihnos
        AddressingTable table = recebeViz(ip, ipBootstrapper, pq);
        // table.setToServer(ipBootstrapper);

        Thread forwarder = new Thread(new ForwarderRTP(table));
        Thread tw = new Thread(new Thread_Node_Writer(pq));
        Thread tr = new Thread(new Thread_Node_Reader(ss, table, pq, ip));

        // Thread beacon = new Thread(new BeaconSender(table, pq, ip));

        forwarder.start();
        tw.start();
        tr.start();
        // beacon.start();

        // Nodo pergunta ao server (que vai ser o bootstraper) os vizihnos

    }

    public static void cliente(String ip, String ipBootstrapper, ServerSocket ss, PacketQueue queue)
            throws IOException {

        // Cliente pergunta ao server (que vai ser o bootstraper) os vizihnos
        AddressingTable table = recebeViz(ip, ipBootstrapper, queue);
        RTPpacketQueue RTPqueue = new RTPpacketQueue();

        // Multiple streams
        // Map<Integer, RTPpacketQueue> queueMap = new HashMap<>();

        Thread tn_reader = new Thread(new Thread_Node_Reader(ss, table, queue, ipBootstrapper));
        Thread tn_writer = new Thread(new Thread_Node_Writer(queue));
        Thread rtp_reader = new Thread(new Client_RTP_Receiver(table, RTPqueue));

        // Thread beacon = new Thread(new BeaconSender(table, queue, ip));

        tn_writer.start();
        rtp_reader.start();
        tn_reader.start();
        // beacon.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;

        Thread display = new Thread(new ClientDisplay(table, RTPqueue, queue, ip));
        display.start();

        while ((line = in.readLine()) != null) {
            if (line.equals("exit")) {
                System.out.println("Goodbye have a great time!!!");
                System.exit(0);
            }

        }

    }

    public static AddressingTable recebeViz(String ip, String ipBootstrapper, PacketQueue pq) throws IOException {

        Packet p = new Packet(ipBootstrapper, ip, 1, "".getBytes(StandardCharsets.UTF_8));
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
        String[] arrr = dados.split(";");
        Map<String, String> viz = new HashMap<>();

        // Iterate over the substrings and add them to the Map
        for (String substring : arrr) {
            // Split each substring using the "=" delimiter
            String[] keyValue = substring.split("=");
            // Add the key and value to the Map
            viz.put(keyValue[0], keyValue[1]);
        }

        System.out.println("Vizinhos=" + viz.toString());

        return new AddressingTable(viz);
    }
}
