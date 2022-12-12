import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class AddressingTable {

    private String toServer;
    private String toNetwork;
    private Boolean isConsuming;

    // (IP vizinhos/adjacentes, True se querem consumir a Stream)
    private Map<String, Boolean> streamingTable;
    private final ReentrantLock lock;

    public String getToNetwork() {
        return toNetwork;
    }

    public void setToNetwork(String toNetwork) {
        this.toNetwork = toNetwork;
    }

    public String getToServer() {
        return toServer;
    }

    public void setToServer(String toServer) {
        this.toServer = toServer;
    }

    public Boolean getisConsuming() {
        lock.lock();
        try {
            return isConsuming;
        } finally {
            lock.unlock();
        }
    }

    public void setisConsuming(Boolean isConsuming) {
        lock.lock();
        try {
            this.isConsuming = isConsuming;
        } finally {
            lock.unlock();
        }
    }

    public void turnOn(String ip) {
        lock.lock();
        try {
            streamingTable.put(ip, true);
        } finally {
            lock.unlock();
        }
    }

    public void turnOff(String ip) {
        lock.lock();
        try {
            streamingTable.put(ip, false);
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getVizinhos() {
        lock.lock();
        try {
            return this.streamingTable.keySet();
        } finally {
            lock.unlock();
        }

    }

    public Map<String, Boolean> getStreamingTable() {
        lock.lock();
        try {
            return this.streamingTable;
        } finally {
            lock.unlock();
        }
    }

    // se estamos a transmitir a stream para algum vizinho
    public Boolean isStreaming() {
        return this.streamingTable.values().stream().collect(Collectors.toSet()).contains(true) || this.isConsuming;
    }

    public AddressingTable(Set<String> neighbours) {
        this.lock = new ReentrantLock();
        this.isConsuming = false;
        this.streamingTable = new HashMap<>();

        for (String n : neighbours)
            this.streamingTable.put(n, false);

    }
}
