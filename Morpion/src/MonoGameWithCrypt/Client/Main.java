package MonoGameWithCrypt.Client;

import MonoGameWithCrypt.ClientP.InputReader;

import java.net.Socket;


public class Main
{

    public static void main(String[] args) throws Exception
    {
        Socket socket = new Socket("localhost", 1234);

        //On créer une connexion au serveur
        Client s = new Client(socket);
        s.run();
    }
}
