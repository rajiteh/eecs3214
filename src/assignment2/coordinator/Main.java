package assignment2.coordinator;
import org.apache.logging.log4j.LogManager;
public class Main {

	public static void main(String[] args) throws InterruptedException {

		Coordinator coordinator = new Coordinator(LogManager.getLogger("Coordinator"));
		CoordinatorCLI cli = new CoordinatorCLI(coordinator);
		try {
			cli.setPrompt("");
			cli.addAttacker("127.0.0.1:31011");
			cli.setTarget("127.0.0.1:31012");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cli.startREPL();
	}
}
