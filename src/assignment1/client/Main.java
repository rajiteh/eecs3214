package assignment1.client;

import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {
	private static final Logger log = Logger.getLogger("Client");

	public static void main(String[] args) throws InterruptedException {
		log.setLevel(Level.SEVERE);
		ClickerClient client = new ClickerClient();
		ClickerCLIClient cli = new ClickerCLIClient(client);
		cli.startREPL();
	}

}
