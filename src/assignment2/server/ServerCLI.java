package assignment2.server;

import java.io.IOException;

import assignment2.util.CLI;
import assignment2.util.HostPort;

public class ServerCLI extends CLI {
	Server server = null;
	
	public ServerCLI(Server s) {
		System.out.println("Welcome to CLICKER Server. Type 'help' to see available commands.");
		validCommands.put("LISTEN", "startServer");
		validCommands.put("STATUS", "getStatus");
		validCommands.put("HELP", "printHelp");
		
		this.server = s;
	}
	
	
	public void startServer(String arguments) throws IOException {
		
		if (arguments.length() > 0) {
			server.setListenAddress(new HostPort(arguments));
		}
		server.startServer();
	}
	
	public void getStatus(String arguments) {
		server.status();
	}

}
