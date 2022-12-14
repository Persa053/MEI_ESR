import java.nio.charset.StandardCharsets;
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
                Thread.sleep(7000);

                Set<String> vizinhos = table.getVizinhos();

                for (String vizinho : vizinhos) {
                    Instant start = Instant.now();
                    Instant wave = Instant.now();
                    String server = ip;
                    queue.add(new Packet(vizinho, table.getIp(vizinho), 5,
                            (ip + " 1 "
                                    + start.toString() + " "
                                    + Duration.ZERO.toString() + " "
                                    + server + " "
                                    + wave.toString())
                                    .getBytes(StandardCharsets.UTF_8)));
                }

            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }

    }

}
