import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Bootstrapper {
    private Map<String, Nodo> bottstraper;
    private ReentrantLock lock;

    static class Nodo {
        private Set<String> vizinhos;
        private boolean visitado;

        Nodo(Set<String> viz) {
            this.vizinhos = viz;
            this.visitado = false;
        }

        public Set<String> getVizinhos() {
            return vizinhos;
        }

        public void setVizinhos(Set<String> vizinhos) {
            this.vizinhos = vizinhos;
        }

        public boolean isVisitado() {
            return visitado;
        }

        public void setVisitado(boolean visitado) {
            this.visitado = visitado;
        }
    }

    Bootstrapper(String path) throws FileNotFoundException {
        this.bottstraper = new HashMap<>();
        this.lock = new ReentrantLock();

        File file = new File(path);
        Scanner sc = new Scanner(file);

        String[] arr;

        while (sc.hasNextLine()) {

            arr = sc.nextLine().split("-");
            for (int i = 0; i < arr.length; i++) {

                System.out.println(arr[i]);
            }

            Set<String> viz = new TreeSet<>(List.of(arr[1].split(";")));
            bottstraper.put(arr[0], new Nodo(viz));
        }
    }

    public String getVizinhos(String ip) {
        lock.lock();
        try {
            Set<String> vizinhos = bottstraper.get(ip).getVizinhos();
            bottstraper.get(ip).setVisitado(true);
            Iterator<String> it = vizinhos.iterator();
            StringBuilder res = new StringBuilder();

            while (it.hasNext()) {
                res.append(it.next());
                if (it.hasNext())
                    res.append(";");
            }
            return res.toString();
        } finally {
            lock.unlock();
        }

    }

}
