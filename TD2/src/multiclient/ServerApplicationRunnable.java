package multiclient;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ServerApplicationRunnable implements Runnable {
    private Socket sockcli;
    private int id;
    private String name;

    public ServerApplicationRunnable(Socket sockcli, int clientid) {
        this.sockcli = sockcli;
        this.id = clientid;
    }

    public void run() {
        BufferedReader in;
        PrintWriter out;
        String message;

        try {
            in = new BufferedReader(new InputStreamReader(this.sockcli.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(this.sockcli.getOutputStream()), true);

            this.name = in.readLine();
            System.out.println("multiclient.Client " + this.id + " : " + this.name + " connected");


            do {
                message = in.readLine();
                //On affiche le message reçu
                if (message != null){
                    System.out.println("multiclient.Client " + this.id + ": " + name + ", message reçu:" + message);
                }

                //On envoie un accusé de reception
                out.println(message);
            } while (message != null && !message.equals("stop"));

            System.out.println(this.name + " closed connection.");

            //On ferme la socket
            this.sockcli.close();
        } catch (IOException ex) {

        }

    }
}
