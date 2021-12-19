package MultipleGames;

import MorpionGame.MorpionGame;
import java.net.Socket;


/**
 * @authors Ylona Fabiani - Elie Roure - David Binaud
 */
public class ServerApplicationRunnable implements Runnable {

    private MorpionGame morpionGame;

    public ServerApplicationRunnable(Socket sockCli1, Socket sockCli2) {
        this.morpionGame = new MorpionGame(sockCli1, sockCli2);
    }

    public void run() {
            this.morpionGame.play();
    }
}