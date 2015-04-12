package assignment2.util;

public class AttackerStatus {
	
	public static final AttackerStatus IDLE      = new AttackerStatus("IDLE");
	public static final AttackerStatus UNKNOWN   = new AttackerStatus("UNKNOWN");
	public static final AttackerStatus ERROR     = new AttackerStatus("ERROR");
	public static final AttackerStatus ATTACKING = new AttackerStatus("ATTACKING");
	
	private String status;
	private String message;
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message.replace(",", "");
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	public boolean equals(AttackerStatus other) {
		return other.getStatus().equals(this.getStatus());
	}
	
	public String toString() {
		return getStatus().toString();
	}
	
	public AttackerStatus(String status) {
		this(status, status);
	}
	
	public AttackerStatus(String status, String message) {
		this.status = status;
		setMessage(message);
	}
	

}
