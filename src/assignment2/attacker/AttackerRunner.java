package assignment2.attacker;


import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.logging.log4j.Logger; 

import assignment2.util.AttackerStatus;
import assignment2.util.HostPort;

public class AttackerRunner implements Runnable {
	
	Thread thread = null;
	HostPort target = null;
	Logger log = null;
	Boolean markedForTermination = false;
	Date startAt = null;
	Attacker attacker = null;
	
	public AttackerRunner(HostPort target, Date when, Attacker attacker, Logger logger) {
		this.attacker = attacker;
		this.target = target;
		this.log = logger;
		this.startAt = when;
		this.thread = new Thread(this);
		this.thread.start();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 * 
	 * Sleeps till the attack time and initiates a connection to the target.
	 */
	@Override
	public void run() {
		Socket socket = null;
		try {
			long timeout = startAt.getTime() - new Date().getTime();
			log.info("Waiting for dispatch time.");
			if (timeout > 0) Thread.sleep(timeout); 
			socket = new Socket(target.getHost(), target.getPort());
			log.info("Connected to " + target.toString());
			NullOutputStream out = new NullOutputStream();
			InputStream in = socket.getInputStream();
			IOUtils.copy(in, out);
		} catch (InterruptedException e) {} catch (IOException e ) {
			AttackerStatus err = AttackerStatus.ERROR;
			err.setMessage(e.getMessage());
			attacker.setStatus(err);
			log.error(e.getMessage());
		}
		try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException e) { }
		markedForTermination = false;
		log.info("Attacker terminated.");
	}

	public void stop() {
		if (thread.isAlive()) {
			markedForTermination = true;
			thread.interrupt();
			log.debug("Waiting for termination.");
			while(thread.isAlive());
		}		
	}

}
