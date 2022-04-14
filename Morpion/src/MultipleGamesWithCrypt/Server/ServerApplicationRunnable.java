package MultipleGamesWithCrypt.Server;

import javax.crypto.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.StringJoiner;
/**
 * @authors Ylona Fabiani - Elie Roure - David Binaud
 */
public class ServerApplicationRunnable implements Runnable{

    public Socket socketCli1;
    public DataInputStream disClient1;
    public DataOutputStream dosClient1;

    public Socket socketCli2;
    public DataInputStream disClient2;
    public DataOutputStream dosClient2;

    private Key des;

    private String[][] grille;

    private ArrayList<String> pseudos;

    /**
     * Constructor for the Server class
     * @param sockCli1 the socket corresponding to the first client
     * @param sockCli2 the socket corresponding to the second client
     * @throws Exception
     */
    public ServerApplicationRunnable(Socket sockCli1, Socket sockCli2) throws Exception {
        pseudos = new ArrayList<>();
        grille = new String[3][3];
        for (int i = 0; i < grille.length; i++) {
            for (int j = 0; j < grille.length; j++) {
                grille[i][j] = " ";
            }
        }

        this.socketCli1 = sockCli1;
        this.disClient1 = new DataInputStream(socketCli1.getInputStream());
        this.dosClient1 = new DataOutputStream(socketCli1.getOutputStream());

        this.socketCli2 = sockCli2;
        this.disClient2 = new DataInputStream(socketCli2.getInputStream());
        this.dosClient2 = new DataOutputStream(socketCli2.getOutputStream());

        //On génère une clé DES
        KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
        keyGenerator.init(56);
        des = keyGenerator.generateKey();

        getRSAFromClientAndSendEncryptedDES(disClient1, dosClient1);
        getRSAFromClientAndSendEncryptedDES(disClient2, dosClient2);

    }

    /**
     * Method that will get the RSA key sent by a client from the corresponding DataInputStream,
     * will encrypt the previously generated DES key with the public RSA key and will send it back to the client
     * via the corresponding DataOutputStream.
     * @param dis DataInputStream
     * @param dos DataOutputStream
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    private void getRSAFromClientAndSendEncryptedDES(DataInputStream dis, DataOutputStream dos) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        PublicKey rsa;
        //On récupère la clé en tant que tableau de byte
        int tailleClef = dis.readInt();
        byte[] clefByte = new byte[tailleClef];
        for (int i = 0; i < clefByte.length; i++)
            clefByte[i] = dis.readByte();

        //On la convertie en objet
        rsa = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(clefByte));


        //On encrypte la clé DES avec la clé publique RSA
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, rsa);
        byte[] desByte = cipher.doFinal(des.getEncoded());

        //On envoie la clé crypté au client
        dos.writeInt(desByte.length);
        dos.write(desByte);
    }

    /**
     * Lit depuis la socket et décrypte le message lu
     * @param dis DataInputStream from which we will be reading
     * @return String le message décrypté
     * @throws Exception
     */
    public String readSocket(DataInputStream dis) throws Exception {

        while (dis.available() == 0) ;

        int size = dis.readInt();

        //On lit le message
        byte[] messageByte = new byte[size];
        for (int i = 0; i < messageByte.length; i++) {
            messageByte[i] = dis.readByte();
        }

        //On le décrypte avec la clé DES
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(2, des);
        byte[] messageCode = cipher.doFinal(messageByte);

        //On renvoie le message décrypté en tant que String
        return new String(messageCode);


    }

    /**
     * Envoi un message crypté dans la socket
     * @param dos DataOutputStream from which we will send the encrypted message
     * @param message String le message à crypter puis envoyer
     * @throws Exception
     */
    public boolean sendSocket(DataOutputStream dos, String message) throws Exception {
        //On encrypte le message avec la clé DES
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(1, des);
        byte[] messageCode = cipher.doFinal(message.getBytes());
        try {
            //On envoie la taille du message et le message
            dos.writeInt(messageCode.length);
            dos.write(messageCode);
        } catch (Exception e) {
            //Si ya une exception c'est que le socket n'est plus valide donc on le ferme et on renvoie false
            socketCli1.close();
            socketCli2.close();
            return false;
        }
        return true;

    }

