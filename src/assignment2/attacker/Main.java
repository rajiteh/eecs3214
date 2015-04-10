package assignment2.attacker;

import java.net.BindException;

import assignment2.util.HostPort;

import org.apache.logging.log4j.LogManager;

public class Main {

	public static void main(String[] args) throws Exception {
		Attacker attacker = null;
		int basePort = 31011;
		attacker = new Attacker(new HostPort("0.0.0.0:" + basePort), LogManager.getLogger("Attacker"));
		while(true) {
			Thread.sleep(1000); 
			if (attacker.isListening()) break;
			attacker.setListenAddress(new HostPort("0.0.0.0:" + ++basePort));
			attacker.startListener();
		}
		
		AttackerCLI cli = new AttackerCLI(attacker);
		cli.setPrompt("");
		cli.startListening("");
		cli.status("");
		cli.startREPL();
	}

}
