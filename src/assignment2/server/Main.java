package assignment2.server;

import java.io.IOException;

import assignment2.util.HostPort;

public class Main {

	public static void main(String[] args) throws InterruptedException, IOException {
		Server server = new Server(new HostPort("0.0.0.0:31012"));
		ServerCLI cli = new ServerCLI(server);
		cli.setPrompt("");
		try {
			cli.startServer("");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cli.startREPL();

	}

}
