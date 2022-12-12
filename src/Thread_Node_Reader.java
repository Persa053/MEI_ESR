import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedInputStream;

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
                    default:
                        System.out.println("Default case");
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
