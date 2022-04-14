package MultipleGamesWithCrypt.Client;

import java.net.Socket;

/**
 * @authors Ylona Fabiani - Elie Roure - David Binaud
 */
public class Main
{

    /**
     * Main function, used to connect to a socket and create a Client
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        Socket socket = new Socket("localhost", 1234);

        //On créer une connexion au serveur
        Client s = new Client(socket);
        s.run();
    }
}
