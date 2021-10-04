import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

class ApplicServerRun implements Runnable{
	private Socket sockcli;
	private int id;
	private String name;

	ApplicServerRun(Socket sockcli, int clientid){
		this.sockcli = sockcli;
		this.id = clientid;
	}

	public void run() {
		DataInputStream in;
		DataOutputStream out;
		byte mess[] = new byte[80];
		Arrays.fill(mess, (byte)0);

		try {
			in = new DataInputStream(this.sockcli.getInputStream());
			out = new DataOutputStream(this.sockcli.getOutputStream());


			in.read(mess,0,80);
			this.name = new String(mess);
			
			//mess = new byte[80];
			String messString;
			do {
				//On lit le message
				mess = new byte[80];
				Arrays.fill(mess, (byte)0);
				in.read(mess,0,80);

				messString = new String(mess);
				//On affiche le message reçu
				System.out.println("Client " + this.id + ": " + name + ", message reçu:" + messString);

				//On envoie un accusé de reception
				out.write(mess);
			} while(!messString.equals("cp"));
					
			System.out.println("Closing");

			//On ferme la socket
			this.sockcli.close();
		} catch (IOException ex){

		}


	}
}