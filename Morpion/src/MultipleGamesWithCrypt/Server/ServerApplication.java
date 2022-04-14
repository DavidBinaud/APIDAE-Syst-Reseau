package MultipleGamesWithCrypt.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A class representing the server
 * it's the entry point for the client as it acts as a dispatcher
 * @authors Ylona Fabiani - Elie Roure - David Binaud
 */
public class ServerApplication {

    /**
     * This is the max number of games simultaneously going on
     * it restrict the amount of ressources that can be created by the server to sustain clients demand
     */
    static int MAX_NUMBER_OF_SIMULTANEOUS_GAMES = 5;

    /**
     * Main function, it will setup the server and receive any demands from clients and when two clients are connected
     * will dispatch them to a game in a new thread
     * @param args
     */
    public static void main(String[] args) {
        ServerSocket sockserv = null;
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_NUMBER_OF_SIMULTANEOUS_GAMES);
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
                } catch (Exception ex){

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
