/* ------------------
   Cliente
   usage: java Cliente
   adaptado dos originais pela equipa docente de ESR (nenhumas garantias)
   colocar o cliente primeiro a correr que o servidor dispara logo!
   ---------------------- */

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

public class ClientDisplay implements Runnable {

  // GUI
  // ----
  private JFrame f = new JFrame("Cliente de Testes");
  private JButton setupButton = new JButton("Setup");
  private JButton playButton = new JButton("Play");
  private JButton pauseButton = new JButton("Pause");
  private JButton tearButton = new JButton("Teardown");
  private JPanel mainPanel = new JPanel();
  private JPanel buttonPanel = new JPanel();
  private JLabel iconLabel = new JLabel();
  private ImageIcon icon;

  // RTP variables:
  // ----------------
  DatagramPacket rcvdp; // UDP packet received from the server (to receive)
  DatagramSocket RTPsocket; // socket to be used to send and receive UDP packet
  static int RTP_RCV_PORT = 25000; // port where the client will receive the RTP packets

  private Timer cTimer; // timer used to receive data from the UDP socket
  byte[] cBuf; // buffer used to store data received from the server

  // Our variables
  private AddressingTable table;
  private RTPpacketQueue RTPqueue;
  private PacketQueue TCPqueue;
  private String ip;

  // --------------------------
  // Constructor
  // --------------------------

  // Fazer uma thread pra reecber isto <------
  public ClientDisplay(AddressingTable table, RTPpacketQueue RTPqueue, PacketQueue TCPqueue, String ip) {
    this.table = table;
    this.RTPqueue = RTPqueue;
    this.TCPqueue = TCPqueue;
    this.ip = ip;
  }

  // ------------------------------------
  // Handler for buttons
  // ------------------------------------

  // Handler for Play button
  // -----------------------
  class playButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {

      System.out.println("Play Button pressed !");
      TCPqueue.add(new Packet(table.getSender(), ip, 3,
          "".getBytes(StandardCharsets.UTF_8)));
      table.setisConsuming(true);
      // start the timers ...
      cTimer.start();
    }
  }

  // Handler for tear button
  // -----------------------
  class tearButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {

      System.out.println("Teardown Button pressed !");
      table.turnOff(ip);
      // stop the timer
      cTimer.stop();

      f.dispose();

      table.setisConsuming(false);
      if (!table.isStreaming()) {
        TCPqueue.add(new Packet(table.getSender(), ip, 4, "".getBytes(StandardCharsets.UTF_8)));
      }
      // exit
      System.exit(0);
    }
  }
  // ------------------------------------
  // Handler for timer (para cliente)
  // ------------------------------------

  class clientTimerListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {

      // Construct a DatagramPacket to receive data from the UDP socket
      // rcvdp = new DatagramPacket(cBuf, cBuf.length);

      try {
        // create an RTPpacket object from the DP
        RTPpacket rtp_packet = RTPqueue.remove();
        // RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());

        // print important header fields of the RTP packet received:
        System.out.println("Got RTP packet with SeqNum # " + rtp_packet.getsequencenumber() + " TimeStamp "
            + rtp_packet.gettimestamp() + " ms, of type " + rtp_packet.getpayloadtype());

        // print header bitstream:
        rtp_packet.printheader();

        // get the payload bitstream from the RTPpacket object
        int payload_length = rtp_packet.getpayload_length();
        byte[] payload = new byte[payload_length];
        rtp_packet.getpayload(payload);

        // get an Image object from the payload bitstream
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.createImage(payload, 0, payload_length);

        // display the image as an ImageIcon object
        icon = new ImageIcon(image);
        iconLabel.setIcon(icon);
      } catch (InterruptedException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }
  }

  @Override
  public void run() {
    // build GUI
    // --------------------------

    // Frame
    f.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    // Buttons
    buttonPanel.setLayout(new GridLayout(1, 0));
    // buttonPanel.add(setupButton);
    buttonPanel.add(playButton);
    // buttonPanel.add(pauseButton);
    buttonPanel.add(tearButton);

    // handlers... (so dois)
    playButton.addActionListener(new playButtonListener());
    tearButton.addActionListener(new tearButtonListener());

    // Image display label
    iconLabel.setIcon(null);

    // frame layout
    mainPanel.setLayout(null);
    mainPanel.add(iconLabel);
    mainPanel.add(buttonPanel);
    iconLabel.setBounds(0, 0, 380, 280);
    buttonPanel.setBounds(0, 280, 380, 50);

    f.getContentPane().add(mainPanel, BorderLayout.CENTER);
    f.setSize(new Dimension(390, 370));
    f.setVisible(true);

    // init para a parte do cliente
    // --------------------------
    cTimer = new Timer(20, new clientTimerListener());
    cTimer.setInitialDelay(0);
    cTimer.setCoalesce(true);
    cBuf = new byte[15000]; // allocate enough memory for the buffer used to receive data from the server

    /*
     * try {
     * // socket e video
     * RTPsocket = new DatagramSocket(RTP_RCV_PORT); // init RTP socket (o mesmo
     * para o cliente e servidor)
     * RTPsocket.setSoTimeout(5000); // setimeout to 5s
     * } catch (SocketException e) {
     * System.out.println("Cliente: erro no socket: " + e.getMessage());
     * }
     */
  }

}// end of Class Cliente
