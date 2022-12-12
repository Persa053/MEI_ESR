import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import static java.nio.ByteBuffer.*;

import java.io.DataInputStream;
import java.io.IOException;

public class Packet {
    private String dest; // ip destino
    private String origem; // ip origem
    private int tipo; // tipo de pacote
    private byte[] dados; // payload

    // Construtores
    public Packet(String dest, String origem, int tipo, byte[] dados) {
        this.dest = dest;
        this.origem = origem;
        this.tipo = tipo;
        this.dados = dados;
    }

    // Criar novo packet com array de bytes (kinda desserialize)
    public Packet(byte[] content) throws UnknownHostException {
        byte[] aux = new byte[4]; // int 4 bytes
        int posicao_atual = 0;

        this.tipo = wrap(content, posicao_atual, 4).getInt();
        posicao_atual += 4;

        System.arraycopy(content, posicao_atual, aux, 0, 4);
        this.origem = InetAddress.getByAddress(aux).getHostAddress();
        posicao_atual += 4;

        System.arraycopy(content, posicao_atual, aux, 0, 4);
        this.dest = InetAddress.getByAddress(aux).getHostAddress();
        posicao_atual += 4;

        if (content.length == posicao_atual) {
            this.dados = null;
        } else {
            byte[] aux2 = new byte[content.length - posicao_atual];
            System.arraycopy(content, posicao_atual, aux2, 0, content.length - posicao_atual);
            this.dados = aux2;
        }
    }

    public static Packet deserialize(DataInputStream in) throws IOException {
        byte[] arr = new byte[4096];
        int size = in.read(arr, 0, 4096);
        byte[] content = new byte[size];
        System.arraycopy(arr, 0, content, 0, size);

        Packet p = new Packet(content);
        return p;
    }

    // getters
    public String getDest() {
        return dest;
    }

    public String getOrigem() {
        return origem;
    }

    public int getTipo() {
        return tipo;
    }

    public byte[] getDados() {
        return dados;
    }

    public void print_packet() {
        switch (this.tipo) {
            case 3:
                System.out.println("3 -> Pedido Stream ");
                break;
            case 4:
                System.out.println("4 -> Parar Stream ");
                break;
            default:
                System.out.println("Outro pacote");
                break;
        }

    }

    // serializar o packet

    public byte[] serialize() throws UnknownHostException {
        byte[] content;

        if (this.dados != null)
            content = new byte[12 + this.dados.length];
        else
            content = new byte[12];

        int posicao = 0;

        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(tipo);
        System.arraycopy(byteBuffer.array(), 0, content, posicao, 4);
        posicao += 4;

        byte[] source = InetAddress.getByName(this.origem).getAddress();
        System.arraycopy(source, 0, content, posicao, 4);
        posicao += 4;

        byte[] destination = InetAddress.getByName(this.dest).getAddress();
        System.arraycopy(destination, 0, content, posicao, 4);
        posicao += 4;

        if (dados != null)
            System.arraycopy(this.dados, 0, content, posicao, this.dados.length);

        return content;
    }

}
