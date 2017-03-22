package client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class testaah {

	public static void main(String[] args) throws URISyntaxException {
		String entity = "dsqjfmlqfjsd";
		String contentLengthHeader = "Content-Length: " + entity.length();
		System.out.println(contentLengthHeader);
	}

}
