package assignment2.attacker;

import java.util.Iterator;

import org.apache.logging.log4j.Logger;

import assignment2.util.AttackerStatus;

public class AttackerRunnerManager implements Runnable {
	Thread thread = null;
	Attacker attacker = null;
	boolean suspended = false;
	Logger log = null;

	public AttackerRunnerManager(Attacker attacker, Logger log) {
		this.attacker = attacker;
		this.log = log;
		this.thread = new Thread(this);
		this.thread.start();
	}
	
	@Override
	public void run() {
		while(true) {
			try { Thread.sleep(500); } catch (InterruptedException e) {};
			boolean someRunning = false;
			Iterator<AttackerRunner> iter = attacker.attackers.iterator();

			while (iter.hasNext()) {
			    AttackerRunner a = iter.next();
			    if (!a.thread.isAlive()) {
					log.debug("Removed AttackerRunner " + a.toString());
					iter.remove();
				} else {
					someRunning = true;
				}
			}
			if (!someRunning) {
				attacker.setStatus(AttackerStatus.IDLE);
			}
		}
	}

	void suspend() {
		suspended = true;
	}
	
	synchronized void resume() {
		suspended = false;
		notify();
	}
}
