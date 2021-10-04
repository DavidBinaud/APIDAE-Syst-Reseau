package ftp;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String url;
        String filename;


        //If pour mettre l'url à la main si besoin
        if(args.length >= 1 && args[0] != null){
            url = args[0];
        } else {
            url = "127.0.0.1";
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

                    Scanner sc = new Scanner(System.in);
                    System.out.print("Entrez le nom du fichier:");
                    filename = sc.nextLine();

                    //envoi du filename
                    out.println(filename);

                    //réponse à recevoir avec le contenu du fichier.

                    ArrayList<String> filecontent = new ArrayList<>();
                    filecontent.add(in.readLine());
                    while(in.ready()){
                        filecontent.add(in.readLine());
                    }

                    System.out.println("Voulez-vous télécharger le fichier? (y/n)");
                    if(sc.nextLine().equals("y")){
                        //Telechargement
                        FileSystem fs = FileSystems.getDefault();
                        Path path = fs.getPath(filename);
                        Files.deleteIfExists(path);
                        Files.createFile(path);
                        Files.write(path, filecontent);
                        System.out.println("File Written");
                    } else {
                        //Affichage
                        System.out.println("Start of file ----");
                        for (String line : filecontent) {
                            System.out.println(line);
                        }
                        System.out.println("EOF");
                    }


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
