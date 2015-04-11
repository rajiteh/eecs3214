package assignment2.util;

public class PortExtractor {

	public static Integer extractPort(String port) throws Exception {
		Integer parsed = null;
		parsed = Integer.parseInt(port);	
				
		if (parsed < 1000 && parsed > 65535) {
			throw new Exception("Invalid port number supplied.");
		}  
		return parsed;
	}
}
