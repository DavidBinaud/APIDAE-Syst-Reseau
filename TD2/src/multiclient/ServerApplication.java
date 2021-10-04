package multiclient;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApplication {
    public static void main(String[] args) {
        ServerSocket sockserv = null;

        try {
            //On instancie le socket serveur sur le port 1234
            sockserv = new ServerSocket(1234);
            int clientid = 0;
            while(true) {
                try {
                    //On instancie le socket et les streams
                    Socket sockcli = sockserv.accept();
                    ServerApplicationRunnable p = new ServerApplicationRunnable(sockcli, clientid);
                    new Thread(p).start();
                    clientid++;
                } catch (IOException ex){

                }
            }
        } catch (IOException ex){

        } finally {
            try {
                sockserv.close();
            } catch (IOException ex) {

            }
        }
    }
}
