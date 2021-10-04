import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.Arrays;

class ApplicClient{
	public static void main(String[] args) {
		String url;
		String name;


		//If pour mettre l'url à la main si besoin
		if(args.length >= 1 && args[0] != null){
			url = args[0];
		} else {
			url = "127.0.0.1";
		}

		if(args.length >= 2 && args[1] != null){
			name = args[1];
		} else {
			name = "Random";
		}

		System.out.println(url);

		Socket sock = null;
		DataInputStream in;
		DataOutputStream out;
		byte mess[];

		try {
			sock = new Socket(url ,1234);
			while(true) {
				try {
					//On instancie les variables
					in = new DataInputStream(sock.getInputStream());
					out = new DataOutputStream(sock.getOutputStream());


					//envoi du nom
					out.write(name.getBytes());

					Scanner sc = new Scanner(System.in);
					String line;

					do{
						mess = new byte[80];
						Arrays.fill(mess, (byte)0);
						//On demande un input pour le message
						System.out.print(":");
	     				line = sc.nextLine();

	     				//On affiche le message
	     				//System.out.println("message à envoyer:" + line);

	     				//On envoi le message
						out.write(line.getBytes());

						//ON reçoit la réponse
						in.read(mess,0,80);
						System.out.println("Vu: (" + new String(mess) + ")\n");
					} while (!line.equals("cp"));

					System.out.println("Closing");
					//ON ferme la socket
					sock.close();
				} catch (IOException ex){

				}
			}
		} catch (IOException ex){

		} finally {
				try {
					sock.close();
				} catch (IOException ex) {

				}
		}
		
	}
}