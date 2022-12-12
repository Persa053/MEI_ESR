import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.io.BufferedInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public class Thread_Node_Reader implements Runnable {

    private final ServerSocket ss;
    private final AddressingTable table;
    private final PacketQueue queue;
    private final String ip;

    public Thread_Node_Reader(ServerSocket ss, AddressingTable table, PacketQueue queue, String ip) {
        this.ss = ss;
        this.table = table;
        this.queue = queue;
        this.ip = ip;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket s = ss.accept();

                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                Packet p = Packet.deserialize(in);

                switch (p.getTipo()) {
                    case 3:
                        System.out.println("Node recebeu tipo 3 de " + p.getOrigem());
                        if (!table.isStreaming()) {
                            System.out.println("Node mandou tipo 3 para " + table.getToServer());
                            Packet bla = new Packet(table.getToServer(), ip, 3, p.getDados());
                            System.out.println("getOrigem " + bla.getOrigem());
                            queue.add(bla);
                        }
                        table.turnOn(p.getOrigem());

                        // table.setisConsuming(true);
                        break;
                    case 5:
                        System.out.println("Current hops: " + table.getHops());
                        System.out.println("Current latency: " + table.getLatency().toString());
                        String dados = new String(p.getDados(), StandardCharsets.UTF_8);
                        // "1 start"
                        String[] array = dados.split(" ");
                        int hops = Integer.parseInt(array[0]);
                        Instant start = Instant.parse(array[1]);
                        Duration prev_latency = Duration.parse(array[2]);

                        System.out.println("Start: " + start.toString() + " milliseconds");

                        Duration timeElapsed = Duration.between(start, Instant.now());
                        System.out.println("Time elapsed before plus: " + timeElapsed.toMillis() + " milliseconds");
                        timeElapsed.plus(prev_latency);

                        System.out.println("Time elapsed: " + timeElapsed.toMillis() + " milliseconds");

                        // magia com tempos
                        Duration d1 = table.getLatency();
                        Duration d2 = timeElapsed;
                        Duration difference = d1.minus(d2);

                        Duration two_secs = Duration.ofMillis(2000);
                        int val = difference.compareTo(two_secs);

                        // se o novo caminho for melhor
                        // update hops to server
                        if (hops < table.getHops() || val > 0) {
                            // atualizar hops
                            table.setHops(hops);

                            // set best route
                            table.setToServer(p.getOrigem());

                            // atualizar tempo
                            table.setLatency(timeElapsed);
                            System.out.println("Updated hops: " + table.getHops());
                            System.out.println("Updated latency: " + table.getLatency().toString());
                        }

                        // reencaminha para todos menos o que lhe enviou
                        Set<String> vizinhos = table.getVizinhos();
                        vizinhos.remove(p.getOrigem());

                        for (String vizinho : vizinhos) {
                            start = Instant.now();
                            queue.add(new Packet(vizinho, ip, 5,
                                    ((table.getHops()+1) + " " + start.toString() + " " + table.getLatency().toString())
                                            .getBytes(StandardCharsets.UTF_8)));

                        }

                        break;
                    case 10:
                        System.out.println("Beacon");
                        break;
                    default:
                        System.out.println("Default");
                        break;
                }

                in.close();
                out.close();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
