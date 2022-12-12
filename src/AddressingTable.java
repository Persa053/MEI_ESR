import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class AddressingTable {

    private String toServer;
    private String toNetwork;
    private Boolean isConsuming;

    private int hops;
    private Duration latency;

    // (IP vizinhos/adjacentes, True se querem consumir a Stream)
    private Map<String, Boolean> streamingTable;
    private final ReentrantLock lock;

    public Duration getLatency() {
        lock.lock();
        try {
            return latency;
        } finally {
            lock.unlock();
        }

    }

    public void setLatency(Duration latency) {
        lock.lock();
        try {
            this.latency = latency;
        } finally {
            lock.unlock();
        }

    }

    public int getHops() {
        lock.lock();
        try {
            return hops;
        } finally {
            lock.unlock();
        }
    }

    public void setHops(int hops) {
        lock.lock();
        try {
            this.hops = hops;
        } finally {
            lock.unlock();
        }
    }

    public String getToNetwork() {
        lock.lock();
        try {
            return this.toNetwork;
        } finally {
            lock.unlock();
        }
    }

    public void setToNetwork(String toNetwork) {
        lock.lock();
        try {
            this.toNetwork = toNetwork;
        } finally {
            lock.unlock();
        }
    }

    public String getToServer() {
        lock.lock();
        try {
            return this.toServer;
        } finally {
            lock.unlock();
        }
    }

    public void setToServer(String toServer) {
        lock.lock();
        try {
            this.toServer = toServer;
        } finally {
            lock.unlock();
        }
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

    public Boolean getEstado(String ip) {
        lock.lock();
        try {
            return this.streamingTable.get(ip);
        } finally {
            lock.unlock();
        }
    }

    public void setEstado(String ip, Boolean estado) {
        lock.lock();
        try {
            this.streamingTable.put(ip, estado);
        } finally {
            lock.unlock();
        }
    }

    public AddressingTable(Set<String> neighbours) {
        this.lock = new ReentrantLock();
        this.isConsuming = false;
        this.streamingTable = new HashMap<>();

        this.hops = Integer.MAX_VALUE;
        this.latency = Duration.ofMillis(Integer.MAX_VALUE);

        for (String n : neighbours)
            this.streamingTable.put(n, false);

    }
}
