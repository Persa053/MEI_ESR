public class SenderUDP implements Runnable {

    private String filename;
    private AddressingTable table;

    public SenderUDP(String filename, AddressingTable table) {
        this.filename = filename;
        this.table = table;
    }

    @Override
    public void run() {
        Stream.execute(filename, table);
    }

}
