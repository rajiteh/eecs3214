package assignment1.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

public class ClickerRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int ACTION_AUTHENTICATE = 0x0a;
	public static final int ACTION_AUTHENTICATED = 0x0b;
	public static final int ACTION_QUESTIONS = 0x0c;
	public static final int ACTION_ANSWERS = 0x0a;
	
	public static final String DATA_AUTHENTICATED = "authenticated_true";
	public static final String DATA_NOT_AUTHENTICATED = "authenticated_false";
	
	
	public static final String[] CHOICE_HEADERS = { "a", "b", "c", "d", "e" };

	int action;
	String data;
	public ClickerRequest() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the action
	 */
	public int getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(int action) {
		this.action = action;
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
		Field[] fields = this.getClass().getFields();
		String action = "Undefined";
		try {
			for(int i=0;i<fields.length;i++){
				if (fields[i].getName().matches("ACTION_.+") && fields[i].getInt(this) == this.getAction()) {
					action = fields[i].getName();
					break;
				}	
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "<ACTION="+ action + ", DATA=" + this.getData() +">";
	}
	
	
	public static ClickerRequest get(InputStream socketIn) throws IOException {
		try {
			return (ClickerRequest) new ObjectInputStream(socketIn).readObject();	
		} catch (ClassNotFoundException e) {
			throw new IOException("Malformed response");
		}
		
	}
	
	public static  void send(ClickerRequest request, OutputStream socketOut) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(socketOut);
		out.writeObject(request);
		out.flush();
	}
	

}
