import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.io.BufferedInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
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
                        System.out.println("\n\n\n");
                        System.out.println("Node recebeu tipo 3 de " + p.getOrigem());
                        if (!table.isStreaming()) {
                            System.out.println("NODE MANDOU TIPO 3 PARA " + table.getToServer());
                            System.out.println("\n\n\n");
                            Packet rp = new Packet(table.getToServer(), table.getIp(table.getToServer()), 3, p.getDados());
                            queue.add(rp);
                            System.out.println("Node mandou tipo 3 para " + table.getToServer());
                        }
                        table.turnOn(p.getOrigem());

                        // table.setisConsuming(true);
                        break;
                    case 4:
                        System.out.println("Node recebeu tipo 4 de " + p.getOrigem());
                        table.turnOff(p.getOrigem());
                        if (!table.isStreaming()) {
                            Packet rp = new Packet(table.getToServer(), table.getIp(table.getToServer()), 4, p.getDados());
                            queue.add(rp);
                            System.out.println("Node mandou tipo 4 para " + table.getToServer());
                        }
                        break;
                    case 5:

                        String sender = p.getOrigem();
                        System.out.println("GET ORIGEM= " + sender);

                        String dados = new String(p.getDados(), StandardCharsets.UTF_8);
                        // "1 start"
                        String[] array = dados.split(" ");

                        // atualizar valor na flooding table para true
                        String prev_sender = array[0];

                        int hops = Integer.parseInt(array[1]);

                        Instant start = Instant.parse(array[2]);
                        // Instant wave = Instant.parse(array[2]);
                        Duration prev_latency = Duration.parse(array[3]);

                        // System.out.println("prev_latency = " + prev_latency);

                        System.out.println("STARTTTTTTTTT = " + start.toString());
                        Duration timeElapsed = Duration.between(start, Instant.now());
                        System.out.println("Tempo de transmissão entre nodos=" + timeElapsed);
                        timeElapsed = timeElapsed.plus(prev_latency);
                        System.out.println("prev_latency = " + prev_latency);
                        System.out.println("timeElapsed = " + timeElapsed);

                        String server = array[4];
                        Instant wave = Instant.parse(array[5]);
                        // Map<String, Instant> floodTable2 = table.getFloodTable();

                        // update new route ---------------------
                        table.setLatency(sender, timeElapsed);
                        table.setHops(sender, hops);
                        System.out.println("Route " + sender + " updated");

                        table.UpdateRoutingTable(sender, server, hops, timeElapsed);

                        System.out
                                .println("Best route: " + table.getToServer() + "\nhops="
                                        + table.getHops(table.getToServer())
                                        + " tempo=" + table.getLatency(table.getToServer()));

                        // --------------------------------------

                        Map<String, Instant> floodTable = table.getFloodTable();

                        System.out.println("FloodTable=" + floodTable.toString());
                        System.out.println("-------------------------------");
                        // Se não existir o server na tabela ou for uma nova wave reencaminha o pacote
                        if (!floodTable.keySet().contains(server) || floodTable.get(server).isBefore(wave)) {

                            floodTable.put(server, wave);
                            // reencaminha para todos menos o que lhe enviou
                            Set<String> vizinhos = table.getVizinhosClone();
                            vizinhos.remove(p.getOrigem());
                            vizinhos.remove(prev_sender);

                            for (String vizinho : vizinhos) {
                                Instant current_start = Instant.now();
                                queue.add(new Packet(vizinho, table.getIp(vizinho), 5,
                                                (prev_sender + " "
                                                + (hops + 1) + " "
                                                + current_start.toString() + " "
                                                + timeElapsed.toString() + " "
                                                + server + " "
                                                + wave.toString())
                                                .getBytes(StandardCharsets.UTF_8)));
                            }
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
