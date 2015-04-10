package assignment2.util;

public class HostPort {

	private String host;
	private Integer port;
	
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	/**
	 * @param host the host to set
	 */
	private void setHost(String host) {
		this.host = host;
	}
	/**
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	private void setPort(Integer port) {
		this.port = port;
	}
	
	public boolean equals(HostPort o) {
		return o.getHost().equals(this.getHost()) && o.getPort().equals(this.getPort());
	}
	
	public String toString() {
		return getHost() + ":" + getPort();
	}
	
	
	public HostPort(String hostPortIn) {
		String[] hostPort = hostPortIn.split(":");
		if (hostPort.length != 2) throw new IllegalArgumentException("Argument must be in the format <host/ip>:<port>");
		if (hostPort[0].length() > 0) {
			setHost(hostPort[0]);	
		} else {
			throw new IllegalArgumentException("Host must be non-empty.");
		}
		try { setPort(Integer.parseInt(hostPort[1])); } catch (NumberFormatException e) {
			throw new IllegalArgumentException("Port must be numeric.");
		}
	}

}
