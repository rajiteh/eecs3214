package assignment2.coordinator;

import java.io.IOException;
import java.util.Date;

import assignment2.util.CLI;
import assignment2.util.HostPort;


public class CoordinatorCLI extends CLI {

	Coordinator coordinator = null;

	public CoordinatorCLI(Coordinator c) {
		super();
		System.out.println("Welcome to CLICKER Server. Type 'help' to see available commands.");
		validCommands.put("ADD", "addAttacker");
		validCommands.put("DEL", "removeAttacker");
		validCommands.put("TARGET", "setTarget");
		validCommands.put("START", "startAttack");
		validCommands.put("END", "stopAttack");
		validCommands.put("STATUS", "listAttackers");
		validCommands.put("HELP", "printHelp");
		
		this.coordinator = c;
	}
	

	public void addAttacker(String argument) throws Exception {
		methodRequiresArgument(argument);
		HostPort hostPort = new HostPort(argument);
		coordinator.addAttacker(hostPort);
	}
	
	public void removeAttacker(String argument) throws Exception {
		methodRequiresArgument(argument);
		HostPort hostPort = new HostPort(argument);
		coordinator.removeAttacker(hostPort);
	}

	public void listAttackers(String argument) {
		methodDoesNotRequireArgument(argument);
		coordinator.printAttackerStatus();
	}
	
	public void setTarget(String argument) throws IOException {
		methodRequiresArgument(argument);
		HostPort targetHostPort = new HostPort(argument);
		coordinator.updateTargetAll(targetHostPort);
		
	}
	
	
	public void startAttack(String argument) throws Exception {
		Integer timeout = 5;
		if (argument.length() > 0) timeout = Integer.parseInt(argument);
		Date when = new Date(new Date().getTime()+timeout*1000);
		coordinator.initiateAttackAll(when);
	}
	
	public void stopAttack(String argument) throws Exception {
		methodDoesNotRequireArgument(argument);
		coordinator.endAttackAll();
	}
	
	public void printHelp(String argument) {
		methodDoesNotRequireArgument(argument);
		System.out.println(
				"START_QUESTION(n): starts the server process that runs with n choices.\n" +
				"END_QUESTION(): Terminate the server process; students can no longer send responses.\n" +
				"LIST: Lists students who sent answers.\n" +
				"STATUS: Shows current server status.\n" +
				"HELP: Prints this screen."
				);
		super.printHelp(argument);
	}
	
	
	public void exit(String argument) { super.exit(argument); }
	
	
	
}
