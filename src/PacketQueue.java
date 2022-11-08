import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class PacketQueue {
    private LinkedList<Packet> queue;
    private ReentrantLock lock;
    private Condition tem_elem;

    public PacketQueue() {
        this.queue = new LinkedList<>();
        this.lock = new ReentrantLock();
        this.tem_elem = lock.newCondition();
    }

    public void add(Packet p){
        lock.lock();
        try{
            queue.add(p);
            tem_elem.signal();
        } finally {
            lock.unlock();
        }
    }

    public Packet remove() throws InterruptedException {
        lock.lock();
        try{
            while (queue.isEmpty()) tem_elem.await();
            return queue.remove();
        } finally {
            lock.unlock();
        }
    }
}
