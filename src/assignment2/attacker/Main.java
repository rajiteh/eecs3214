package assignment2.attacker;


import assignment2.util.HostPort;
import assignment2.util.PortExtractor;

import org.apache.logging.log4j.LogManager;

public class Main {

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			throw new Exception("Port number must be supplied.");
		}
		
		Integer port = PortExtractor.extractPort(args[0]);
		Attacker attacker = new Attacker(new HostPort("0.0.0.0:" + port), LogManager.getLogger("Attacker"));
		
		AttackerCLI cli = new AttackerCLI(attacker);
		cli.setPrompt("");
		cli.startListening("");
		cli.status("");
		cli.startREPL();
	}

}
