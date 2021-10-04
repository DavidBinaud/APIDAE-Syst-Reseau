package ftp;

import java.io.*;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ServerApplicationRunnable implements Runnable {
    private Socket sockcli;
    private int id;
    private String filename;

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

            this.filename = in.readLine();
            System.out.println("Client " + this.id + " : connected. file wanted: " + this.filename );

            //fichier à lire sur le système
            FileSystem fs = FileSystems.getDefault();
            Path filepath = fs.getPath(filename);
            if(Files.exists(filepath)){
                List<String> fileLines = Files.readAllLines(filepath);
                for (String line: fileLines) {
                    out.println(line);
                    System.out.println(line);
                }
            } else {
                out.println("Fichier qui n'existe pas");
            }

            System.out.println("Client " + this.id + " closed connection.");

            //On ferme la socket
            this.sockcli.close();
        } catch (IOException ex) {

        }

    }
}
