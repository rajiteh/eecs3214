package assignment1.client;

import java.util.Scanner;


import assignment1.util.ClickerCLI;

/**
 * @author rajiteh
 *
 */
public class ClickerCLIClient extends ClickerCLI {
	Scanner in = new Scanner(System.in);
	ClickerClient qClient = null;

	/**
	 * Instantiate REPL instance with a ClickerClient
	 * @param qClient
	 */
	public ClickerCLIClient(ClickerClient qClient) {
		super();
		System.out.println("Welcome to CLICKER Client. Type 'help' to see available commands.");
		this.qClient = qClient;
		//Add all available commands in the REPL.
		// Entry = <CommandName>, <MethodName>
		validCommands.put("STUDENT_NUMBER", "setStudentNumber");
		validCommands.put("CLASS_INFO", "setClassIP");
		validCommands.put("ENTER_CHOICE", "enterChoice");
		validCommands.put("INFO", "listInfo");
		validCommands.put("HELP", "printHelp");
	}

	public void setStudentNumber(String argument) {
		methodDoesNotRequireArgument(argument);
		System.out.print("Enter your student number: ");
		qClient.setUserName(in.nextLine());
	}

	public void setClassIP(String argument) {
		methodDoesNotRequireArgument(argument);
		System.out.print("Enter class infromation (<host/ip>:<port>) : ");
		String hostPortIn = in.nextLine();
		String[] hostPort = hostPortIn.split(":");
		if (hostPort.length != 2) throw new IllegalArgumentException("Argument must be in the format <host/ip>:<port>");
		try {
			qClient.setServerIP(hostPort[0]);
			qClient.setServerPort(Integer.parseInt(hostPort[1]));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Port must be numeric.");
		}
	}

	public void enterChoice(String argument) {
		methodDoesNotRequireArgument(argument);
		qClient.connect();
	}

	public void listInfo(String argument) {
		methodDoesNotRequireArgument(argument);
		System.out.println("Current student number : " + qClient.getUserName());
		System.out.println("Current class info     : " + qClient.getServerIP() + ":" + qClient.getServerPort());
	}
	
	public void printHelp(String argument) {
		methodDoesNotRequireArgument(argument);
		System.out.println(
				"STUDENT_NUMBER: allows the student to enter their student number.\n" +
						"CLASS_INFO: allows the student to enter the IP address and port number of the instructor's computer.\n" +
						"ENTER_CHOICE(): Start a client process, connect to server (using the information above) and send the " +
						"student number, receive the number of choices, prompt the student user for his/her choice and send " + 
						"this choice to the server an close connection.\n" +
						"INFO: Display current student number and class information."
				);
		super.printHelp(argument);
	}
	
	public void exit(String argument) { super.exit(argument); }
}
