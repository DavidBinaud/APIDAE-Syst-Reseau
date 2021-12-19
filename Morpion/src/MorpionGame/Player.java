package MorpionGame;

import java.io.*;
import java.net.Socket;

/**
 * A class representing a player in a game of morpion
 * @authors Ylona Fabiani - Elie Roure - David Binaud
 */
public class Player {

    /**
     * the pseudonyme of the player for display purposes
     */
    private String pseudo;

    /**
     * the role of the player in the morpion game
     * it can be either "O" or "X"
     */
    private String role;

    /**
     * The socket used to communicate with the client
     */
    private Socket sockCli;

    /**
     * The object that allows us to read what the distant client is writing
     */
    private BufferedReader inCli;

    /**
     * The object that allows us to write to the client
     */
    private PrintWriter outCli;

    /**
     * The constructor of the class Player
     * @param sockCli
     */
    public Player(Socket sockCli) {
        this.sockCli = sockCli;
        try{
            this.inCli = new BufferedReader(new InputStreamReader(this.sockCli.getInputStream()));
            this.outCli = new PrintWriter(new OutputStreamWriter(this.sockCli.getOutputStream()), true);
        } catch (
        IOException ioEx){
            System.err.println(ioEx.toString());
        }
    }

    /**
     * The getter of the pseudo
     * @return the pseudo of the player
     */
    public String getPseudo() {
        return pseudo;
    }

    /**
     * The setter of the pseudo
     * @param pseudo
     */
    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    /**
     * The getter of the socket
     * @return the socket of the player
     */
    public Socket getSockCli() {
        return sockCli;
    }

    /**
     * The getter of the reader
     * @return the reader of the player
     */
    public BufferedReader getInCli() {
        return inCli;
    }

    /**
     * The getter of the writer
     * @return the writer of the player
     */
    public PrintWriter getOutCli() {
        return outCli;
    }

    /**
     * The getter of the role
     * @return the role of the player
     */
    public String getRole() {
        return role;
    }

    /**
     * The setter of the role
     * @param role
     */
    public void setRole(String role) {
        this.role = role;
    }
}
