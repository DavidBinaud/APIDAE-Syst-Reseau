package MultipleGames;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;

public class ServerApplicationRunnable implements Runnable {
    private Socket sockCli1;
    private Socket sockCli2;

    private BufferedReader inCli1;
    private BufferedReader inCli2;

    private PrintWriter outCli1;
    private PrintWriter outCli2;

    private PrintWriter outPlayer = this.outCli1;
    private BufferedReader inPlayer = this.inCli1;
    private PrintWriter outWaiter = this.outCli2;

    private String[][] grille;
    private ArrayList<String> pseudos;

    public ServerApplicationRunnable(Socket sockCli1, Socket sockCli2) {
        this.sockCli1 = sockCli1;
        this.sockCli2 = sockCli2;
        try {
            this.inCli1 = new BufferedReader(new InputStreamReader(this.sockCli1.getInputStream()));
            this.outCli1 = new PrintWriter(new OutputStreamWriter(this.sockCli1.getOutputStream()), true);

            this.inCli2 = new BufferedReader(new InputStreamReader(this.sockCli2.getInputStream()));
            this.outCli2 = new PrintWriter(new OutputStreamWriter(this.sockCli2.getOutputStream()), true);
        } catch (IOException ioEx){
            System.err.println(ioEx.toString());
        }

        this.outPlayer = this.outCli1;
        this.inPlayer = this.inCli1;
        this.outWaiter = this.outCli2;


        this.grille = new String[3][3];

        for (int i = 0; i < grille.length; i++) {
            for (int j = 0; j < grille.length; j++) {
                grille[i][j] = " ";
            }
        }

       this.pseudos = new ArrayList<>();
    }

    public void run() {
        try {

            //Validation Pseudo
            this.pseudos.add(inCli1.readLine());
            this.outCli1.println("103");
            this.pseudos.add(inCli2.readLine());
            this.outCli2.println("103");

            printGrille();

            this.outCli1.println("201");
            this.outCli2.println("202");

            gameLoop:  do{
                while(!verifPosition()){
                    outPlayer.println("204");
                }
                outPlayer.println("203");
                printGrille();

                switch(partieEnCours()){
                    case -1 :
                        //egalite
                        this.outPlayer.println("303");
                        this.outWaiter.println("303");
                        break gameLoop;
                    case 0 :
                        //On continue a jouer
                        break;
                    case 1 :
                        //Heureux gagnant
                        this.outPlayer.println("301");
                        this.outWaiter.println("302");
                        break gameLoop;

                }


                this.outPlayer.println("202");
                this.outWaiter.println("201");

                this.outPlayer = (this.outCli1 == this.outPlayer) ? this.outCli2 : this.outCli1;
                this.inPlayer = (this.inCli1 == this.inPlayer) ? this.inCli2 : this.inCli1;
                this.outWaiter = (this.outCli1 == this.outWaiter) ? this.outCli2 : this.outCli1;

                }while(true);

        } catch (IOException ex){
            System.err.println(ex.getMessage());
        } finally {
            try {
                if(!this.sockCli1.isClosed()){
                    this.sockCli1.close();
                }
            } catch (IOException ioEx){
                System.err.println(ioEx.getMessage());
            } catch (NullPointerException nullEx){
                System.err.println(nullEx.getMessage());
            }

            try {
                if(!this.sockCli2.isClosed()){
                    this.sockCli2.close();
                }
            } catch (IOException ioEx){
                System.err.println(ioEx.getMessage());
            } catch (NullPointerException nullEx){
                System.err.println(nullEx.getMessage());
            }
        }
    }

    private boolean verifPosition(){
        String pos = null;
        try {
            pos = this.inPlayer.readLine();
        } catch (IOException e) {
            return false;
        }
        int abs = Integer.parseInt(String.valueOf(pos.charAt(1)))  -1;
        switch(pos.charAt(0)){
            case 'A':
                if(this.grille[0][abs].compareTo(" ") !=0) return false;
                this.grille[0][abs] = String.valueOf(pos.charAt(3));
                break;
            case 'B':
                if(this.grille[1][abs].compareTo(" ") !=0) return false;
                this.grille[1][abs] = String.valueOf(pos.charAt(3));
                break;
            case 'C':
                if(this.grille[2][abs].compareTo(" ") !=0) return false;
                this.grille[2][abs] = String.valueOf(pos.charAt(3));
                break;
        }

        return true;
    }

    private void printGrille(){
        StringJoiner joinerFinal = new StringJoiner("\n","205\n    1   2   3\n","");
        for (int i = 0; i < 3; i++) {
            StringJoiner joinerRow = new StringJoiner(" | ",""," |");
            joinerRow.add(i == 0 ? "A" : (i == 1 ? "B" : "C"));
            for (int j = 0; j < 3; j++) {
                joinerRow.add(this.grille[i][j]);
            }
            joinerFinal.add(joinerRow.toString());

        }
        System.out.println(this.pseudos);
        System.out.println(joinerFinal.toString().substring(3));
        this.outCli1.println(joinerFinal.toString());
        this.outCli2.println(joinerFinal.toString());
    }

    private void verifPseudo(){

    }

    private int partieEnCours(){
        //verif ligne OK
        for (int ligne = 0; ligne < 3; ligne++) {
            if(!this.grille[ligne][0].equals(" ") && this.grille[ligne][0].equals(this.grille[ligne][1]) && this.grille[ligne][0].equals(this.grille[ligne][2])){
                return 1;
            }
        }
        //verfi colonne OK
        for (int colonne = 0; colonne < 3; colonne++) {
            if(!this.grille[0][colonne].equals(" ") && this.grille[0][colonne].equals(this.grille[1][colonne]) && this.grille[0][colonne].equals(this.grille[2][colonne])){
                return 1;
            }
        }

        //Verfi diagonale
        if(!this.grille[0][0].equals(" ") && this.grille[0][0].equals(this.grille[1][1]) && this.grille[0][0].equals(this.grille[2][2])){
            return 1;
        }
        if(!this.grille[2][0].equals(" ") &&this.grille[2][0].equals(this.grille[1][1]) && this.grille[2][0].equals(this.grille[0][2])){
            return 1;
        }

        //On continue a jouer
        for (int ligne = 0; ligne < 3; ligne++) {
            for (int colonne = 0; colonne < 3; colonne++) {
                if(this.grille[ligne][colonne].equals(" ")){
                    return 0;
                }
            }
        }

        //Egalité
        return -1;
    }
}