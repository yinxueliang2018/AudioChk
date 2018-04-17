package net.wyun.audio.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * A normalized response object for all RecognitionRequests
 * 
 * Map is hashed off of a set of values provided by the RecognitionInstance. 
 * Normalized values in ResponseKey enum.
 * 
 * @author bob
 *
 */
public class ReadResponse {
	Map<String, String> values;

	public ReadResponse() {
		values = new HashMap<String, String>();
	}
	
	public Map<String, String> getValues() {
		return values;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}
	
	public void addValue(String key, String value) {		
		values.put(key, value);
	}
	
}
