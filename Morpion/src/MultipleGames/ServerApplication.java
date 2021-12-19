package MultipleGames;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @authors Ylona Fabiani - Elie Roure - David Binaud
 */
public class ServerApplication {
    public static void main(String[] args) {
        ServerSocket sockserv = null;
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        try {
            //On instancie le socket serveur sur le port 1234
            sockserv = new ServerSocket(1234);
            while(true) {
                try {
                    //On instancie le socket et les streams
                    Socket sockcli1 = sockserv.accept();
                    Socket sockcli2 = sockserv.accept();
                    ServerApplicationRunnable p = new ServerApplicationRunnable(sockcli1, sockcli2);
                    executor.execute(p);
                } catch (IOException ex){

                }
            }
        } catch (IOException ex){

        } finally {
            try {
                sockserv.close();
            } catch (IOException ex) {

            }
            executor.shutdown();
        }
    }
}
