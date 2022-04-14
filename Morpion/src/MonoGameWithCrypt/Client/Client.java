package MonoGameWithCrypt.Client;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * @authors Ylona Fabiani - Elie Roure - David Binaud
 */
public class Client {
    private static Pattern pattern;
    private static Matcher matcher;

    public Socket socketCli;
    public DataOutputStream dos;
    public DataInputStream dis;

    public boolean partieEnCours = true;

    private KeyPair rsa;
    private Key des;

    /**
     * Constructor for the Client class
     * @param s Socket used to communicate with the server
     * @throws Exception
     */
    public Client(Socket s) throws Exception {
        this.socketCli = s;
        this.dos = new DataOutputStream(s.getOutputStream());
        this.dis = new DataInputStream(s.getInputStream());
        initCryptage();
    }

    /**
     * Runs during the whole game of Morpion
     */
    public void run() {
        String role;
        String code;

        try {

            if (socketCli.isConnected()) {
                System.out.println("Vous etes connecté");

                System.out.println("Saisir votre pseudonyme :");
                Scanner sc = new Scanner(System.in);

                String pseudo = sc.nextLine();
                sendSocket(pseudo);
                code = readSocket();
                while (!code.equals("103")) {
                    System.out.println("Pseusonyme invalide, veuillez saisir un autre pseudonyme :");
                    pseudo = sc.nextLine();
                    sendSocket(pseudo);
                }

                System.out.println("En attente d'un autre joueur ... ");
                //On affiche la grille vierge
                code = readSocket();
                if (code.contains("205")) {
                    System.out.println(code.substring(4));
                }

                //attribut les roles
                code = readSocket();
                role = code.contains("201") ? "O" : "X";
                System.out.println("Vous jouez avec les " + role);
                //on lance le premier tour

                do {
                    //traitement switch
                    switch (code.substring(0, 3)) {
                        case "201":
                            System.out.println("Saisir votre coup : ");
                            CheckMove(sc, role);

                            code = readSocket();
                            while (code.contains("204")) {
                                System.out.println("Coup invalide, la case n'est pas vide : ");
                                CheckMove(sc, role);
                                code = readSocket();
                            }
                            //grille
                            code = readSocket();
                            if (code.contains("205")) {
                                System.out.println(code.substring(4));
                            }
                            break;

                        case "202":
                            System.out.println("Coup de l'adversaire, Veuillez patienter");
                            //grille
                            code = readSocket();
                            if (code.contains("205")) {
                                System.out.println(code.substring(4));
                            }
                            break;

                        case "301":
                            System.out.println("Vous avez gagné!");
                            partieEnCours = false;
                            return;
                        case "302":
                            System.out.println("vous avez perdu!");
                            partieEnCours = false;
                            return;
                        case "303":
                            System.out.println("Match nul.");
                            partieEnCours = false;
                            return;
                    }

                    code = readSocket();
                } while (partieEnCours);


            }

        } catch (IOException ioEx) {
            System.err.println(ioEx.getMessage());
        } catch (NullPointerException nullEx) {
            System.err.println("Adversaire deconnecté");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (!socketCli.isClosed()) {
                    socketCli.close();
                }
            } catch (IOException ioEx) {
                System.err.println(ioEx.getMessage());
            }
        }
    }

    /**
     * Will check the format of the move
     * @param saisie
     * @return true if the format is correct, else false
     */
    public boolean checkMoveFormat(String saisie) {
        pattern = Pattern.compile("^[ABC][123]$");
        matcher = pattern.matcher(saisie);
        if (matcher.find()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Will send the move to the server
     * @param sc The Scanner used
     * @param role The role of the player, either X or O
     */
    public void CheckMove(Scanner sc, String role) throws Exception {
        String coups = sc.nextLine();
        while (!checkMoveFormat(coups)) {
            System.out.println("Saisir votre coup dans le format [ABC][123] : ");
            coups = sc.nextLine();
        }
        sendSocket(coups + "_" + role);
    }

    /**
     * Envoi un message crypté dans la socket
     * @param message String le message à crypter puis envoyer
     * @throws Exception
     */
    public synchronized void sendSocket(String message) throws Exception {
        //On encrypte avec la clé DES qu'on a et on envoie la taille du tableau de byte, puis le tableau de byte
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(1, des);
        byte[] messageCode = cipher.doFinal(message.getBytes());
        dos.writeInt(messageCode.length);
        dos.write(messageCode);
    }

    /**
     * Lit depuis la socket et décrypte le message lu
     * @return String le message décrypté
     * @throws Exception
     */
    public String readSocket() throws Exception {
        while (dis.available() == 0) ;

        //On lit la taille du tableau de byte
        int size = dis.readInt();
        if (size == -1) {
            socketCli.close();
            System.out.println("Socket fermé");
        }

        byte[] messageByte = new byte[size];
        //On écrit les byte recu dans un tableau
        for (int i = 0; i < messageByte.length; i++) {
            messageByte[i] = dis.readByte();
        }
        System.err.println("reçu:" + new String(messageByte));
        //On décode le message avec la clé DES
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(2, des);
        byte[] messageCode = cipher.doFinal(messageByte);
        System.err.println("decodé:" + new String(messageCode));
        return new String(messageCode);
    }


    /**
     * It initalizes the RSA key, sends it to the server and gets the DES key crypted by the RSA and decodes it.
     * @throws Exception
     */
    public void initCryptage() throws Exception {
        //On créer une pair de clé RSA
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);

        rsa = keyPairGenerator.generateKeyPair();

        //On encode et envoie la clé publique
        byte[] rsaKey = rsa.getPublic().getEncoded();
        dos.writeInt(rsaKey.length);
        dos.write(rsaKey);

        //On lit la clé DES encodé RSA
        int tailleClef = dis.readInt();
        byte[] clefByte = new byte[tailleClef];

        for (int i = 0; i < clefByte.length; i++)
            clefByte[i] = dis.readByte();

        //On décode la clé DES avec la clé privé RSA
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, rsa.getPrivate());
        byte[] clefByteDecode = cipher.doFinal(clefByte);

        //On récupère et enregistre la clé DES
        SecretKeyFactory sf = SecretKeyFactory.getInstance("DES");
        des = sf.generateSecret(new DESKeySpec(clefByteDecode));

    }
}
// 