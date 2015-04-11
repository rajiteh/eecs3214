package assignment2.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import org.apache.logging.log4j.Logger;

public class ServerWorker implements Runnable {
	
	Socket socket = null;
	Runnable timeKeeper = null;
	boolean markForTermination = false;
	Logger log = null;
	ServerListener serverListener = null;
	
	public ServerWorker(Socket socket, ServerListener listener, Logger log) {
		this(socket, listener, 10, log);
	}
	
	public ServerWorker(Socket socket, ServerListener listener, Integer keepAlive, Logger log) {
		this.socket = socket;
		this.log = log;
		this.serverListener = listener;
		this.timeKeeper = new Runnable() {
			ServerWorker worker = null;
			Integer keepAliveTimer = null;
			Runnable initialize(ServerWorker worker, Integer keepAlive) {
				this.worker = worker;
				this.keepAliveTimer = keepAlive;
				return this;
			}
			
			@Override
			public void run() {
				worker.log.debug("Worker timeout in " + keepAliveTimer + " seconds.");
				try { Thread.sleep(keepAliveTimer * 1000); } catch (InterruptedException e) {}
				try { worker.closeSocket(); } catch (IOException e) { e.printStackTrace(); }
			}
		}.initialize(this, keepAlive);	
	}
	

	@Override
	public void run() {
		Thread timer = new Thread(this.timeKeeper);
		timer.start();
		byte[] downloadFile = new byte[1024*10]; //10kb;
		try {
			while(!markForTermination) {
				new Random().nextBytes(downloadFile);
				ObjectOutputStream out =  new ObjectOutputStream(socket.getOutputStream());
				out.write(downloadFile);
				out.flush();
			}
		} catch (IOException e) {
			if (e.getMessage().equals("Socket closed")) {
				log.debug("Connection closed.");
			} else {
				e.printStackTrace();	
			}
		}
		timer.interrupt();
		int count = serverListener.workerCount.decrementAndGet();
		log.debug("Worker count " + count);
	}
	
	public void closeSocket() throws IOException {
		this.markForTermination = true;
		if (!socket.isClosed()) socket.close();
	}
	
	

}
