package assignment1.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
public class ClickerAPIServer extends ClickerAPI {
	private static final Logger log = Logger.getLogger( "Server" );
	/**
	 *  Data source for user lookup
	 */
	ClickerDAO dao = null;
	
	/**
	 *  User authenticated for the current session
	 */
	String currentUser = null;
	
	
	/**
	 * Instantiate an API Server class with a backing data store, input stream and an output stream for communication.
	 * @param dao
	 * @param inputStream
	 * @param outputStream
	 */
	public ClickerAPIServer(ClickerDAO dao, InputStream inputStream,
			OutputStream outputStream) {
		super(inputStream, outputStream);
		this.dao = dao;
	}

	
	/* 
	 * Establishes a communication session with the client by authenticating, sending answers and recieving answers.
	 * (non-Javadoc)
	 * @see assignment1.util.ClickerAPI#createSession()
	 */
	public void createSession() {
		try {
			if (authenticate()) {
				log.log(Level.INFO, this.currentUser + " | Sending choices");
				sendChoiceCount();
				log.log(Level.INFO, this.currentUser + " | Waiting for answers");
				Integer answer = retrieveAnswer();
				log.log(Level.INFO, this.currentUser + " | Received answer " + answer);
				this.dao.incrementChoice(currentUser, answer);
			} else {
				System.out.println("Client was not authenticated");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves the answers sent by the client. Reads a ClickerRequest with intent ANSWERS
	 * @return Answer from the user.
	 * @throws IOException
	 */
	protected Integer retrieveAnswer() throws IOException {
		ClickerRequest answerResponse = ClickerRequest.get(this.socketIn);
		if (answerResponse.getAction() == ClickerRequest.ACTION_ANSWERS) {
			try {
				Integer answer = Integer.parseInt(answerResponse.getData());
				if (answer < 1 || answer > this.dao.choiceCount) throw new IOException("Invalid choice sent by client." + answerResponse.toString());
				return answer;
			} catch (NumberFormatException e) {
				throw new IOException("Unexpected data in the choice field." + answerResponse.toString());
			}
		} else {
			throw new IOException("Unexpected response from client." + answerResponse.toString());
		}
	}
	/**
	 * Sends available question choices to the client via a ClickerRequest with intent QUESTIONS.
	 * @throws IOException
	 */
	protected void sendChoiceCount() throws IOException {
		ClickerRequest choiceResponse = new ClickerRequest();
		choiceResponse.setAction(ClickerRequest.ACTION_QUESTIONS);
		choiceResponse.setData(this.dao.getChoiceCount().toString());
		ClickerRequest.send(choiceResponse, this.socketOut);
	}
	
	/**
	 * Expects a ClickerRequest with intent AUTHENTICATE and sends a ClickerRequest with intent AUTHENTICATED with the corresponding value
	 * @return boolean value indicating the authorization status
	 * @throws IOException
	 */
	protected boolean authenticate() throws IOException {
		boolean authenticated = false;
		log.log(Level.INFO, "Expecting authentication request.");
		ClickerRequest authRequest = ClickerRequest.get(this.socketIn);
		if (authRequest.getAction() == ClickerRequest.ACTION_AUTHENTICATE) {
			String username = authRequest.getData();
			ClickerRequest authResponse = new ClickerRequest();
			authResponse.setAction(ClickerRequest.ACTION_AUTHENTICATED);
			authenticated = dao.userExists(username);
			if (authenticated == true) {
				log.log(Level.INFO, username + " | Authenticated.");
				authResponse.setData(ClickerRequest.DATA_AUTHENTICATED);
				this.currentUser = username;
			} else {
				log.log(Level.INFO, "Denied authentication for " + username);
				authResponse.setData(ClickerRequest.DATA_NOT_AUTHENTICATED);	
			}
			ClickerRequest.send(authResponse, this.socketOut);
			return authenticated;
		} else {
			throw new IOException("Client did not send a valid authentication request." + authRequest.toString());
		}

	}


}

