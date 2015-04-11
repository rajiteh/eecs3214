package assignment2.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.logging.log4j.LogManager;

import assignment2.util.HostPort;

public class Server {
	ServerListener serverListener = null;
	HostPort listenAddress = null;
	
	
	
	
	public HostPort getListenAddress() {
		return listenAddress;
	}

	/**
	 * @param listenAddress the listenAddress to set
	 */
	public void setListenAddress(HostPort listenAddress) {
		this.listenAddress = listenAddress;
	}

	public Server(HostPort listenAddress) throws IOException {
		this.listenAddress = listenAddress;
	}

	public void startServer() throws IOException {
		stopServer();
		serverListener = new ServerListener(listenAddress, LogManager.getLogger("ServerListener"));
	}
	
	public void stopServer() throws IOException {
		if (serverListener != null && serverListener.isRunning()) {
			serverListener.stop();
		}
	}
	public void status() {
		System.out.println("Running\t:\t" + (serverListener != null ? serverListener.isRunning() : false));
		System.out.println("Workers\t:\t" + (serverListener != null ? serverListener.workerCount.get() : "N/A"));
	}
}
