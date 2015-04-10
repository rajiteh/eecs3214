package assignment2.attacker;

import assignment2.util.CLI;
import assignment2.util.HostPort;

public class AttackerCLI extends CLI {

	Attacker attacker = null;

	public AttackerCLI(Attacker a) {
		super();
		System.out.println("Welcome to CLICKER Server. Type 'help' to see available commands.");
		validCommands.put("LISTEN", "startListening");
		validCommands.put("LISTEN_TO", "setListenAddress");
		validCommands.put("STATUS", "status");
		validCommands.put("HELP", "printHelp");
		
		this.attacker = a;
	}
	

	public void setListenAddress(String argument) throws Exception {
		methodRequiresArgument(argument);
		HostPort hostPort = new HostPort(argument);
		attacker.setListenAddress(hostPort);
	}
	
	public void startListening(String argument) throws Exception {
		methodDoesNotRequireArgument(argument);
		attacker.startListener(true);
	}
	
	public void status(String argument) {
		methodDoesNotRequireArgument(argument);
		attacker.status();
	}
	
	
	public void printHelp(String argument) {
		methodDoesNotRequireArgument(argument);
		System.out.println(
				""
				);
		super.printHelp(argument);
	}
	
	public void exit(String argument) { super.exit(argument); }
	
	
	
}
