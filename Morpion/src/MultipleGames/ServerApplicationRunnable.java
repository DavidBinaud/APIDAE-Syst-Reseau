package MultipleGames;

import MorpionGame.MorpionGame;
import java.net.Socket;


/**
 * A class that is a runnable, it can be threaded
 * It will be the class that instanciate the game
 * @authors Ylona Fabiani - Elie Roure - David Binaud
 */
public class ServerApplicationRunnable implements Runnable {

    /**
     * Represents the game of morpion
     */
    private MorpionGame morpionGame;

    /**
     * Constructor of the class, it will create a new MorpionGame
     * @param sockCli1
     * @param sockCli2
     */
    public ServerApplicationRunnable(Socket sockCli1, Socket sockCli2) {
        this.morpionGame = new MorpionGame(sockCli1, sockCli2);
    }

    /**
     * Function that will be called by the thread
     * it will launch the game of Morpion
     */
    public void run() {
            this.morpionGame.play();
    }
}