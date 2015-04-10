package assignment2.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class API {

	protected InputStream socketIn = null;	
	protected OutputStream socketOut = null;
	protected Socket socket = null;
	
	public API(Socket s) throws IOException {
		this.socket = s;
		this.socketIn = s.getInputStream();
		this.socketOut = s.getOutputStream();
	}
	
	abstract public void createSession() throws IOException; 


}
