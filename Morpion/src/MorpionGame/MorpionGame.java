package MorpionGame;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @authors Ylona Fabiani - Elie Roure - David Binaud
 */
public class MorpionGame {

    private String[][] grid;

    private ArrayList<Player> players;

    private Player playingPlayer;
    private Player waitingPlayer;

    private static Pattern pattern;
    private static Matcher matcher;

    public MorpionGame(Socket socket1, Socket socket2) {
        this.grid = new String[3][3];
        this.setUpGrid();

        this.players = new ArrayList<>();
        Player player1 = new Player(socket1);
        this.players.add(player1);

        Player player2 = new Player(socket2);
        this.players.add(player2);
        this.chooseFirstPlayer(player1, player2);
    }

    private void chooseFirstPlayer(Player p1, Player p2){
        Random r = new Random();
        if(r.nextInt(2) == 0){
            this.playingPlayer = p1;
            this.waitingPlayer = p2;
            p1.setRole("O");
            p2.setRole("X");
        } else {
            this.playingPlayer = p2;
            this.waitingPlayer = p1;
            p1.setRole("X");
            p2.setRole("O");
        }
    }

    public void play(){
        try {
            this.askPseudos();
            this.printGrid();

            this.playingPlayer.getOutCli().println("201");
            this.waitingPlayer.getOutCli().println("202");
            try {
                do{
                    gameLoop:
                    do {
                        while (!checkPos()) {
                            this.playingPlayer.getOutCli().println("204");
                        }
                        this.playingPlayer.getOutCli().println("203");
                        this.printGrid();

                        switch (this.isGamePlaying()) {
                            case -1:
                                //egalite
                                this.playingPlayer.getOutCli().println("303");
                                this.waitingPlayer.getOutCli().println("303");
                                break gameLoop;
                            case 0:
                                //On continue a jouer
                                break;
                            case 1:
                                //Heureux gagnant
                                this.playingPlayer.getOutCli().println("301");
                                this.waitingPlayer.getOutCli().println("302");
                                break gameLoop;

                        }


                        this.playingPlayer.getOutCli().println("202");
                        this.waitingPlayer.getOutCli().println("201");

                        this.switchPlayersTurn();

                    } while (true);

                }while(this.askPlayAgain());
            }
            catch(NullPointerException NPEx){
                System.out.println("Joueur deco");
            }

        } catch (IOException ex){
            System.err.println(ex.getMessage());
        } finally {
            try {
                if(!this.playingPlayer.getSockCli().isClosed()){
                    this.playingPlayer.getSockCli().close();
                }
            } catch (IOException ioEx){
                System.err.println(ioEx.getMessage());
            } catch (NullPointerException nullEx){
                System.err.println(nullEx.getMessage());
            }

            try {
                if(!this.waitingPlayer.getSockCli().isClosed()){
                    this.waitingPlayer.getSockCli().close();
                }
            } catch (IOException ioEx){
                System.err.println(ioEx.getMessage());
            } catch (NullPointerException nullEx){
                System.err.println(nullEx.getMessage());
            }
        }
    }

    private void askPseudos() throws IOException{
        try {
            this.playingPlayer.setPseudo(this.playingPlayer.getInCli().readLine());
            this.playingPlayer.getOutCli().println("103");
            this.waitingPlayer.setPseudo(this.waitingPlayer.getInCli().readLine());
            this.waitingPlayer.getOutCli().println("103");
        } catch (IOException ex){
            System.err.println(this.playingPlayer.getPseudo() + " " + this.waitingPlayer.getPseudo());
            System.err.println(ex.getMessage());
        }
    }

    public void switchPlayersTurn(){
        Player tmp = this.playingPlayer;
        this.playingPlayer = this.waitingPlayer;
        this.waitingPlayer = tmp;
    }

    private boolean checkPos(){
        String pos = null;
        try {
            pos = this.playingPlayer.getInCli().readLine();
        } catch (IOException e) {
            return false;
        }
        pattern = Pattern.compile("^[ABC][123]$");
        System.out.println(pos);
        matcher = pattern.matcher(pos);
        if (!matcher.find()) {
            return false;
        }
        int abs = Integer.parseInt(String.valueOf(pos.charAt(1)))  -1;
        switch(pos.charAt(0)){
            case 'A':
                if(this.grid[0][abs].compareTo(" ") !=0) return false;
                this.grid[0][abs] = this.playingPlayer.getRole();
                break;
            case 'B':
                if(this.grid[1][abs].compareTo(" ") !=0) return false;
                this.grid[1][abs] = this.playingPlayer.getRole();
                break;
            case 'C':
                if(this.grid[2][abs].compareTo(" ") !=0) return false;
                this.grid[2][abs] = this.playingPlayer.getRole();
                break;
        }

        return true;
    }


    private int isGamePlaying(){
        //Check of the rows
        for (int row = 0; row < 3; row++) {
            if(!this.grid[row][0].equals(" ") && this.grid[row][0].equals(this.grid[row][1]) && this.grid[row][0].equals(this.grid[row][2])){
                return 1;
            }
        }
        //Check of the columns
        for (int column = 0; column < 3; column++) {
            if(!this.grid[0][column].equals(" ") && this.grid[0][column].equals(this.grid[1][column]) && this.grid[0][column].equals(this.grid[2][column])){
                return 1;
            }
        }

        //Check of the diagonals
        if(!this.grid[0][0].equals(" ") && this.grid[0][0].equals(this.grid[1][1]) && this.grid[0][0].equals(this.grid[2][2])){
            return 1;
        }
        if(!this.grid[2][0].equals(" ") &&this.grid[2][0].equals(this.grid[1][1]) && this.grid[2][0].equals(this.grid[0][2])){
            return 1;
        }

        //Check for empty spaces in the grid
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                if(this.grid[row][column].equals(" ")){
                    return 0;
                }
            }
        }

        //Egalité - Draw
        return -1;
    }

    private void printGrid() throws IOException{
        StringJoiner joinerFinal = new StringJoiner("\n","205\n    1   2   3\n","");
        for (int i = 0; i < 3; i++) {
            StringJoiner joinerRow = new StringJoiner(" | ",""," |");
            joinerRow.add(i == 0 ? "A" : (i == 1 ? "B" : "C"));
            for (int j = 0; j < 3; j++) {
                joinerRow.add(this.grid[i][j]);
            }
            joinerFinal.add(joinerRow.toString());

        }
        System.out.println(this.playingPlayer.getPseudo() + " " + this.waitingPlayer.getPseudo());
        System.out.println(joinerFinal.toString().substring(3));
        this.playingPlayer.getOutCli().println(joinerFinal.toString());
        this.waitingPlayer.getOutCli().println(joinerFinal.toString());
    }

    private void setUpGrid(){
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid.length; j++) {
                grid[i][j] = " ";
            }
        }
    }

    private void resetGame(){
        this.setUpGrid();
        this.chooseFirstPlayer(this.playingPlayer, this.waitingPlayer);
    }

    public boolean askPlayAgain(){
        try {
            this.playingPlayer.getOutCli().println("Voulez-vous rejouer?(y/n)");
            String p1Answer = this.playingPlayer.getInCli().readLine();
            this.playingPlayer.getOutCli().println("Voulez-vous rejouer?(y/n)");
            String p2Answer = this.waitingPlayer.getInCli().readLine();
            if(p1Answer.equals("y") && p2Answer.equals("y")){
                this.resetGame();
                return true;
            }
            return false;
        } catch (IOException ex){
            System.err.println(ex.toString());
            return false;
        }
    }
}
