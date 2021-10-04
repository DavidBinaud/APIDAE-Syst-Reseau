import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Application implements Runnable{

    private int id;

    public Application(int id) {
        this.id = id;
    }

    public void run() {
        for (int i = 0; i < 20; i++){
            System.out.println("Thread " + this.id + ": " + i);
        }
    }

    public static void main(String[] args) {
        ExecutorService es = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 9; i++) {
            es.execute(new Application(i));
        }
        es.shutdown();
    }
}
