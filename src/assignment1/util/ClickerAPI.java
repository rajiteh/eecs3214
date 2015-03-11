package assignment1.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class ClickerAPI {

	protected InputStream socketIn = null;	
	protected OutputStream socketOut = null;
	
	
	public ClickerAPI(InputStream inputStream,
			OutputStream outputStream) {
		this.socketIn = inputStream;
		this.socketOut = outputStream;
	}
	
	abstract public void createSession() throws IOException; 

	abstract protected boolean authenticate() throws IOException;

}
