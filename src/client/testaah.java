package client;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class testaah {

	public static void main(String[] args) throws URISyntaxException {
		String test = "HEAD http://localhost/blue.txt HTTP/1.1 ";
		System.out.println(test.matches("(GET|HEAD|POST|PUT)\\s+((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])\\s+(HTTP/1.(1|0))"));
		
		
		String requestLine = "GET http://localhost/blue.txt HTTP/1.1";
		System.out.println(requestLine.matches("[(GET)(HEAD)(POST)(PUT)]\\s+((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])\\s+(HTTP/1.(0-9){1})"));
		
	}

}
