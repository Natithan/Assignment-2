package client; 

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jsoup.*;
import client.Command;

/* A class that implements a HTTP client, implementing
 * - GET
 * - HEAD
 * - PUT
 * - POST
 * methods
*/
public class MyHTTPClient {
	
	
	private static final String[] COMMANDS_VALUES = new String[] { "GET", "POST", "PUT", "HEAD" };
	public static final Set<String> COMMANDS = new HashSet<String>(Arrays.asList(COMMANDS_VALUES));
	

	// Main function, initiated by command line or console MyHTTPClient
	public static void main(String argv[]) throws Exception
	{
	
		 // Create a connection with the user
	    BufferedReader inFromUser = new BufferedReader( new
		InputStreamReader(System.in));
	    
	    String[] firstInput;
		// Get arguments from user.
	    // Command line arguments
		if ((argv != null)&& (argv.length != 0)){
			firstInput = argv;
		} else { //Otherwise, terminal input arguments
			String requestLineString = inFromUser.readLine();
			firstInput = requestLineString.split("\\s+");
		}
		
		// Check if first input is valid
		if (!isValidFirstInput(firstInput)) {
			throw new IOException("Invalid first input");
		}
		// All is well, name args, get domain from uri
		
		String HTTPCommand = firstInput[0];
		String requestURI = firstInput[1]; 
		int Port = Integer.parseInt(firstInput[2]);
		URI uri = new URI(requestURI);
	    String domain = uri.getHost();
		

	   
	    
		// Create a connection to the server
		Socket clientSocket = new Socket(domain, Port);
		DataOutputStream outToServer = new DataOutputStream(clientSocket .getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		//Based on HTTP command, go to the correct functionality
		if (HTTPCommand == "GET") {
			sendGETRequest(HTTPCommand, requestURI, Port);
		}
		if (HTTPCommand == "HEAD") {
			sendHEADRequest(HTTPCommand, requestURI, Port);
		}
		if (HTTPCommand == "PUT") {
			sendPOSTRequest(HTTPCommand, requestURI, Port);
		}
		if (HTTPCommand == "POST") {
			sendPUTRequest(HTTPCommand, requestURI, Port);
		}
		
		// 
		// Pass the argument on to the server in correct format
		String request = HTTPCommand + " " + requestURI + " " + "HTTP/1.1 " + "\r\n" + "Host:" + " " + domain;
		outToServer.writeBytes(request + "\r\n\r\n");
		
		System.out.println("Out to server: " + "\r\n" + request);
		
		//Display the servers response
		String response = inFromServer.readLine();
		System.out.println("FROM SERVER: " + response);
		
		
		
		// Close the connection
		clientSocket.close();
		
		

	
	}
	
	// Method that checks whether the input is valid
	private static boolean isValidFirstInput(String[] argv) {
		System.out.println(argv[2].matches("[0-9]*"));
		return // inexisting or wrong nb of args
				!((argv == null) || (argv.length != 3) ) &&
				// Wrong command
				COMMANDS.stream().anyMatch(COMMAND -> (COMMAND.equals(argv[0]))) &&
				// Wrong URI
				argv[1].matches("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]") &&
				// Wrong PORT
				argv[2].matches("[0-9]*");
	}
	
//	private static void myPut(String hTTPCommand, String uRI, int port) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	private static void myPost(String hTTPCommand, String uRI, int port) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	private static void myHead(String hTTPCommand, String uRI, int port) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	// Method that sends a GET-request, retrieves the entity identified by the Request-uri
//	private static void myGet(String hTTPCommand, String uRI, int port) {
//		// TODO Auto-generated method stub
//		
//	}
	

}
