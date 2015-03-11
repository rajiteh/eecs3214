package assignment1.server;

import java.io.IOException;
import java.util.Map;

import assignment1.util.ClickerCLI;
import assignment1.util.ClickerRequest;

public class ClickerCLIServer extends ClickerCLI {

	ClickerServer qServer = null;

	public ClickerCLIServer(ClickerServer qServer) {
		super();
		System.out.println("Welcome to CLICKER Server. Type 'help' to see available commands.");
		this.qServer = qServer;
		validCommands.put("START_QUESTION", "startQuestion");
		validCommands.put("END_QUESTION", "endQuestion");
		validCommands.put("LIST", "listUsers");
		validCommands.put("STATUS", "status");
		validCommands.put("HELP", "printHelp");
	}
	

	public void startQuestion(String argument) throws IOException {
		methodRequiresArgument(argument);
		try {
			int count = Integer.parseInt(argument);
			if (count < 2 || count > 5) throw new NumberFormatException();
			qServer.stopServer().setChoices(count).startServer();
			if (qServer.isRunning()) {
				System.out.println("Server started.");
			} else {
				System.out.println("Could not start the server.");
			}
			status("");
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Count must be between 3-5.");
		}
		
	}

	public void endQuestion(String argument) throws IOException {
		methodDoesNotRequireArgument(argument);
		qServer.stopServer();
		if (qServer.isRunning()) {
			System.out.println("Could not stop the server.");
		} else {
			System.out.println("Server stopped.");
		}
		status("");
	}
	
	
	public void listUsers(String argument) {
		methodDoesNotRequireArgument(argument);
		Map<String, Integer[]> userList = qServer.getUsers();
		System.out.print("Name\t\t|");
		for(int i=0; i < qServer.choices; i++)
			System.out.print(ClickerRequest.CHOICE_HEADERS[i] + "\t\t|");
		System.out.println();
		for (Map.Entry<String, Integer[]> entry : userList.entrySet())
		{
			
			System.out.print(entry.getKey() + "\t\t|");
			Integer[] answers = entry.getValue();
			for(int i=0; i < qServer.choices; i++)
				System.out.print(answers[i] + "\t\t|");
			System.out.println();
		}
		
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
	
	public void status(String argument) {
		methodDoesNotRequireArgument(argument);
		System.out.println("Server port    : " + qServer.getServerPort());
		System.out.println("Question count : " + qServer.getChoices());
		System.out.println("Server running : " + (qServer.isRunning() ? "YES" : "NO"));
	}
	
	public void exit(String argument) { super.exit(argument); }
	
	
	
}
