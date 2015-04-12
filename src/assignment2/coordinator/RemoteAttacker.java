package assignment2.coordinator;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

import org.apache.logging.log4j.Logger; 

import assignment2.util.APIException;
import assignment2.util.APIMessage;
import assignment2.util.APIMethod;
import assignment2.util.AttackerStatus;
import assignment2.util.HostPort;


/**
 * @author rajiteh
 *  * Holds information and communication methods with an attacker.
 *
 */
public class RemoteAttacker {
	
	/**
	 * How many times should the sync protocol send packet bursts. 
	 */
	private static final int SYNC_CYCLES = 3;
	
	/**
	 * Amount of packets sent during each burst. 
	 */
	private static final int SYNC_DELTA_COUNT = 5;
	
	/**
	 * Number of seconds to wait between bursts.
	 */
	private static final int SYNC_DELTA_TIMEOUT = 2;
	
	/**
	 * Number of seconds to wait before each synchronization. 
	 */
	private static final int CLOCK_SYNC_INTERVAL = 60;
	

	/**
	 * Curret attacker status, initialized to UNKNOWN
	 */
	private AttackerStatus status = AttackerStatus.UNKNOWN;
	
	/**
	 * Last message recieved from the attacker
	 */
	private String lastMessage = "";
	
	
	/**
	 * Target to attack.  
	 */
	private HostPort target = null;
		
	/**
	 * Thread holding the current RemoteAttacker instance.
	 */
	private Thread thread = null;
	
	/**
	 * Runnable instance holding the keep alive timer logic. 
	 */
	private Runnable keepAlive = new Runnable() {
		RemoteAttacker remoteAttacker = null;
		
		public Runnable initialize(RemoteAttacker ra) {
			this.remoteAttacker = ra;
			return this;
		}
		
		@Override
		public void run() {
			remoteAttacker.log.debug("Starting keep alive thread.");
			if (markedForTermination) remoteAttacker.log.warn("Keep alive thread marked for termination.");
			
			while(!markedForTermination) {
				try {
					remoteAttacker.queryStatus();
					if (remoteAttacker.isClockSyncRequired()) {
						remoteAttacker.syncClock();
					} 
				} catch (IOException | APIException | InterruptedException e1) {
					remoteAttacker.setStatus(AttackerStatus.ERROR, e1.getMessage());
				}
				try { Thread.sleep(5000); } catch (InterruptedException e) { }
			}
			remoteAttacker.log.debug("Keep alive thread done.");
		}
		
	}.initialize(this);
	
	/**
	 * Socket that the attacker is connected to. 
	 */
	private Socket socket = null;
	
	/**
	 * Thread marked for termination? 
	 */
	private Boolean markedForTermination = false;
	
	/**
	 * Time when the last message from the attacker was recieved.
	 */
	private Date lastUpdated = new Date(0);
	
	/**
	 * Time when the last clock synchronization happened. 
	 */
	private Date lastClockSync = new Date(0);
	
	/**
	 * Logger for this instance. 
	 */
	private Logger log = null;
	
	/**
	 * RemoteAttacker's IP and port. 
	 */
	private HostPort hostPort;
	
	/**
	 * @return the hostPort
	 */
	public HostPort getHostPort() {
		return hostPort;
	}

	/**
	 * @param hostPort the hostPort to set
	 */
	public void setHostPort(HostPort hostPort) {
		this.hostPort = hostPort;
	}
	
	/**
	 * @return the lastMessage
	 */
	public String getLastMessage() {
		return lastMessage;
	}

