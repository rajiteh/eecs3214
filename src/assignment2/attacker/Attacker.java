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
	
	private Logger log;
	
	final int ATTACKER_COUNT = 1;
	
	AttackerListener listener = null;
	
	HostPort listenAddress = null;
	
	HostPort targetAddress = null;
	
	AttackerStatus status = null;
	
	AttackerRunnerManager attackerRunnerManager = null;
	
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
	
	public void startListener() throws Exception {
		//Check if we are already running
		if (isListening()) {
			log.error("Request to start the server while the server is already started.");
			throw new IllegalStateException("Server is already running. Stop the server before trying to restart it.");
		}
		
		this.listener = new AttackerListener(this, getListenAddress(), LogManager.getLogger("AttackerListener"));
		
		log.info("Server dispatched."); 
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
	
	public void startAttack(Date when) throws Exception {
		
		if (attackers.size() > 0) throw new IllegalStateException("Attackers are active. Stop them.");
		for(int i = 0; i < ATTACKER_COUNT; i++){
			attackers.add(new AttackerRunner(getTargetAddress(), when, LogManager.getLogger("AttackerRunner("+ i +")")));
		}
		setStatus(AttackerStatus.ATTACKING);
	}
	
	public void stopAttack() {
		for(AttackerRunner attacker : attackers) {
			attacker.stop();
		}
		setStatus(AttackerStatus.IDLE);
		attackers.clear();
	}
	
	public synchronized void stopAttackerRunner(AttackerRunner attacker) {
		attackers.remove(attacker);
	}


	public void status() {
		System.out.println("Bound\t:\t" + listenAddress.toString());
		System.out.println("Listen\t:\t" + isListening());
		System.out.println("Workers\t:\t" + attackers.size());
		System.out.println("Status\t:\t" + status.toString());
		System.out.println("Target\t:\t" + (targetAddress != null ? targetAddress.toString() : "") );
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
	
	
}
