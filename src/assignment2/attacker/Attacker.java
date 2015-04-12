package assignment2.attacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger; 
import org.apache.logging.log4j.LogManager;

import assignment2.util.AttackerStatus;
import assignment2.util.HostPort;

public class Attacker {
	
	/**
	 * Number of attackers to the connection. 
	 */
	final int ATTACKER_COUNT = 1;
	
	private Logger log;
		
	/**
	 * Listener instance to accept connections from a remote coordinator.
	 */
	AttackerListener listener = null;
	
	/**
	 * Address to listen to remote connections.
	 */
	HostPort listenAddress = null;
	
	/**
	 * Address of the target to attack to.
	 */
	HostPort targetAddress = null;
	
	/**
	 * Current status of the attacker.
	 */
	AttackerStatus status = null;
	
	/**
	 * Instance to manage the status of the attackers.
	 */
	AttackerRunnerManager attackerRunnerManager = null;
	
	/**
	 * List of attackers actively attacking a remote.
	 */
	List<AttackerRunner> attackers = new ArrayList<AttackerRunner>();
	
	public Attacker(HostPort hostPort, Logger log) {
		this.status = AttackerStatus.IDLE;
		this.listenAddress = hostPort;
		this.log = log;
		this.attackerRunnerManager = new AttackerRunnerManager(this, this.log);
	}
	/**
	 * @return the targetAddress
	 * @throws Exception 
	 */
	public HostPort getTargetAddress() throws Exception {
		if (targetAddress == null) throw new Exception("Target address is not defined.");
		return targetAddress;
	}


	/**
	 * @param targetAddress the targetAddress to set
	 */
	public void setTargetAddress(HostPort targetAddress) {
		this.targetAddress = targetAddress;
	}
	
	/**
	 * @return the status
	 */
	public AttackerStatus getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(AttackerStatus status) {
		this.status = status;
	}
	
	/**
	 * @return the listenAddress
	 * @throws Exception 
	 */
	public HostPort getListenAddress() throws Exception {
		if (listenAddress == null) throw new Exception("Listener address is not defined.");
		return listenAddress;
	}

	/**
	 * @param listenAddress the listenAddress to set
	 */
	public void setListenAddress(HostPort listenAddress) {
		this.listenAddress = listenAddress;
	}

	public boolean isListening() {
		return this.listener != null && this.listener.isRunning();
	}
	
	public void startListener(boolean b) throws Exception {
		if (b && isListening()) {
			stopListener();
			startListener();
		} else {
			startListener();
		}	
	}
	
	/**
	 * @throws Exception
	 * 
	 * Starts a listener instance to accept commands from a remote.
	 */
	public void startListener() throws Exception {
		//Check if we are already running
		if (isListening()) {
			log.error("Request to start the server while the server is already started.");
			throw new IllegalStateException("Server is already running. Stop the server before trying to restart it.");
		}
		
		this.listener = new AttackerListener(this, getListenAddress(), LogManager.getLogger("AttackerListener"));
		 
	}
	

	/**
	 * Stops a running server
	 * @return
	 * @throws IOException
	 */
	public void stopListener() throws IOException {
		log.info("Trying to stop listener");
		this.listener.stop();
	}
	
	
	/**
	 * @param when time of the attack
	 * @throws Exception
	 * 
	 * Starts an attack on the target at the specified time.
	 */
	public void startAttack(Date when) throws Exception {
		if (attackers.size() > 0) throw new IllegalStateException("Attackers are active. Stop them.");
		for(int i = 0; i < ATTACKER_COUNT; i++){
			attackers.add(new AttackerRunner(getTargetAddress(), when, this, LogManager.getLogger("AttackerRunner("+ i +")")));
		}
		setStatus(AttackerStatus.ATTACKING);
	}
	
	/**
	 * Stop an ongoing attack.
	 */
	public void stopAttack() {
		for(AttackerRunner attacker : attackers) {
			attacker.stop();
		}
		setStatus(AttackerStatus.IDLE);
		attackers.clear();
	}

	/**
	 * Prints status of current attacker. 
	 */
	public void status() {
		System.out.println("Bound\t:\t" + listenAddress.toString());
		System.out.println("Listen\t:\t" + isListening());
		System.out.println("Workers\t:\t" + attackers.size());
		System.out.println("Status\t:\t" + status.toString());
		System.out.println("Target\t:\t" + (targetAddress != null ? targetAddress.toString() : "") );
	}	
	
}
