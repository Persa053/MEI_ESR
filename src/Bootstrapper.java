import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Bootstrapper {
    private Map<String, Nodo> bottstraper;
    private ReentrantLock lock;

    static class Nodo {
        private Map<String, String> ips;
        private boolean visitado;

        Nodo(Map<String, String> ips) {
            this.ips = ips;
            this.visitado = false;
        }

        public Map<String, String> getIps(){
            return ips;
        }

        public void setIps(Map<String, String> ips){
             this.ips = ips;
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
            


            // Split the input string using the "," delimiter

            String ip;
            while (sc.hasNextLine()) {

                arr = sc.nextLine().split(";");
                String[] keyValue = arr[0].split("=");
                ip = keyValue[0];
                Map<String, String> viz = new HashMap<>();

                // Iterate over the substrings and add them to the Map
                for (String substring : arr) {
                    // Split each substring using the "=" delimiter
                    keyValue = substring.split("=");
                    // Add the key and value to the Map
                    viz.put(keyValue[0], keyValue[1]);
                }

                    this.bottstraper.put(ip, new Nodo(viz));
            }


        }
        sc.close();
    }

    public String getVizinhos(String ip) {
        lock.lock();
        try {
            Set<String> vizinhos = this.bottstraper.get(ip).getIps().keySet();
            this.bottstraper.get(ip).setVisitado(true);
            Iterator<String> it = vizinhos.iterator();
            StringBuilder res = new StringBuilder();

            while (it.hasNext()) {
                String a = it.next();
                res.append(a).append("=");
                res.append(bottstraper.get(ip).getIps().get(a));
                res.append(";");
            }
            return res.toString();
        } finally {
            lock.unlock();
        }


    }

}
