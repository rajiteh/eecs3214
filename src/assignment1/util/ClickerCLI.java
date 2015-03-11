package assignment1.util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClickerCLI {

	protected static final Logger log = Logger.getLogger( ClickerCLI.class.getName() );
	protected Scanner in = new Scanner(System.in);
	/**
	 * Map containing the valid commands that is supported by this REPL interface. <cmdName>, <methodName>
	 */
	protected HashMap<String, String> validCommands = new HashMap<String,String>();
	
	public ClickerCLI() {
		log.setLevel(Level.SEVERE);
		validCommands.put("EXIT", "exit");
	}

	/**
	 * Accepts user input, validates it's syntax and executes the corresponding method associated with it as specified
	 * by the validCommands HashMap. This method is encapsulated in an infinite loop to accept user input till program termination.
	 * @throws InterruptedException
	 */
	public void startREPL() throws InterruptedException {
		Pattern p = Pattern.compile("(?<command>[A-Za-z_]+)(?<argument>\\(.*\\))?");
		while(true) {
			//Get input
			String input = getPrompt();
			Matcher m = p.matcher(input);
			try {
				//Valid input?
				if (m.matches()) {
					String parsedCommand = m.group("command").toUpperCase();
					String parsedArgument = "";
					parsedArgument = m.group("argument") != null ? m.group("argument") : "()";
					//Get any argument supplied with the command
					parsedArgument = parsedArgument.substring(1, parsedArgument.length() - 1); //Strip brackets
					log.log(Level.INFO, "Parsed command : " + parsedCommand + " Arguments: " + parsedArgument);
					//Valid command?
					if (validCommands.containsKey(parsedCommand)) {
						//Execute the command on the class using Reflections.
						this.getClass().getDeclaredMethod(validCommands.get(parsedCommand) , new Class[] { String.class }).invoke(this, parsedArgument);
					} else {
						throwSyntaxError(parsedCommand + "(" + parsedArgument + ")");	
					}
				} else if (input.length() > 0) {
					throwUnrecognizedCmd(input);
				}
			} catch (IllegalAccessException | IllegalArgumentException
					| NoSuchMethodException
					| SecurityException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				Throwable cause = e.getCause();
				if (cause != null)
					cause.printStackTrace();
				else
					e.printStackTrace();
			};
		}
	}
	

	private String getPrompt() {
		System.out.print(">>> ");
		return in.nextLine();
	}
	
	
	protected void methodRequiresArgument(String argument) throws IllegalArgumentException {
		if (argument.length() == 0) throw new IllegalArgumentException("This method requires an argument.");
	}
	
	protected void methodDoesNotRequireArgument(String argument) throws IllegalArgumentException {
		if (argument.length() > 0) throw new IllegalArgumentException("This method does not accept any arguments.");
	}
	
	private void throwUnrecognizedCmd(String command) throws NoSuchMethodException {
		throw new NoSuchMethodException("Unrecognized command. ('" + command + "'). Type help to see the available commands.");	
	}
	private void throwSyntaxError(String command) throws NoSuchMethodException {
		throw new NoSuchMethodException("Syntax error. ('" + command + "'). Type help to see the available commands.");
	}

	public void printHelp(String argument) {
		System.out.println("EXIT: Quit the program.");
	}
	
	public void exit(String argument) {
		System.out.println("Exiting");
		System.exit(0);
	}
}
