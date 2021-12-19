package MorpionGame;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class representing our game of Morpion
 * @authors Ylona Fabiani - Elie Roure - David Binaud
 */
public class MorpionGame {

    /**
     * An array of Strings representing the grid of the game
     * It will be set to a size 3x3 and containing a space at the start
     * example:
     * [[" "," "," "],
     * [" "," "," "],
     * [" "," "," "]]
     */
    private String[][] grid;

    /**
     * The list of players
     */
    private ArrayList<Player> players;

    /**
     * The player that is playing.
     * ie, it's his turn to play
     */
    private Player playingPlayer;

    /**
     * The player which turn it's not to play,
     * He's waiting for the other player to play
     */
    private Player waitingPlayer;

    /**
     * represents a regex
     */
    private static Pattern pattern;

    /**
     * represents an object that matches a regex
     */
    private static Matcher matcher;

    /**
     * The constructor of the game of morpion.
     * It takes the two sockets used to communicate with the clients
     * @param socket1
     * @param socket2
     */
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

    /**
     * Function that chooses Randomly the first player to play.
     * @param p1 Player
     * @param p2 Player
     */
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

    /**
     * The main function of the class.
     * It runs the game completely from start to finish.
     * It will call the other functions of the class.
     */
    public void play(){
        try {
            this.askPseudos();
            this.printGrid();

            //We tell the players who starts
            this.playingPlayer.getOutCli().println("201");
            this.waitingPlayer.getOutCli().println("202");
            try {
                //Loop for the restart of a game
                do{
                    //the game loop, it will be breaked when the game is over
                    gameLoop:
                    do {
                        //while the position given by the player is not correct we ask it again
                        while (!checkPos()) {
                            this.playingPlayer.getOutCli().println("204");
                        }
                        this.playingPlayer.getOutCli().println("203");
                        this.printGrid();

                        switch (this.isGamePlaying()) {
                            case -1:
                                //draw
                                this.playingPlayer.getOutCli().println("303");
                                this.waitingPlayer.getOutCli().println("303");
                                break gameLoop;
                            case 0:
                                //We keep playing as nobody won and the game is'nt over
                                break;
                            case 1:
                                //The player that played wins
                                this.playingPlayer.getOutCli().println("301");
                                this.waitingPlayer.getOutCli().println("302");
                                break gameLoop;

                        }

                        //We switch the players to play
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

    /**
     * Function that asks both the players their pseudos
     * @throws IOException
     */
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

    /**
     * Function to switch the players turns
     */
    private void switchPlayersTurn(){
        Player tmp = this.playingPlayer;
        this.playingPlayer = this.waitingPlayer;
        this.waitingPlayer = tmp;
    }

    /**
     * Function that will ask the player a move and check if it's a valid move.
     * It will play the move if it's valid.
     * @return true if the position given by the playing player is valid(in the grid and empty) else it returns false
     */
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

    /**
     * Function that will check the state of the game.
     * It will check if someone won and/or any move is still possible(there's an empty box)
     * @return 1 if a player has won meaning there's 3 of the same symbols on the same row or the same column or in one of the diagonals
     *         0 if nobody has won and there's still room to play
     *         -1 if nobody has won and there's no room to play, ie it's a draw
     */
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

    /**
     * Function that will format the grid to a friendly format
     * And it will then send that output to the two players
     * @throws IOException
     */
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

    /**
     * Function to fill the grid with spaces
     */
    private void setUpGrid(){
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid.length; j++) {
                grid[i][j] = " ";
            }
        }
    }

    /**
     * function to reset the game by reseting the grid to it's initial state
     * and by choosing the next player to start the game
     */
    private void resetGame(){
        this.setUpGrid();
        this.chooseFirstPlayer(this.playingPlayer, this.waitingPlayer);
    }

    /**
     * Function that will ask the players if they want to play a new game
     * @return true if both players agreed to play again, else false
     */
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
