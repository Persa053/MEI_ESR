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

    private String prev_sender;

    private Map<String, Duration> latencies;
    private Map<String, Integer> hops;

    // Controlo de flooding para evitar loops infinitos
    // ServerIP, (Vizinho, True se ja reencaminhamos)

    // (IP vizinhos/adjacentes, True se querem consumir a Stream)
    private Map<String, Boolean> streamingTable;
    private final ReentrantLock lock;

    public Duration getLatency(String ip) {
        lock.lock();
        try {
            return this.latencies.get(ip);
        } finally {
            lock.unlock();
        }

    }

    public void setLatency(String ip, Duration latency) {
        lock.lock();
        try {
            this.latencies.put(ip, latency);
        } finally {
            lock.unlock();
        }

    }

    public int getHops(String ip) {
        lock.lock();
        try {
            return this.hops.get(ip);
        } finally {
            lock.unlock();
        }
    }

    public void setHops(String ip, int hops) {
        lock.lock();
        try {
            this.hops.put(ip, hops);
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

    public Set<String> getVizinhosClone() {
        lock.lock();
        try {
            return this.streamingTable.keySet().stream().map(String::new).collect(Collectors.toSet());
        } finally {
            lock.unlock();
        }

    }

    public Map<String, Duration> getLatencies() {
        lock.lock();
        try {
            return this.latencies;
        } finally {
            lock.unlock();
        }
    }

    public Map<String, Integer> getHopsTable() {
        lock.lock();
        try {
            return this.hops;
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

    public void UpdateRoute(String sender, int hops, Duration timeElapsed) {

        int best_route_hops;
        Duration best_route_duration;
        Duration one_secs = Duration.ofMillis(1000);

        best_route_hops = this.getHops(this.toServer);
        best_route_duration = this.getLatency(this.toServer);

        Duration limite_inferior = best_route_duration.minus(one_secs);
        Duration limite_superior = best_route_duration.plus(one_secs);

        int val_inferior = limite_inferior.compareTo(timeElapsed);
        int val_superior = limite_superior.compareTo(timeElapsed);
        if (val_inferior > 0) {
            this.setToServer(sender);
            System.out.println("Route mais rápida encontrada");
        } else if (val_superior > 0 && best_route_hops > hops) {
            System.out.println("prev_best_route = " + this.getToServer() + "   best_route_hops = " + best_route_hops
                    + " sender hops = " + hops);
            // 10.0.2.1 hops = 2 hops = 2
            this.setToServer(sender);
            System.out.println("Diferença de tempo < 1 sec, mas menos hops");
        } else {
            System.out.println("Não é mais rápida");
        }

    }

    public void UpdateRoutingTable(String sender, int hops, Duration timeElapsed) {

        String best_route_ip;

        best_route_ip = this.getToServer();

        if (best_route_ip == null) {
            this.setToServer(sender);
            this.setLatency(sender, timeElapsed);
            this.setHops(sender, hops);
            System.out.println("First route to Server found");

        } else if (!sender.equals(best_route_ip)) {
            UpdateRoute(sender, hops, timeElapsed);
        } else if (sender.equals(best_route_ip)) {
            for (String ip : this.getVizinhosClone()) {
                UpdateRoute(ip, this.getHops(ip), this.getLatency(ip));
            }
        }
    }

    public AddressingTable(Set<String> neighbours) {
        this.lock = new ReentrantLock();
        this.isConsuming = false;
        this.streamingTable = new HashMap<>();
        this.hops = new HashMap<>();
        this.latencies = new HashMap<>();

        this.toServer = null;

        this.prev_sender = null;

        for (String n : neighbours) {
            this.streamingTable.put(n, false);
            this.hops.put(n, Integer.MAX_VALUE);
            this.latencies.put(n, Duration.ofMillis(Integer.MAX_VALUE));
        }

    }

}
