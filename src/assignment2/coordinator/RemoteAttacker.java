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


public class RemoteAttacker {
	private static final int SYNC_CYCLES = 5;
	private static final int SYNC_DELTA_COUNT = 5;
	private static final int SYNC_DELTA_TIMEOUT = 3;
	private static final int CLOCK_SYNC_INTERVAL = 60;
	

	private AttackerStatus status = AttackerStatus.UNKNOWN;
	private String lastMessage = "";
	private HostPort target = null;
	private Thread thread = null;
	private Runnable keepAlive = null;
	private Socket socket = null;
	private Boolean markedForTermination = false;
	private Logger log = null;
	private Date lastUpdated = new Date(0);
	private Date lastClockSync = new Date(0);
	HostPort hostPort;
	
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
	
	public boolean equals(RemoteAttacker o) {
		return o.getHostPort().equals(this.getHostPort());
	}
	
	public String toString() {
		return hostPort.getHost() + ":" + hostPort.getPort();
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
	 * @param string 
	 */
	public void setStatus(AttackerStatus status, String string) {
		if (!(this.status.equals(status) || this.lastMessage.equals(string)))
			log.info("STATUS CHANGE : " + status.toString() + " MESSAGE : " + string);
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
	 * @throws IOException 
	 */
	public void setTarget(HostPort hostPort, boolean localOnly) throws IOException { 
		if (!localOnly) {
			APIMessage request = new APIMessage(hostPort.toString(), APIMethod.ATTACKER_SET_TARGET);
			try {
				sendRequest(request);
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

	public RemoteAttacker(HostPort hp, Logger log) throws IOException {
		this.hostPort = hp;
		this.log = log;
		this.keepAlive = new Runnable() {
			RemoteAttacker remoteAttacker = null;
			
			public Runnable initialize(RemoteAttacker ra) {
				this.remoteAttacker = ra;
				return this;
			}
			
			@Override
			public void run() {
				remoteAttacker.log.info("Starting keep alive thread.");
				if (markedForTermination) remoteAttacker.log.warn("Keep alive thread marked for termination.");
				
				while(!markedForTermination) {
					try {
						remoteAttacker.queryStatus();
						if (remoteAttacker.isClockSyncRequired()) {
							remoteAttacker.syncClock();
						} 
					} catch (IOException | APIException | InterruptedException e1) {
						remoteAttacker.setStatus(AttackerStatus.ERROR, e1.getMessage());
						e1.printStackTrace();
					}
					try { Thread.sleep(5000); } catch (InterruptedException e) { }
				}
				remoteAttacker.log.info("Keep alive thread done.");
			}
			
		}.initialize(this);
		
		startKeepAlive();
	}
	
	public void startKeepAlive() throws IOException {
		terminate();
		this.thread = new Thread(this.keepAlive);
		this.thread.start();
	}

	protected void syncClock() throws IOException, APIException, InterruptedException {
		sendRequest(new APIMessage("", APIMethod.ATTACKER_START_SYNC));
		queryStatus();
		for (int j=0; j < SYNC_CYCLES; j++) {
			sendRequest(new APIMessage(String.valueOf(j), APIMethod.ATTACKER_SYNC_CYCLE));
			for(int i=0; i < SYNC_DELTA_COUNT; i++ ) {
				sendRequest(new APIMessage(String.valueOf(i), APIMethod.ATTACKER_SYNC_DELTA));
			}
			Thread.sleep(SYNC_DELTA_TIMEOUT * 1000);	
		}
		sendRequest(new APIMessage(String.valueOf(new Date().getTime()), APIMethod.ATTACKER_STOP_SYNC));
		lastClockSync = new Date();	
		log.debug("Client clock synchronized.");
	}
	

	public void startAttack(Date when) throws Exception {
		if (when.before(new Date())) throw new Exception("Cannot travel back in time!");
		
		sendRequest(new APIMessage(String.valueOf(when.getTime()), APIMethod.ATTACKER_START_ATTACK));	
		queryStatus();	
	}
	
	public void stopAttack() throws Exception {
		sendRequest(new APIMessage("", APIMethod.ATTACKER_STOP_ATTACK));		
		queryStatus();	
	}

	public void queryStatus() throws IOException {
		APIMessage request = new APIMessage("", APIMethod.ATTACKER_QUERY_STATE);
		try {
			APIMessage response = sendRequest(request);
			
			String[] statusTarget = response.getData().split(",");
			if (statusTarget.length == 0 || statusTarget[0].length() < 1) {
				throw new APIException("Invalid response");
			}
			
			if (statusTarget.length == 2 && statusTarget[1].length() > 0) {
				setTarget(new HostPort(statusTarget[1]), true); 
			} else {
				setTarget(null, true);
			}
			
			setStatus(new AttackerStatus(statusTarget[0]), response.getData());
			
		} catch (APIException e) {
			setStatus(AttackerStatus.ERROR, e.getMessage());
			
		}
	}
	
	public synchronized APIMessage sendRequest(APIMessage request) throws IOException, APIException {
		boolean doNotLog = false;
		if (request.getMethod().equals(APIMethod.ATTACKER_QUERY_STATE) ||
				request.getMethod().equals(APIMethod.ATTACKER_START_SYNC) ||
				request.getMethod().equals(APIMethod.ATTACKER_SYNC_CYCLE) ||
				request.getMethod().equals(APIMethod.ATTACKER_SYNC_DELTA) ||
				request.getMethod().equals(APIMethod.ATTACKER_STOP_SYNC)
				) { doNotLog = true; }
		int count = 0;
		int maxTries = 2;
		while(true) {
			try {
				initiateConnection();
				if (!doNotLog) log.debug("Sending request " + request.toString());
				APIMessage.send(request, socket.getOutputStream());
				APIMessage response = APIMessage.get(socket.getInputStream());
				if (!doNotLog) log.debug("Recieved response " + response.toString());
				if (response.getMethod().equals(APIMethod.COORDINATOR_COMMAND_FAIL)) {
					throw new APIException(response.getData());
				}
				return response;
			} catch (SocketException e) {
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
	
	public boolean isClockSyncRequired() {
		return getStatus().equals(AttackerStatus.IDLE) &&
				(new Date().getTime() - lastClockSync.getTime() > CLOCK_SYNC_INTERVAL * 1000); 
	}
	
	public void initiateConnection() throws IOException {
		if (markedForTermination) throw new IOException("Thread is terminating.");
		if (socket == null || socket.isClosed() || !socket.isConnected()) {
			log.debug("Connecting to " + hostPort.toString());
			socket = new Socket(hostPort.getHost(), hostPort.getPort());	
		}				
	}
	
	public void terminate() throws IOException {
		markedForTermination = true;
		if (socket != null && !socket.isClosed()) socket.close();
		if (thread != null) while(thread.isAlive());
		markedForTermination = false;
	}




}
