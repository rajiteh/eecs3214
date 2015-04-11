package assignment2.server;

import java.io.IOException;

import assignment2.util.HostPort;
import assignment2.util.PortExtractor;

public class Main {

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			throw new Exception("Port number must be supplied.");
		}
		
		Integer port = PortExtractor.extractPort(args[0]);
		Server server = new Server(new HostPort("0.0.0.0:" + port));
		ServerCLI cli = new ServerCLI(server);
		try {
			cli.startServer("");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cli.startREPL();

	}

}
