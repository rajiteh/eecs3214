package assignment1.server;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

	private static final Logger log = Logger.getLogger("Server");


	public static void main(String args[]) throws Exception {
		int port = 31011;
		//Try to get the args
		try {
			if (args.length > 0) {
				if (args.length == 1) {
					try {
						port = Integer.parseInt(args[0]);
					} catch (NumberFormatException e) {
						throw new Exception("Argument must be a number.");
					}	
				} else {
					throw new Exception("Invalid number of arguments.");		
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			System.out.println("Usage: java assignment1.server.Main [PortNumber]");
			System.exit(1);
		}
		
		log.setLevel(Level.SEVERE);
		ClickerServer server = new ClickerServer(ClickerDAOHardcoded.class).setServerPort(port);
		ClickerCLIServer cli = new ClickerCLIServer(server);
		cli.startREPL();
	}


}
