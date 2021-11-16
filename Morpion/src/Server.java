import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.*;
import java.util.StringJoiner;

public class Server {
    
    public static void main(String[] args) throws Exception {
        String[][] grille = new String[3][3];
        for (int i = 0; i < grille.length; i++) {
            for (int j = 0; j < grille.length; j++) {
                grille[i][j] = " ";
            }
        }
        
        ArrayList<String> pseudos = new ArrayList<>();
        ServerSocket sockserv;
        BufferedReader inCli1;
        BufferedReader inCli2;
        PrintWriter outCli1;
        PrintWriter outCli2;
        
        try {
            sockserv = new ServerSocket(1234);
            try {
                
                boolean partieEnCours = true;
                
                Socket sockCli1 = sockserv.accept();
                inCli1 = new BufferedReader(new InputStreamReader(sockCli1.getInputStream()));
                outCli1 = new PrintWriter(new OutputStreamWriter(sockCli1.getOutputStream()), true);
                
                
                Socket sockCli2 = sockserv.accept();
                inCli2 = new BufferedReader(new InputStreamReader(sockCli2.getInputStream()));
                outCli2 = new PrintWriter(new OutputStreamWriter(sockCli2.getOutputStream()), true);
                
                //Validation Pseudo
                pseudos.add(inCli1.readLine());
                outCli1.println("103");
                pseudos.add(inCli2.readLine());
                outCli2.println("103");
                
                printGrille(outCli1, outCli2, grille);
                
                outCli1.println("201");
                outCli2.println("202");
                PrintWriter outPlayer = outCli1;
                BufferedReader inPlayer = inCli1;
                PrintWriter outWaiter = outCli2;
                gameLoop:  do{
é                    while(!verifPosition(inPlayer.readLine(),grille)){
                        outPlayer.println("204");
                    }
                    outPlayer.println("203");
                    printGrille(outCli1, outCli2, grille);
                    
                    switch(partieEnCours(grille)){
                        case -1 :
                        //egalite
                        outPlayer.println("303");
                        outWaiter.println("303");
                        break gameLoop;
                        case 0 :
                        //On continue a jouer
                        break;
                        case 1 :
                        //Heureux gagnant
                        outPlayer.println("301");
                        outWaiter.println("302");
                        break gameLoop;
                        
                    }
                    
                    
                    outPlayer.println("202");
                    outWaiter.println("201");
                    
                    outPlayer = (outCli1 == outPlayer) ? outCli2 : outCli1;
                    inPlayer = (inCli1 == inPlayer) ? inCli2 : inCli1;
                    outWaiter = (outCli1 == outWaiter) ? outCli2 : outCli1;
                    
                }while(true);
                
                
                
            }
            catch (IOException ex){
                
            }
            
        }
        catch(IOException ioEx){
            
        }
    }
    
    private static boolean verifPosition(String pos, String[][] grille){
        int abs = Integer.parseInt(String.valueOf(pos.charAt(1)))  -1;
        System.err.println(pos + " " + abs);
        switch(pos.charAt(0)){
            case 'A':
            if(grille[0][abs].compareTo(" ") !=0) return false;
            grille[0][abs] = String.valueOf(pos.charAt(3));
            break;
            case 'B':
            if(grille[1][abs].compareTo(" ") !=0) return false;
            grille[1][abs] = String.valueOf(pos.charAt(3));
            break;
            case 'C':
            if(grille[2][abs].compareTo(" ") !=0) return false;
            grille[2][abs] = String.valueOf(pos.charAt(3));
            break;
        }
        
        return true;
    }
    
    private static void printGrille(PrintWriter outCli1, PrintWriter outCli2, String[][] grille){
        StringJoiner joinerFinal = new StringJoiner("\n","205\n    1   2   3\n","");
        for (int i = 0; i < 3; i++) {
            StringJoiner joinerRow = new StringJoiner(" | ",""," |");
            joinerRow.add(i == 0 ? "A" : (i == 1 ? "B" : "C"));
            for (int j = 0; j < 3; j++) {
                joinerRow.add(grille[i][j]);
            }
            joinerFinal.add(joinerRow.toString());
            
        }
        System.out.println(joinerFinal.toString());
        outCli1.println(joinerFinal.toString());
        outCli2.println(joinerFinal.toString());
    }
    
    private void verifPseudo(){
        
    }
    
    private static int partieEnCours(String[][] grille){
        //verif ligne OK
        for (int ligne = 0; ligne < 3; ligne++) {
            if(!grille[ligne][0].equals(" ") && grille[ligne][0].equals(grille[ligne][1]) && grille[ligne][0].equals(grille[ligne][2])){
                return 1;
            }
        }
        //verfi colonne OK
        for (int colonne = 0; colonne < 3; colonne++) {
            if(!grille[0][colonne].equals(" ") && grille[0][colonne].equals(grille[1][colonne]) && grille[0][colonne].equals(grille[2][colonne])){
                return 1;
            }
        }
        
        //Verfi diagonale
        if(!grille[0][0].equals(" ") && grille[0][0].equals(grille[1][1]) && grille[0][0].equals(grille[2][2])){
            return 1;
        }
        if(!grille[2][0].equals(" ") &&grille[2][0].equals(grille[1][1]) && grille[2][0].equals(grille[0][2])){
            return 1;
        }
        
        //On continue a jouer
        for (int ligne = 0; ligne < 3; ligne++) {
            for (int colonne = 0; colonne < 3; colonne++) {
                if(grille[ligne][colonne].equals(" ")){
                    return 0;
                }
            }
        }
        
        //Egalité
        return -1;
    }
}
