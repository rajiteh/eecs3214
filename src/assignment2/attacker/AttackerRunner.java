package assignment2.attacker;


import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.logging.log4j.Logger; 

import assignment2.util.HostPort;

public class AttackerRunner implements Runnable {
	
	Thread thread = null;
	HostPort target = null;
	Logger log = null;
	Boolean markedForTermination = false;
	Date startAt = null;
	public AttackerRunner(HostPort target, Date when, Logger logger) {
		this.target = target;
		this.log = logger;
		this.startAt = when;
		this.thread = new Thread(this);
		this.thread.start();
	}

	@Override
	public void run() {
		log.info("Attacker initialized.");
		Socket socket = null;
		try {
			long timeout = startAt.getTime() - new Date().getTime();
			log.debug("Waiting for dispatch time.");
			if (timeout > 0) Thread.sleep(timeout); 
			log.info("Connecting to " + target.toString());
			socket = new Socket(target.getHost(), target.getPort());
			NullOutputStream out = new NullOutputStream();
			InputStream in = socket.getInputStream();
			IOUtils.copy(in, out);
		} catch (InterruptedException e) {} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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
