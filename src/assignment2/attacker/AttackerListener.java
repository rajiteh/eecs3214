package assignment2.attacker;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.Logger; 

import assignment2.util.HostPort;

public class AttackerListener implements Runnable {
	Logger log = null;
	Attacker attacker = null;
	HostPort listenAddress = null;
	Thread thread = null;
	Boolean markedForTermination = false;
	ServerSocket serverSocket = null;
	
	public AttackerListener(Attacker attacker, HostPort listenAddress, Logger log) {
		this.attacker = attacker;
		this.listenAddress = listenAddress;
		this.log = log;
		this.thread = new Thread(this);
		this.thread.start();
	}
	

	public void run() {
		
		log.debug("Starting Attacker Listener.");
		try {
			serverSocket = new ServerSocket(listenAddress.getPort(), 
					50, InetAddress.getByName(listenAddress.getHost()));
			log.debug("Server bound to port " + serverSocket.getLocalPort());
			//Listen to connections as long not marked for termination.
			while (!markedForTermination) {
				try {
					Socket socket = serverSocket.accept();
					
					log.info(String.format("Coordinator connected. IP:%s PORT:%d", 
							socket.getInetAddress().toString(),
							socket.getPort()));
					
					AttackerAPI api = new AttackerAPI(attacker, socket, log);
					api.createSession();
				} catch (EOFException e) {
					log.warn("Coordinator disconnected");
					attacker.stopAttack(); 
				} catch (Exception e) {
					log.warn(e.toString());
					break;
				}
				//Resume listening to new connections.
			}
			attacker.stopAttack();
			log.info("Shutting down server.");
			if (serverSocket != null &&
					!serverSocket.isClosed()) {
				serverSocket.close();
			}
	
		} catch (IOException | IllegalArgumentException | SecurityException e1) {
			log.fatal(e1.toString());
		}
		//Server died. Clean up.
		markedForTermination = false;
	}
	
	public void stop() throws IOException {
		if (thread.isAlive()) {
			markedForTermination = true;
			log.debug("Waiting for termination.");
			serverSocket.close();
			while(thread.isAlive());
			log.info("Listener terminated.");
		}
		else {
			throw new IllegalStateException("Can't stop a non running server!");
		}
	}
	


	public boolean isRunning() {
		return thread.isAlive();
	}


	

}
