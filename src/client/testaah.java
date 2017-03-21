package client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class testaah {

	public static void main(String[] args) throws URISyntaxException {
		String requestURI = "http://www.themountaingoats.net/contact/blabla/.html";
		String scheme = requestURI.split("/")[0];
		String domain = requestURI.split("//")[1].split("/")[0];
		String baseURI = scheme + "//" + domain + "/";
		System.out.println(baseURI);
	}

}
