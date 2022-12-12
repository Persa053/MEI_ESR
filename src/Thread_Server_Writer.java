import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Thread_Server_Writer implements Runnable {
    private PacketQueue queue;

    public Thread_Server_Writer(PacketQueue queue) {
        this.queue = queue;

    }
    
    public void run() {
        while (true) {
            try {
                Packet packet = queue.remove();

                Socket s = new Socket(packet.getDest(), 8080);

                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                DataInputStream in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

                out.write(packet.serialize());
                out.flush();


                in.close();
                out.close();
                s.close();
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
