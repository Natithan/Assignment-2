package client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class testaah {

	public static void main(String[] args) throws URISyntaxException {
		String message = "HEAD /pics/nursinggoat.jpg HTTP/1.1";
		String filePath = message.split("\\s")[2];
		System.out.println(filePath);
	}

}
