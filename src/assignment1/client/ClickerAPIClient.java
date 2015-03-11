package assignment1.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import assignment1.util.ClickerAPI;
import assignment1.util.ClickerRequest;

/**
 * @author rajiteh
 *
 */
/**
 * @author rajiteh
 *
 */
/**
 * @author rajiteh
 *
 */
/**
 * @author rajiteh
 *
 */
/**
 * @author rajiteh
 *
 */
public class ClickerAPIClient extends ClickerAPI {
	/**
	 * Static logger for printing debug information
	 */
	private static final Logger log = Logger.getLogger("Client");
	/*
	 * Username assigned to the client instance.
	 */
	protected String user = null;

	/**
	 * Instantiates a ClickerAPIClient instance that would recieve seralized ClickerRequest objects
	 * from the supplied inputStream and outputStream. 
	 * @param user Name of the user the API session belongs to.
	 * @param inputStream Input stream to receieve ClickerRequest objects.
	 * @param outputStream Output stream to send ClickerRequest objects.
	 */
	public ClickerAPIClient(String user, InputStream inputStream,
			OutputStream outputStream) {
		super(inputStream, outputStream);
		this.user = user;
		// TODO Auto-generated constructor stub
	}

	/* Performs client authentication and answer feedback.
	 * 
	 * (non-Javadoc)
	 * @see assignment1.util.ClickerAPI#createSession()
	 */
	@Override
	public void createSession() throws IOException {
		if (authenticate()) {
			log.log(Level.INFO, "Waiting for choice count.");
			Integer choiceCount = retrieveChoiceCount();
			printChoices(choiceCount);
			Integer answer = getUserInput(choiceCount);
			log.log(Level.INFO, "Sending reply. " + answer);
			sendChoice(answer);
		} else {
			throw new IOException("Server denied authentication request.");
		}
	}
	
	/**
	 * Creates a TickerRequest containing the supplied answer and sends it via the output stream.
	 * @param answer
	 * @throws IOException
	 */
	protected void sendChoice(Integer answer) throws IOException {
		ClickerRequest response = new ClickerRequest();
		response.setAction(ClickerRequest.ACTION_ANSWERS);
		response.setData(answer.toString());
		ClickerRequest.send(response, this.socketOut);
	}
	
	/**
	 * Gets a valid choice from user from the given choices.
	 * @param availableChoices
	 * @return User's choice.
	 */
	protected Integer getUserInput(int availableChoices) {
		Integer answer = -1;
		List<String> optionArray = Arrays.asList(ClickerRequest.CHOICE_HEADERS)
				.subList(0, availableChoices);
		while(true) {
			@SuppressWarnings("resource")
			Scanner in = new Scanner(System.in);
			String option = in.nextLine();
			answer = optionArray.indexOf(option);
			if (answer == -1) {
				System.out.println("Invalid input. Try again.");
			} else {
				answer++;  //To offset the array
				break;
			}
		}
		return answer;
	}
	
	
	/**
	 * Presents user with the number of choices available.
	 * @param choiceCount
	 */
	protected void printChoices(int choiceCount) {
		System.out.println("Please select a chice by typing in the corresponding choice number.");
		for(int i=0; i < choiceCount; i++) {
			System.out.println(ClickerRequest.CHOICE_HEADERS[i] + ") Choice " + (i+1));
		}
	}

	/**
	 * Retrieves a choice count from the server.
	 * @return
	 * @throws IOException
	 */
	protected Integer retrieveChoiceCount() throws IOException {
		ClickerRequest choiceCountRequest = ClickerRequest.get(this.socketIn);
		if (choiceCountRequest.getAction() == ClickerRequest.ACTION_QUESTIONS) {
			try {
				Integer choices = Integer.parseInt(choiceCountRequest.getData());
				if (choices < 1 || choices > 5) throw new IOException("Invalid choice count sent by server." + choiceCountRequest.toString());
				return choices;
			} catch (NumberFormatException e) {
				throw new IOException("Unexpected data in the choice field." + choiceCountRequest.toString());
			}
		} else {
			throw new IOException("Unexpected message sent from the client" + choiceCountRequest.toString());
		}
	}
	
	
	/*
	 * Authenticates the current API session user against the server by sending ClickerRequest with intent AUTHENTICATE
	 *  (non-Javadoc)
	 * @see assignment1.util.ClickerAPI#authenticate()
	 */
	@Override
	protected boolean authenticate() throws IOException {
		log.log(Level.INFO, "Sending authentication request");
		ClickerRequest authRequest = new ClickerRequest();
		authRequest.setAction(ClickerRequest.ACTION_AUTHENTICATE);
		authRequest.setData(this.user);
		ClickerRequest.send(authRequest, this.socketOut);
		
		log.log(Level.INFO, "Waiting for authentication response.");
		ClickerRequest response = ClickerRequest.get(this.socketIn);
		if (response.getAction() == ClickerRequest.ACTION_AUTHENTICATED) {
			if (response.getData().equals(ClickerRequest.DATA_AUTHENTICATED)) {
				return true;
			} else if (response.getData().equals(ClickerRequest.DATA_NOT_AUTHENTICATED)) {
				return false;
			} else {
				throw new IOException("Server responded with invalid data." + response.toString());
			}
		} else {
			throw new IOException("Server did not respond to authentication request properly." + authRequest.toString());
		}
	}

}
