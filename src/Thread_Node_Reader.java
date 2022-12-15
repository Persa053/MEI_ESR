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
import java.util.TreeSet;

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
                        if (!table.isStreaming() && !p.getOrigem().equals(table.getToServer())) {
                            System.out.println("NODE MANDOU TIPO 3 PARA " + table.getToServer());
                            System.out.println("\n\n\n");
                            Packet rp = new Packet(table.getToServer(), table.getIp(table.getToServer()), 3,
                                    p.getDados());
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
                            Packet rp = new Packet(table.getToServer(), table.getIp(table.getToServer()), 4,
                                    p.getDados());
                            queue.add(rp);
                            System.out.println("Node mandou tipo 4 para " + table.getToServer());
                        }
                        break;
                    case 5:

                        String sender = p.getOrigem();
                        System.out.println("-------------------------------");
                        System.out.println("FLOOD de " + p.getOrigem());

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

                        Duration timeElapsed = Duration.between(start, Instant.now());
                        // System.out.println("Tempo de transmissão entre nodos=" + timeElapsed);
                        timeElapsed = timeElapsed.plus(prev_latency);
                        // System.out.println("prev_latency = " + prev_latency);
                        System.out.println("Tempo da rota= " + timeElapsed);

                        String server = array[4];

                        Instant wave = Instant.parse(array[5]);
                        // Map<String, Instant> floodTable2 = table.getFloodTable();

                        Set<String> alias = new TreeSet<>();
                        int i = 6;
                        for (; i < array.length; i++)
                            alias.add(array[i]);

                        Set<String> alias2 = new TreeSet<>();
                        if (i < array.length) {
                            for (; i < array.length; i++)
                                alias2.add(array[i]);
                        }
                        // FIM DO PARSE

                        // System.out.println("-------->" + alias);
                        Map<String, Instant> floodTable = table.getFloodTable();
                        // Se não existir o server na tabela ou for uma nova wave reencaminha o pacote
                        if (!floodTable.keySet().contains(server) || floodTable.get(server).isBefore(wave)
                                || floodTable.get(server).equals(wave)) {

                            floodTable.put(server, wave);

                            String best = table.getToServer();
                            // update new route ---------------------
                            table.setToServer(sender);
                            table.setLatency(sender, timeElapsed);
                            table.setHops(sender, hops);

                            table.UpdateRoutingTable(sender, server, hops, timeElapsed, wave);

                            if (best != null && table.getToServer() != null && !best.equals(table.getToServer())) {
                                System.out.println("UPDATED");
                                System.out.println("Old Best = " + best);
                                System.out.println("New Best = " + table.getToServer());

                                if (table.isStreaming()) {
                                    Packet stream = new Packet(table.getToServer(), table.getIp(table.getToServer()), 3,
                                            p.getDados());

                                    Packet stopStream = new Packet(best,
                                            table.getIp(best), 4,
                                            p.getDados());

                                    // table.turnOn(table.getToServer());
                                    queue.add(stream);
                                    System.out.println("Mandei pacote 3 para " + table.getToServer());

                                    // table.turnOff(best);
                                    queue.add(stopStream);
                                    System.out.println("Mandei pacote 4 para " + best);
                                }

                            }

                            System.out
                                    .println("BEST ROUTE: " + table.getToServer() + " hops="
                                            + table.getHops(table.getToServer())
                                            + " tempo=" + table.getLatency(table.getToServer()));

                            // --------------------------------------

                            // System.out.println("FloodTable=" + floodTable.toString());
                            StringBuilder sb = new StringBuilder();
                            for (String ss : this.table.getIps().values())
                                sb.append(" ").append(ss);

                            StringBuilder sb2 = new StringBuilder();
                            for (String ss : alias)
                                sb2.append(" ").append(ss);

                            System.out.println(sb.toString());
                            // reencaminha para todos menos o que lhe enviou
                            Set<String> vizinhos = table.getVizinhosClone();
                            vizinhos.remove(p.getOrigem());
                            for (String string : alias)
                                vizinhos.remove(string);

                            if (!alias2.isEmpty()) {
                                for (String string : alias2)
                                    vizinhos.remove(string);
                            }

                            for (String vizinho : vizinhos) {
                                Instant current_start = Instant.now();
                                System.out.println("--------------->mandei o para:" + vizinho + "\n");
                                queue.add(new Packet(vizinho, table.getIp(vizinho), 5,
                                        (prev_sender + " "
                                                + (hops + 1) + " "
                                                + current_start.toString() + " "
                                                + timeElapsed.toString() + " "
                                                + server + " "
                                                + wave.toString()
                                                + sb.toString()
                                                + sb2.toString())
                                                .getBytes(StandardCharsets.UTF_8)));
                                // System.out.println("Enviado para " + vizinho);
                                // System.out.println("------->" + ss.toString());
                            }

                        }

                        System.out.println("-------------------------------");
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
