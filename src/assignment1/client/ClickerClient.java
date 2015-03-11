package assignment1.client;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClickerClient {
	private static final Logger log = Logger.getLogger("Client");
	String serverIP;
	String userName;
	int serverPort;

	public ClickerClient() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the serverIP
	 */
	public String getServerIP() {
		return serverIP;
	}

	/**
	 * @param serverIP the serverIP to set
	 */
	public void setServerIP(String serverIP) {
		log.log(Level.INFO, "Server IP set to " + serverIP);
		this.serverIP = serverIP;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		log.log(Level.INFO, "Username set to " + userName);
		this.userName = userName;
	}

	/**
	 * @return the serverPort
	 */
	public int getServerPort() {
		return serverPort;
	}

	/**
	 * @param serverPort the serverPort to set
	 */
	public void setServerPort(int serverPort) {
		log.log(Level.INFO, "Port set to " + serverPort);
		this.serverPort = serverPort;
	}

	
	/**
	 * Creates a connection to the Clicker Server instance and hands over the socket streams to API Class
	 */
	public void connect() {
		Socket socket = null;
		ClickerAPIClient api = null;
		try {
			log.log(Level.INFO, "Attempting to create connection to " + this.getServerIP() + ":" + this.getServerPort());
			socket = new Socket(this.getServerIP(), this.getServerPort());
			api = new ClickerAPIClient(this.getUserName(), socket.getInputStream(), socket.getOutputStream());
			api.createSession();
			socket.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

}
