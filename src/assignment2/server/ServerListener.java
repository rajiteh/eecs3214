package assignment2.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import assignment2.util.HostPort;

public class ServerListener implements Runnable {
	Thread thread = null;
	ServerSocket serverSocket = null;
	HostPort listenAddress = null;
	Boolean markedForTermination  = false;
	Logger log = null;
	AtomicInteger workerCount = null;

	
	/**
	 * @return the listenAddress
	 */
	public HostPort getListenAddress() {
		return listenAddress;
	}

	/**
	 * @param listenAddress the listenAddress to set
	 */
	public void setListenAddress(HostPort listenAddress) {
		this.listenAddress = listenAddress;
	}

	public ServerListener(HostPort listener, Logger log) {
		this.listenAddress = listener;
		this.log = log;
		this.workerCount = new AtomicInteger();
		this.thread = new Thread(this);
		this.thread.start();
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(listenAddress.getPort(), 
					50, InetAddress.getByName(listenAddress.getHost()));
			log.debug("Server bound to port " + serverSocket.getLocalPort());
			//Listen to connections as long not marked for termination.
			while (!markedForTermination) {
				try {
					Socket clientSocket  = serverSocket.accept();
					log.info(String.format("Connected. IP:%s PORT:%d", 
							clientSocket.getInetAddress().toString(),
							clientSocket.getPort()));
					//Creates a new thread that executes the accepted client connection
					int count = workerCount.incrementAndGet();
					new Thread(new ServerWorker(clientSocket, this, LogManager.getLogger("ServerWorker"))).start();
					log.debug("Worker count " + count);
				} catch (Exception e) {
					e.printStackTrace();
					log.error(e.toString());
					break;
				}
			}
			log.info("Shutting down server.");
			if (serverSocket != null &&
					!serverSocket.isClosed()) {
				serverSocket.close();
			}
	
		} catch (Exception e) {
			log.fatal(e.toString());
		}
		
	}
	
	public void stop() throws IOException {
		if (thread.isAlive()) {
			markedForTermination = true;
			log.debug("Waiting for termination.");
			serverSocket.close();
			while(thread.isAlive());
			log.info("Listener terminated.");
			markedForTermination = false;
		}
		else {
			throw new IllegalStateException("Can't stop a non running server!");
		}
	}
	


	public boolean isRunning() {
		return thread.isAlive();
	}


}
