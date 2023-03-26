package aufgabe_1;

import java.io.IOException;

public class Start {

	public static void main(String[] args) throws IOException {
		int port = 7777;
		Client client = new Client(port);
		Server server = new Server(port);
		server.start();
		client.start();
	}
}
