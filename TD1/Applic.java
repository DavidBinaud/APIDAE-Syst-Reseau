import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class Applic{
	public static void main(String[] args) {
		ServerSocket sockserv = null;
		DataInputStream in;
		DataOutputStream out;
		byte mess[];

		try {
		sockserv = new ServerSocket(1234);
			while(true) {
				try {
					Socket sockcli = sockserv.accept();
					in = new DataInputStream(sockcli.getInputStream());
					out = new DataOutputStream(sockcli.getOutputStream());
					mess = new byte[80];
					in.read(mess,0,80);
					out.write(mess);
					sockcli.close();
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