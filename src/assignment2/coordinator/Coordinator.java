package assignment2.coordinator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.apache.logging.log4j.Logger; 
import org.apache.logging.log4j.LogManager;

import dnl.utils.text.table.TextTable;
import assignment2.util.AttackerStatus;
import assignment2.util.HostPort;

public class Coordinator {

	/**
	 * List of attackers configured by the current coordinator.
	 */
	List<RemoteAttacker> attackers = new ArrayList<RemoteAttacker>();
	
	/**
	 * Logger for current coordinator. 
	 */
	Logger log = null;
	
	
	public Coordinator(Logger log) {
		this.log = log;
	}
	
	/**
	 * @param hostPort
	 * @throws Exception
	 * 
	 * Add a new attacker to the coordinator. Checks if the attacker already exists.
	 */
	public void addAttacker(HostPort hostPort) throws Exception {
		for(RemoteAttacker attacker : attackers) {
			if (attacker.getHostPort().equals(hostPort)) {
				throw new Exception("Attacker already exists.");		
			}
		}
			
		RemoteAttacker attacker = new RemoteAttacker(hostPort, LogManager.getLogger("Attacker(" + attackers.size() + ")"));
		log.info("Adding attacker " + hostPort.toString());
		attackers.add(attacker);
	}
	
	
	/**
	 * @param hostPort
	 * @throws Exception
	 * 
	 * Shutdown the connection and remove the attacker from current coordinator.
	 */
	public void removeAttacker(HostPort hostPort) throws Exception {
		for(RemoteAttacker attacker : attackers) {
			if (attacker.getHostPort().equals(hostPort)) {
				attacker.terminate();
				log.info("Removing attacker " + hostPort.toString());
				attackers.remove(attacker);
				return;		
			}
		}
		throw new Exception("Attacker does not exist.");
	}

	/**
	 * Prints a summary of the attackers connected.
	 */
	public void printAttackerStatus() {
		String[] columnNames = {                                       
                "Host",
                "Port",
                "Status",
                "Target",
                "Updated",
                "Last Message"
                };
		Object[][] data = new Object[attackers.size()][columnNames.length];
		ListIterator<RemoteAttacker> iter = attackers.listIterator();
		for(int i = 0; iter.hasNext(); i++) {
			RemoteAttacker attacker = iter.next();
			data[i][0] = attacker.getHostPort().getHost();
			data[i][1] = attacker.getHostPort().getPort();
			data[i][2] = attacker.getStatus();
			data[i][3] = attacker.getTarget();
			data[i][4] = attacker.getLastUpdated();
			data[i][5] = attacker.getLastMessage();
		}
		TextTable tt = new TextTable(columnNames, data);
		tt.setAddRowNumbering(true);
		tt.printTable();
		
	}

	public void updateTargetAll(HostPort target) throws IOException {
		ListIterator<RemoteAttacker> iter = attackers.listIterator();
		while(iter.hasNext()) setTarget(iter.next(), target);
	}
	
	public void setTarget(RemoteAttacker attacker ,HostPort target) throws IOException {
		attacker.setTarget(target);
	}
	
	public void initiateAttackAll(Date when) throws Exception {
		if (attackers.size() == 0) throw new Exception("No attackers to attack with.");
		for(RemoteAttacker attacker : attackers) {
			initiateAttack(attacker, when);	
		}
	}
	
	public void initiateAttack(RemoteAttacker attacker, Date when) throws Exception {
		if (attacker.getStatus().equals(AttackerStatus.IDLE)) {
			attacker.startAttack(when);
		} else {
			throw new Exception("Cannot start attack on " + attacker.getHostPort().toString() +
					" because status " + attacker.getStatus().toString());
		}
	}
	
	public void endAttackAll() throws Exception {
		if (attackers.size() == 0) throw new Exception("No attackers added.");
		for(RemoteAttacker attacker : attackers) {
			endAttack(attacker);
		}
	}
	
	public void endAttack(RemoteAttacker attacker) throws Exception {
		if (attacker.getStatus().equals(AttackerStatus.ATTACKING)) {
			attacker.stopAttack();
		} else {
			throw new Exception("Cannot stop attack on " + attacker.getHostPort().toString() +
					" because status " + attacker.getStatus().toString());
		}
		
	}

}
