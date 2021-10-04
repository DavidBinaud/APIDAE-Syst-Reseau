package multiclient;

import java.net.Socket;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String url;
        String name;


        //If pour mettre l'url à la main si besoin
        if(args.length >= 1 && args[0] != null){
            url = args[0];
        } else {
            url = "127.0.0.1";
        }

        if(args.length >= 2 && args[1] != null){
            name = args[1];
        } else {
            name = "Random";
        }

        System.out.println(url);

        Socket sock = null;
        BufferedReader in;
        PrintWriter out;
        String message;


        try {
            sock = new Socket(url ,1234);
            while(true) {
                try {
                    //On instancie les variables
                    in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()), true);

                    //envoi du nom
                    out.println(name);

                    Scanner sc = new Scanner(System.in);
                    String line;

                    do{
                        //On demande un input pour le message
                        System.out.print(":");
                        line = sc.nextLine();

                        //On affiche le message
                        System.out.println("message à envoyer:" + line);

                        //On envoi le message
                        out.println(line);

                        System.out.println("message envoyé");
                        //ON reçoit la réponse
                        message = in.readLine();
                        System.out.println("Vu: (" + message + ")\n");

                    } while (!line.equals("stop"));

                    System.out.println("Closing");
                    //ON ferme la socket
                    sock.close();
                } catch (IOException ex){

                }
            }
        } catch (IOException ex){

        } finally {
            try {
                sock.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
