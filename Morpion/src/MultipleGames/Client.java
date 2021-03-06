package MultipleGames;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @authors Ylona Fabiani - Elie Roure - David Binaud
 */
public class Client {
    private static Pattern pattern;
    private static Matcher matcher;
    
    public static void main(String[] args) throws Exception {
        Socket socketCli;
        try {
            socketCli = new Socket("127.0.0.1", 1234);
        } catch (IOException ioEx) {
            System.out.println("Connexion échoué : " + ioEx.getMessage());
            return;
        }
        String role;
        String code;
        String grille;
        boolean partieEnCours = true;

        try {
            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(socketCli.getInputStream()));
            PrintWriter out;
            out = new PrintWriter(new OutputStreamWriter(socketCli.getOutputStream()), true);

            if (socketCli.isConnected()) {
                System.out.println("Vous etes connecté");

                System.out.println("Saisir votre pseudonyme :");
                Scanner sc = new Scanner(System.in);

                String pseudo = sc.nextLine();
                out.println(pseudo);
                while (!in.readLine().contains("103")) {
                    System.out.println("Pseusonyme invalide, veuillez saisir un autre pseudonyme :");
                    pseudo = sc.nextLine();
                    out.println(pseudo);
                }

                //On affiche la grille vierge
                code = in.readLine();
                if (code.contains("205")) {
                    printGrid(in);
                }

                //attribut les roles
                code = in.readLine();
                role = code.contains("201") ? "O" : "X";
                System.out.println("Vous jouez avec les " + role);
                //on lance le premier tour

                do{
                    //traitement switch
                    switch(code.substring(0,3)){
                        case "201":
                        System.out.println("Saisir votre coup : ");
                        CheckMove(in,out,sc,role);

                        code = in.readLine();
                        while(code.contains("204")){
                            System.out.println("Coup invalide, la case n'est pas vide : ");
                            CheckMove(in,out,sc,role);
                            code = in.readLine();
                        }
                        //grille
                        code = in.readLine();
                        if (code.contains("205")) {
                            printGrid(in);
                        }
                        break;

                        case "202":
                        System.out.println("Coup de l'adversaire, Veuillez patienter");
                        //grille
                        code = in.readLine();
                        if (code.contains("205")) {
                            printGrid(in);
                        }
                        break;

                        case "301":
                        System.out.println("Vous avez gagné!");
                        partieEnCours = false;
                        return;
                        case "302":
                        System.out.println("vous avez perdu!");
                        partieEnCours = false;
                        return;
                        case "303":
                        System.out.println("Match nul.");
                        partieEnCours = false;
                        return;
                    }

                    code = in.readLine();
                }
                while(partieEnCours);


            }
        } catch (IOException ioEx){
            System.err.println(ioEx.getMessage());
        } catch(NullPointerException nullEx){
            System.err.println("Adversaire deconnecté");
        } finally {
            try {
                if(!socketCli.isClosed()){
                    socketCli.close();
                }
            } catch (IOException ioEx){
                System.err.println(ioEx.getMessage());
            }
        }

    }

    /**
     * Will check the format of the move
     * @param saisie
     * @return true if the format is correct, else false
     */
    public static boolean checkMoveFormat(String saisie) {
        pattern = Pattern.compile("^[ABC][123]$");
        matcher = pattern.matcher(saisie);
        if (matcher.find()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Will send the move to the server
     * @param in
     * @param out
     * @param sc
     * @param role
     */
    public static void CheckMove(BufferedReader in, PrintWriter out, Scanner sc, String role) {
        String coup = sc.nextLine();
        while (!checkMoveFormat(coup)) {
            System.out.println("Saisir votre coup dans le format [ABC][123] : ");
            coup = sc.nextLine();
        }
        out.println(coup);
    }

    /**
     * Will format the grid received from the server and will output it
     * @param in
     * @throws IOException
     */
    private static void printGrid(BufferedReader in) throws IOException{
        StringJoiner st = new StringJoiner("\n");
        for (int i = 0; i < 4; i++) {
            st.add(in.readLine());
        }
        System.out.println(st.toString());
    }
}
// 