    @Override
    public void run(){
        try {

            //Validation Pseudo
            pseudos.add(readSocket(disClient1));
            sendSocket(dosClient1, "103");
            pseudos.add(readSocket(disClient2));
            sendSocket(dosClient2, "103");

            printGrid();

            sendSocket(dosClient1, "201");
            sendSocket(dosClient2, "202");
            DataOutputStream outPlayer = dosClient1;
            DataInputStream inPlayer = disClient1;
            DataOutputStream outWaiter = dosClient2;
            gameLoop:
            do {
                while (!checkPos(readSocket(inPlayer), grille)) {
                    sendSocket(outPlayer, "204");
                }
                sendSocket(outPlayer, "203");
                printGrid();

                switch (isGamePlaying()) {
                    case -1:
                        //egalite
                        sendSocket(outPlayer, "303");
                        sendSocket(outWaiter, "303");
                        break gameLoop;
                    case 0:
                        //On continue a jouer
                        break;
                    case 1:
                        //Heureux gagnant
                        sendSocket(outPlayer, "301");
                        sendSocket(outWaiter, "302");
                        break gameLoop;

                }


                sendSocket(outPlayer, "202");
                sendSocket(outWaiter, "201");

                outPlayer = (dosClient1 == outPlayer) ? dosClient2 : dosClient1;
                inPlayer = (disClient1 == inPlayer) ? disClient2 : disClient1;
                outWaiter = (dosClient1 == outWaiter) ? dosClient2 : dosClient1;

            } while (true);


        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        } finally {
            try {
                if (!socketCli1.isClosed()) {
                    socketCli1.close();
                }
            } catch (IOException ioEx) {
                System.err.println(ioEx.getMessage());
            }

            try {
                if (!socketCli2.isClosed()) {
                    socketCli2.close();
                }
            } catch (IOException ioEx) {
                System.err.println(ioEx.getMessage());
            }
        }

    }

    /**
     * Function that will ask the player a move and check if it's a valid move.
     * It will play the move if it's valid.
     * @return true if the position given by the playing player is valid(in the grid and empty) else it returns false
     */
    private static boolean checkPos(String pos, String[][] grille) {
        int abs = Integer.parseInt(String.valueOf(pos.charAt(1))) - 1;
        switch (pos.charAt(0)) {
            case 'A':
                if (grille[0][abs].compareTo(" ") != 0) return false;
                grille[0][abs] = String.valueOf(pos.charAt(3));
                break;
            case 'B':
                if (grille[1][abs].compareTo(" ") != 0) return false;
                grille[1][abs] = String.valueOf(pos.charAt(3));
                break;
            case 'C':
                if (grille[2][abs].compareTo(" ") != 0) return false;
                grille[2][abs] = String.valueOf(pos.charAt(3));
                break;
        }

        return true;
    }

    /**
     * Function that will format the grid to a friendly format
     * And it will then send that output to the two players
     * @throws IOException
     */
    private void printGrid() throws Exception {
        StringJoiner joinerFinal = new StringJoiner("\n", "205\n    1   2   3\n", "");
        for (int i = 0; i < 3; i++) {
            StringJoiner joinerRow = new StringJoiner(" | ", "", " |");
            joinerRow.add(i == 0 ? "A" : (i == 1 ? "B" : "C"));
            for (int j = 0; j < 3; j++) {
                joinerRow.add(grille[i][j]);
            }
            joinerFinal.add(joinerRow.toString());

        }
        System.out.println(pseudos.get(0) + " - " + pseudos.get(1));
        System.out.println(joinerFinal.toString().substring(3));
        sendSocket(dosClient1, joinerFinal.toString());
        sendSocket(dosClient2, joinerFinal.toString());
    }

    /**
     * Function that will check the state of the game.
     * It will check if someone won and/or any move is still possible(there's an empty box)
     * @return 1 if a player has won meaning there's 3 of the same symbols on the same row or the same column or in one of the diagonals
     *         0 if nobody has won and there's still room to play
     *         -1 if nobody has won and there's no room to play, ie it's a draw
     */
    private int isGamePlaying() {
        //verif ligne OK
        for (int ligne = 0; ligne < 3; ligne++) {
            if (!grille[ligne][0].equals(" ") && grille[ligne][0].equals(grille[ligne][1]) && grille[ligne][0].equals(grille[ligne][2])) {
                return 1;
            }
        }
        //verfi colonne OK
        for (int colonne = 0; colonne < 3; colonne++) {
            if (!grille[0][colonne].equals(" ") && grille[0][colonne].equals(grille[1][colonne]) && grille[0][colonne].equals(grille[2][colonne])) {
                return 1;
            }
        }

        //Verfi diagonale
        if (!grille[0][0].equals(" ") && grille[0][0].equals(grille[1][1]) && grille[0][0].equals(grille[2][2])) {
            return 1;
        }
        if (!grille[2][0].equals(" ") && grille[2][0].equals(grille[1][1]) && grille[2][0].equals(grille[0][2])) {
            return 1;
        }

        //On continue a jouer
        for (int ligne = 0; ligne < 3; ligne++) {
            for (int colonne = 0; colonne < 3; colonne++) {
                if (grille[ligne][colonne].equals(" ")) {
                    return 0;
                }
            }
        }

        //Egalité
        return -1;
    }

}
