import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Set;

public class BeaconSender implements Runnable {

    private AddressingTable table;
    private PacketQueue TCPqueue;
    private String ip;

    public BeaconSender(AddressingTable table, PacketQueue TCPqueue, String ip) {
        this.table = table;
        this.TCPqueue = TCPqueue;
        this.ip = ip;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

        while (true) {
            try {
                Set<String> vizinhos = table.getVizinhos();
                for (String vizinho : vizinhos) {
                    try {
                        Socket s = new Socket(vizinho, 8080);
                        DataOutputStream out = new DataOutputStream(s.getOutputStream());

                        // send beacon
                        Packet p = new Packet(vizinho, ip, 10, null);
                        out.write(p.serialize());
                        out.flush();
                        System.out.println("Enviei Beacon para " + p.getDest());

                        out.close();
                        s.close();

                    } catch (Exception e) {
                        // TODO: handle exception
                        System.out.println("Nao há vizinhos conectados..");

                    }
                }

                Thread.sleep(3000);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

    }

}
