import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Thread_Server_Reader implements Runnable {
    private ServerSocket ss;
    private Bootstrapper bootstrapper;
    private PacketQueue queue;
    private AddressingTable table;

    public Thread_Server_Reader(ServerSocket ss, Bootstrapper bootstrapper, PacketQueue queue, AddressingTable table) {
        this.ss = ss;
        this.bootstrapper = bootstrapper;
        this.queue = queue;
        this.table = table;
    }

    public void run() {
        while (true) {
            try {
                Socket s = ss.accept();
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

                Packet packet = Packet.deserialize(in);

                System.out.println("[" + Thread.currentThread().getId() + "] Recebi o pacote de " + packet.getOrigem() +
                        " tipo " + packet.getTipo() + "\n");

                switch (packet.getTipo()) {
                    // pedido Vizinhos
                    case 1:
                        String adj = bootstrapper.getVizinhos(this.table.getIps().get(packet.getOrigem()));
                        Packet rp = new Packet(packet.getOrigem(), packet.getDest(), 2,
                                adj.getBytes(StandardCharsets.UTF_8));

                        System.out.println(
                                "[" + Thread.currentThread().getId() + "] Mandei o pacote para " + rp.getDest() +
                                        " tipo " + rp.getTipo() + "\n");

                        out.write(rp.serialize());
                        out.flush();
                        break;
                    case 3:
                        // init Stream
                        System.out
                                .println("PACKET ORIGEM: " + packet.getOrigem() + "Packet Destino " + packet.getDest());
                        table.turnOn(packet.getOrigem());
                        System.out.println("init Stream");
                        break;
                    case 4:
                        // stop Stream
                        table.turnOff(packet.getOrigem());
                        System.out.println("stop Stream");
                        break;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
