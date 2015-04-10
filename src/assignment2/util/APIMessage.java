package assignment2.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class APIMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6967606215290809993L;

	APIMethod method;

	String data;

	public APIMessage(String data, APIMethod method) {
		this.data = data;
		this.method = method;
	}

	/**
	 * @return the method
	 */
	public APIMethod getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(APIMethod method) {
		this.method = method;
	}
	
	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(String data) {
		this.data = data;
	}
	
	public String toString() {
		
		return "<ACTION="+ getMethod().name() + ", DATA=" + this.getData() +">";
	}
	
	
	public static APIMessage get(InputStream socketIn) throws IOException {
		try {
			return (APIMessage) new ObjectInputStream(socketIn).readObject();	
		} catch (ClassNotFoundException e) {
			throw new IOException("Malformed response");
		}
		
	}
	
	public static  void send(APIMessage request, OutputStream socketOut) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(socketOut);
		out.writeObject(request);
		out.flush();
	}
	

}
