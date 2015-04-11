package assignment2.util;

public class PortExtractor {

	public static Integer extractPort(String port, Integer def) throws Exception {
		Integer parsed = null;
		try {
			parsed = Integer.parseInt(port);	
		} catch (NumberFormatException e) {
			parsed = def;
		}		
		if (parsed < 1000 && parsed > 65535) {
			throw new Exception("Invalid port number supplied.");
		}  
		return parsed;
	}
}
