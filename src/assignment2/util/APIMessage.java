package assignment2.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;

public class APIMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6967606215290809993L;

	APIMethod method;

	String data;
	
	Long timeAtSend = null;
	Long timeAtRetrieve = null;

	/**
	 * @return the timeAtSend
	 */
	public Long getTimeAtSend() {
		return timeAtSend;
	}

	/**
	 * @param timeAtSend the timeAtSend to set
	 */
	public void setTimeAtSend(Long timeAtSend) {
		this.timeAtSend = timeAtSend;
	}

	/**
	 * @return the timeAtRetrieve
	 */
	public Long getTimeAtRetrieve() {
		return timeAtRetrieve;
	}

	/**
	 * @param timeAtRetrieve the timeAtRetrieve to set
	 */
	public void setTimeAtRetrieve(Long timeAtRetrieve) {
		this.timeAtRetrieve = timeAtRetrieve;
	}

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
		String timeAtSend = "";
		if (this.timeAtSend != null) {
			timeAtSend = this.timeAtSend + "";
		}
		String timeAtRetrieve = "";
		if (this.timeAtRetrieve != null) {
			timeAtRetrieve = this.timeAtRetrieve + "";
		}
		return "<SENT=" + timeAtSend + ", RETR=" + timeAtRetrieve +  " ACTION="+ getMethod().name() + 
				", DATA=" + this.getData() + ">";
	}
	
	
	public static APIMessage get(InputStream socketIn) throws IOException {
		try {
			APIMessage msg = (APIMessage) new ObjectInputStream(socketIn).readObject();
			msg.setTimeAtRetrieve(new Date().getTime());
			return msg;
		} catch (ClassNotFoundException e) {
			throw new IOException("Malformed response");
		}
		
	}
	
	public static  void send(APIMessage request, OutputStream socketOut) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(socketOut);
		request.setTimeAtSend(new Date().getTime());
		out.writeObject(request);
		out.flush();
	}
	

}