	/**
	 * @param lastMessage the lastMessage to set
	 */
	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}
	
	/**
	 * @return the status
	 */
	public AttackerStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 * @param string message to log with the status change
	 */
	public void setStatus(AttackerStatus status, String string) {
		if (!(this.status.equals(status) || this.lastMessage.equals(string)))
			log.info("Status of " + this.getHostPort().toString() + " set to " + status.toString() + " (" + string + ")");
		this.lastUpdated = new Date();
		this.status = status;
		this.lastMessage = string;
	}

	/**
	 * @return the lastUpdated
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * @return the target
	 */
	public HostPort getTarget() {
		return target;
	}

	/**
	 * @param hostPort the target to set
	 * @param localOnly does not broadcast the attacker to attacker.
	 * @throws IOException 
	 */
	public void setTarget(HostPort hostPort, boolean localOnly) throws IOException { 
		if (!localOnly) {
			String targetStr;
			if (hostPort == null) {
				targetStr = "";
			} else { 
				targetStr = hostPort.toString();
			}
			try {
				sendRequest(new APIMessage(targetStr , APIMethod.ATTACKER_SET_TARGET));
				this.target = hostPort;
			} catch (APIException e) {
				setStatus(AttackerStatus.ERROR, e.getMessage());
			}
		} else {
			this.target = hostPort;
		}
		
	}
	
	public void setTarget(HostPort hostPort) throws IOException {
		setTarget(hostPort, false);
	}

	public RemoteAttacker(HostPort hp, Logger log) throws IOException, APIException, InterruptedException {
		this.hostPort = hp;
		this.log = log;

		//Perform initial clock synchronization.
		log.info("Starting attacker clock synchronization.");
		syncClock();
		log.info("Attacker clock synchronized.");
		
		//Start the keep alive thread.
		this.thread = new Thread(this.keepAlive);
		this.thread.start();
	}
	
	/**
	 * @throws IOException
	 * @throws APIException
	 * @throws InterruptedException
	 * 
	 * Allows the client to synchronize it's clock with the coordinator via a special set of timestamped packets.
	 */
	protected void syncClock() throws IOException, APIException, InterruptedException {
		//Notify the attacker that time synchroniztion session is initialized.
		sendRequest(new APIMessage("", APIMethod.ATTACKER_START_SYNC));
		for (int j=0; j < SYNC_CYCLES; j++) {
			//Allow the attacker to prepare for a new cycle of burst values
			sendRequest(new APIMessage(String.valueOf(j), APIMethod.ATTACKER_SYNC_CYCLE));
			for(int i=0; i < SYNC_DELTA_COUNT; i++ ) {
				//Burst send packets for the attacker to calculate roundtrip value.
				sendRequest(new APIMessage(String.valueOf(i), APIMethod.ATTACKER_SYNC_DELTA));
			}
			//Delay before sending the next cycle of burst packets.
			Thread.sleep(SYNC_DELTA_TIMEOUT * 1000);	
		}
		//Notify the attacker that time sync is done.
		sendRequest(new APIMessage(String.valueOf(new Date().getTime()), APIMethod.ATTACKER_STOP_SYNC));
		lastClockSync = new Date();	
		log.debug("Client clock synchronized.");
	}
	
	/**
	 * @param when time for this attack to start at (must be in future)
	 * @throws Exception
	 * 
	 * Notify the attacker to start an attack on the given target.
	 */
	public void startAttack(Date when) throws Exception {
		if (when.before(new Date())) throw new Exception("Cannot travel back in time!");
		
		sendRequest(new APIMessage(String.valueOf(when.getTime()), APIMethod.ATTACKER_START_ATTACK));	
		queryStatus();	
	}
	
	/**
	 * @throws Exception
	 * 
	 * Notify the attacker to immediately stop attacking.
	 */
	public void stopAttack() throws Exception {
		sendRequest(new APIMessage("", APIMethod.ATTACKER_STOP_ATTACK));		
		queryStatus();	
	}

	/**
	 * @throws IOException
	 * 
	 * Query the attacker for it's current state and configured remote. If mismatch,
	 * then update the attacker with proper values.
	 */
	public void queryStatus() throws IOException {
		try {
			APIMessage response = sendRequest(new APIMessage("", APIMethod.ATTACKER_QUERY_STATE));
			
			AttackerStatus status = null;
			HostPort target = null;
			
			//Read the status and remote information from attacker
			String[] statusTarget = response.getData().split(",");
			if (statusTarget.length < 2 || statusTarget[0].length() < 1 || statusTarget[1].length() < 1) {
				throw new APIException("Invalid response");
			} else {
				status = new AttackerStatus(statusTarget[0], statusTarget[1]);
			}
			
			if (statusTarget.length == 3 && statusTarget[2].length() > 0) {
				target = new HostPort(statusTarget[2]);
			}
			
			//Ensure that the attacker has up do date configuration, if not update the attacker.
			if (getTarget() == null) {
				if (target != null) {
					setTarget(null);
				}
			} else {
				if (target == null || !target.equals(getTarget())) {
					setTarget(getTarget());
				}
			}
			
			//Update local status
			setStatus(status, response.getData());
			
		} catch (APIException e) {
			setStatus(AttackerStatus.ERROR, e.getMessage());
		}
	}
	
	/**
	 * @param request
	 * @return APIMessage containing the response of the sent request.
	 * @throws IOException
	 * @throws APIException
	 * 
	 * Sends a serialized APIMessage object to the socket and expects a response from the remote.
	 */
	private synchronized APIMessage sendRequest(APIMessage request) throws IOException, APIException {
		//Avoid logging keep alive and time synchronization packets.
		boolean doNotLog = false;
		if (request.getMethod().equals(APIMethod.ATTACKER_QUERY_STATE) ||
				request.getMethod().equals(APIMethod.ATTACKER_START_SYNC) ||
				request.getMethod().equals(APIMethod.ATTACKER_SYNC_CYCLE) ||
				request.getMethod().equals(APIMethod.ATTACKER_SYNC_DELTA) ||
				request.getMethod().equals(APIMethod.ATTACKER_STOP_SYNC)
				) { doNotLog = true; }
		//Emulate a "retry" feature to the try catch block to resend the request if we encounter
		//"Broken Pipe" socket exception. The "Broken Pipe" error is unexpected and is thrown only
		//when Socket#write is called upon.
		int count = 0;
		int maxTries = 2;
		while(true) {
			try {
				//Establish connection
				initiateConnection();
				if (!doNotLog) log.debug("Sending request " + request.toString());
				//Send the request
				APIMessage.send(request, socket.getOutputStream());
				//Accept the response
				APIMessage response = APIMessage.get(socket.getInputStream());
				if (!doNotLog) log.debug("Recieved response " + response.toString());
				//Check if the response indicates a failure
				if (response.getMethod().equals(APIMethod.COORDINATOR_COMMAND_FAIL)) {
					throw new APIException(response.getData());
				}
				return response;
			} catch (SocketException e) {
				lastClockSync = new Date(0); //Reset last sync time.
				//Close the socket and retry if it's a broken pipe.
				if (e.getMessage().equals("Broken pipe")) {
					socket.close();
					initiateConnection();
					if (++count == maxTries) throw e;
				} else {
					throw e;
				}
			}
		}
		
	}
	
	/**
	 * @return boolean value indicating if clock sync should be performed now.
	 * 
	 * Checks if the current attacker is ready for a time sync by checking if the status is IDLE and
	 * it has been CLOCK_SYNC_INTERVAL seconds than the last sync.
	 */
	private boolean isClockSyncRequired() {
		return getStatus().equals(AttackerStatus.IDLE) &&
				(new Date().getTime() - lastClockSync.getTime() > CLOCK_SYNC_INTERVAL * 1000); 
	}
	
	/**
	 * @throws IOException
	 * 
	 * Establishes a new socket connection or attempts to reconnect if it's closed. Throws an exception 
	 * if the thread is marked for termination.
	 */
	private void initiateConnection() throws IOException {
		if (markedForTermination) throw new IOException("Thread is terminating.");
		if (socket == null || socket.isClosed() || !socket.isConnected()) {
			log.debug("Connecting to " + hostPort.toString());
			socket = new Socket(hostPort.getHost(), hostPort.getPort());	
		}				
	}
	
	/**
	 * @throws IOException
	 * 
	 * Closes the socket, markes the thread for termination and wait for the thread to die.
	 */
	public void terminate() throws IOException {
		markedForTermination = true;
		if (socket != null && !socket.isClosed()) socket.close();
		if (thread != null) while(thread.isAlive());
		markedForTermination = false;
	}

	public boolean equals(RemoteAttacker o) {
		return o.getHostPort().equals(this.getHostPort());
	}
	
	public String toString() {
		return hostPort.getHost() + ":" + hostPort.getPort();
	}



}
