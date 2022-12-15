import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

public class Client_RTP_Receiver implements Runnable {

    private final AddressingTable table;
    private final RTPpacketQueue RTPqueue;

    public Client_RTP_Receiver(AddressingTable table, RTPpacketQueue RTPqueue) {
        this.table = table;
        this.RTPqueue = RTPqueue;
    }

    @Override
    public void run() {

        try {
            int RTP_RCV_PORT = 25000;
            DatagramSocket RTPsocket = new DatagramSocket(RTP_RCV_PORT);

            while (true) {
                byte[] cBuf = new byte[15000];
                DatagramPacket rcvdp = new DatagramPacket(cBuf, cBuf.length);

                RTPsocket.receive(rcvdp);

                RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());

                Map<String, Boolean> ips = table.getStreamingTable();
                for (String ip : ips.keySet()) {
                    if (ips.get(ip)) {
                        int RTP_dest_port = 25000;
                        DatagramPacket senddp = new DatagramPacket(rcvdp.getData(), rcvdp.getData().length,
                                InetAddress.getByName(ip), RTP_dest_port);
                        RTPsocket.send(senddp);
                    }
                }


                
                rtp_packet.printheader();
                if (table.getisConsuming()) {
                    RTPqueue.add(rtp_packet);
                }
            }
        } catch (InterruptedIOException iioe) {
            System.out.println("Nothing to read");
        } catch (IOException ioe) {
            System.out.println("Exception caught: " + ioe);
        }
        
    }

}
