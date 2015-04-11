package assignment2.coordinator;
import org.apache.logging.log4j.LogManager;

import assignment2.util.PortExtractor;
public class Main {

	public static void main(String[] args) throws Exception {
		Coordinator coordinator = new Coordinator(LogManager.getLogger("Coordinator"));
		CoordinatorCLI cli = new CoordinatorCLI(coordinator);
		cli.startREPL();
	}
}
