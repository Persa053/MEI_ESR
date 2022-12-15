import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;
import java.time.Duration;
import java.time.Instant;

public class ServerMonitoring implements Runnable {

    private final AddressingTable table;
    private final PacketQueue queue;
    private final String ip;

    public ServerMonitoring(AddressingTable table, PacketQueue queue, String ip) {
        this.table = table;
        this.queue = queue;
        this.ip = ip;
    }

    @Override
    public void run() {

        while (true) {
            try {
                Thread.sleep(5000);

                Set<String> vizinhos = table.getVizinhos();

                for (String vizinho : vizinhos) {
                    Instant start = Instant.now();
                    Instant wave = Instant.now();
                    String server = ip;

                    StringBuilder s = new StringBuilder();
                    for (String ss : this.table.getIps().values())
                        s.append(" ").append(ss);

                    System.out.println("Flooding wave " + wave);
                    queue.add(new Packet(vizinho, table.getIp(vizinho), 5,
                            (ip + " 1 "
                                    + start.toString() + " "
                                    + Duration.ZERO.toString() + " "
                                    + server + " "
                                    + wave.toString() + s.toString())
                                    .getBytes(StandardCharsets.UTF_8)));
                    System.out.println("-------->" + s.toString());
                }

            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }

    }

}
