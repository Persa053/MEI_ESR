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

                byte[] arr = new byte[4096];
                int size = in.read(arr, 0, 4096);
                byte[] content = new byte[size];
                System.arraycopy(arr, 0, content, 0, size);

                Packet packet = new Packet(content);

                System.out.println("[" + Thread.currentThread().getId() + "] Recebi o pacote de " + packet.getOrigem() +
                        " tipo " + packet.getTipo() + "\n");

                switch (packet.getTipo()) {
                    // pedido Vizinhos
                    case 1:
                        String adj = bootstrapper.getVizinhos(packet.getOrigem());
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
                        table.turnOn(packet.getOrigem());
                        System.out.println("init Stream");
                        Thread sender_udp = new Thread(new SenderUDP("default", table));
                        sender_udp.start();
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
