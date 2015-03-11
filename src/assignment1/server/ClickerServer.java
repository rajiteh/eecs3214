package assignment1.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClickerServer implements Runnable {
	private static final Logger log = Logger.getLogger("Server");

	/**
	 *  DAO Class the server will be instantiating up on starting
	 */
	Class<ClickerDAO> daoClass;
	/**
	 *  DAO class the current server have instantiated
	 */
	ClickerDAO clickerDAO = null;
	/**
	 * 
	 */
	int serverPort 		= 8080;
	Socket socket = null;
	ServerSocket serverSocket = null;
	Thread serverThread = null;
	/*
	 * Indicates whether current server thread is marked for termination
	 */
	boolean markedForTermination = false;
	/*
	 * Number of choices the server is running with
	 */
	int choices = 0;

	/**
	 * @return the serverPort
	 */
	protected int getServerPort() {
		return serverPort;
	}

	/**
	 * @param serverPort the serverPort to set
	 */
	protected ClickerServer setServerPort(int serverPort) {
		this.serverPort = serverPort;
		log.log(Level.INFO, "Port set to " + this.serverPort);
		return this;
	}

	/**
	 * @return the socket
	 */
	protected ServerSocket getServerSocket() {
		return serverSocket;
	}

	/**
	 * @return the socket
	 */
	protected Socket getSocket() {
		return socket;
	}

	/**
	 * @param socket the socket to set
	 */
	protected void setSocket(Socket socket) {
		this.socket = socket;
	}

	/**
	 * @param socket the socket to set
	 */
	protected void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public ClickerServer setChoices(int n){
		this.choices = n;
		log.log(Level.INFO, "Choices set to " + this.choices);
		return this;
	}
	
	public int getChoices(){
		return this.choices;
	}

	/*
	 * Instantiates a clicker server controller with a DAO Class to instantiate up on server start.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ClickerDAO> ClickerServer(Class<T> daoClass) {
		this.daoClass = (Class<ClickerDAO>) daoClass;
		log.log(Level.INFO, "Server instantiated.");
	}

	/**
	 * Creates a non-blocking server listening to connections on the specified port.
	 * @return
	 * @throws IOException
	 */
	public ClickerServer startServer() throws IOException {
		//Check if we are already running
		if (this.isRunning()) {
			log.log(Level.SEVERE, "Request to start the server while the server is already started.");
			throw new IllegalStateException("Server is already running. Stop the server before trying to restart it.");
		}
		
		//Instantiate Thread object with a Runnable that creates a server that listens to incoming connections.
		this.serverThread = new Thread(new Runnable() {
			ClickerServer qs = null;

			Runnable initialize(ClickerServer qs) {
				this.qs = qs;
				return this;
			}

			public void run() {
				log.log(Level.INFO, "Server started.");
				try {
					//Set up the data access object
					this.qs.clickerDAO = this.qs.daoClass.getDeclaredConstructor(Integer.class).newInstance(this.qs.choices);
					//Update the serverSocket for the main class
					this.qs.setServerSocket(new ServerSocket(this.qs.getServerPort(), 50, InetAddress.getByName("0.0.0.0")));
					log.log(Level.INFO, "Server bound to port " +
							this.qs.getServerSocket().getLocalPort());
					//Listen to connections as long not marked for termination.
					while (!this.qs.markedForTermination) {
						try {
							this.qs.setSocket(this.qs.getServerSocket().accept());
							log.log(Level.INFO, String.format("Connected. IP:%s PORT:%d", 
									this.qs.getSocket().getInetAddress().toString(),
									this.qs.getSocket().getPort()));
							//Creates a new thread that executes the accepted client connection
							new Thread(this.qs).start();
						} catch (Exception e) {
							log.log(Level.SEVERE, e.toString());
							break;
						}
						//Resume listening to new connections.
					}
					log.log(Level.INFO, "Shutting down server.");
					if (this.qs.getServerSocket() != null &&
							!this.qs.getServerSocket().isClosed()) {
						this.qs.getServerSocket().close();
					}

				} catch (IOException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
					log.log(Level.SEVERE, e1.toString());
				}
				//Server died. Clean up.
				this.qs.clickerDAO = null;
				this.qs.markedForTermination = false;
			}

		}.initialize(this));
		//Actually run the instantiated thread.
		this.serverThread.start();
		log.log(Level.INFO, "Server dispatched."); 
		return this;
	}
	
	
	/**
	 * Stops a running server
	 * @return
	 * @throws IOException
	 */
	public ClickerServer stopServer() throws IOException {
		if (this.isRunning()) {
			this.markedForTermination = true;
			this.getServerSocket().close();
			log.log(Level.INFO, "Waiting for termination.");
			while(this.isRunning());
		}
		else {
			log.log(Level.INFO, "Asked to stop a non running server!");
		}
		return this;
	}

	/*
	 * Creates new Client API class and assigns teh socket input and output from current client connection to it.
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		try {
			ClickerAPIServer api = new ClickerAPIServer(this.clickerDAO, this.socket.getInputStream(), this.socket.getOutputStream());
			log.log(Level.INFO, "Session starts.");
			api.createSession();
			this.socket.close();
			log.log(Level.INFO, "Session stopped.");
		} catch (IOException e) {
			System.out.println(e);
		}
	}


	public boolean isRunning() {
		return this.serverThread != null && this.serverThread.isAlive();
	}

	public Map<String, Integer[]> getUsers() {
		if (this.clickerDAO == null) throw new IllegalStateException("Server has not been started yet.");
		return this.clickerDAO.getAnswers();
	}
}
