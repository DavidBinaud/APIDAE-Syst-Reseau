import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

class ApplicServer{
	public static void main(String[] args) {
		ServerSocket sockserv = null;
		
		try {
			//On instancie le socket serveur sur le port 1234
			sockserv = new ServerSocket(1234);
			int clientid = 0;
			while(true) {
				try {
					//On instancie le socket et les streams
					Socket sockcli = sockserv.accept();
					ApplicServerRun p = new ApplicServerRun(sockcli, clientid);
     				new Thread(p).start();
     				clientid++;
				} catch (IOException ex){

				}
			}
		} catch (IOException ex){

		} finally {
				try {
					sockserv.close();
				} catch (IOException ex) {

				}
		}
	}

}