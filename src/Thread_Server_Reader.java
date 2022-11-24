import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Thread_Server_Reader implements Runnable{
    private ServerSocket ss;
    private Bootstrapper bootstrapper;
    private PacketQueue queue;

    public Thread_Server_Reader(ServerSocket ss, Bootstrapper bootstrapper, PacketQueue queue) {
        this.ss = ss;
        this.bootstrapper = bootstrapper;
        this.queue = queue;
    }


    public void run() {
        while(true){
            try {
                Socket s = ss.accept();
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

                byte[] arr = new byte[4096];
                int size = in.read(arr, 0, 4096);
                byte[] content = new byte[size];
                System.arraycopy(arr, 0, content, 0, size);

                Packet packet = new Packet(content);

                System.out.println("["+ Thread.currentThread().getId() + "] Recebi o pacote de " + packet.getOrigem() +
                                            " tipo " + packet.getTipo() + "\n");

                switch (packet.getTipo()) {
                    case 1:
                        String adj = bootstrapper.getVizinhos(packet.getOrigem());
                        Packet rp = new Packet(packet.getOrigem(), packet.getDest(), 2, adj.getBytes(StandardCharsets.UTF_8));

                        System.out.println("["+ Thread.currentThread().getId() + "] Mandei o pacote para " + rp.getDest() +
                                            " tipo " + rp.getTipo() + "\n");

                        out.write(rp.serialize());
                        out.flush();


                }



            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
