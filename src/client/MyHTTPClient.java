package client; 

import java.io.*;
import java.net.*;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
		String domain = requestURI.split("//")[1].split("/")[0];
		

	   
	    
		// Create a connection to the server
		Socket clientSocket = new Socket(domain, Port);
		DataOutputStream outToServer = new DataOutputStream(clientSocket .getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		//Based on HTTP command, go to the correct functionality
		if (HTTPCommand.equals("GET")) {
			// send the get request
			sendGETRequest(HTTPCommand, requestURI, Port, outToServer, domain);
			// Handle the response: display on terminal, store in file
			handleGETResponse(outToServer, inFromServer, requestURI);
		}
		else if (HTTPCommand.equals("HEAD")) {
			sendHEADRequest(HTTPCommand, requestURI, Port, outToServer, domain);
			// Handle the response: display on terminal, store in file
			handleHEADResponse(outToServer, inFromServer, requestURI);
		}
//		if (HTTPCommand == "PUT") {
//			sendPOSTRequest(HTTPCommand, requestURI, Port);
//		}
//		if (HTTPCommand == "POST") {
//			sendPUTRequest(HTTPCommand, requestURI, Port);
//		}
//		
		// 
			
		
		
		// Close the connection
		clientSocket.close();
		
		

	
	}
	// Prints out the server response to the terminal and stores it in a .txt file
	private static void handleHEADResponse(DataOutputStream outToServer, BufferedReader inFromServer,
			String requestURI) throws IOException {
		// Create proper name for responseFile
				String filename = requestURI.replace("/", "-");
				filename = filename.replace(":", "_");
				
				System.out.println(filename);
				File responseFile = new File("C:\\Users\\Nathan\\Desktop\\" + filename + ".txt");
				
				responseFile.createNewFile();
				System.out.println(responseFile);
				
				FileWriter fw = new FileWriter(responseFile.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				
				// Deal with the response line
				handleResponseLine(inFromServer, bw);
				// Deal with the headers: print out and write away
				HashMap<String, String> headersMap = handleHeaders(inFromServer, bw);
				
				bw.close();
				return;
		
	}
	// Prints out and stores the ResponseLine
	private static void handleResponseLine(BufferedReader inFromServer, BufferedWriter bw) throws IOException {
		String serverResponseLine = inFromServer.readLine();
		System.out.println(serverResponseLine);
		bw.write(serverResponseLine);
		bw.newLine();
		
	}
	// Method that sends a GET request to the server in the correct format
	private static void sendHEADRequest(String hTTPCommand, String requestURI, int port, DataOutputStream outToServer,
			String domain) throws IOException {
		
		// Deal with common stuff in separate method
		sendRequestLineAndHostHeader(hTTPCommand, requestURI, port, outToServer, domain);
		// Nothing but common stuff here
		return;	
				
	}

	private static void sendRequestLineAndHostHeader(String hTTPCommand, String requestURI, int port, DataOutputStream outToServer,
			String domain) throws IOException {
		String request = hTTPCommand + " " + requestURI + " " + "HTTP/1.1 " + "\r\n" + "Host:" + " " + domain;
		outToServer.writeBytes(request + "\r\n\r\n");
				
		System.out.println("Out to server: " + "\r\n" + request);
		return;
		
	}

	// Prints out the server response to the terminal and stores it in a .txt file
	private static void handleGETResponse(DataOutputStream outToServer, BufferedReader inFromServer, String requestURI) throws Exception {
		
		// Create proper name for responseFile
		String filename = requestURI.replace("/", "-");
		filename = filename.replace(":", "_");
		File responseFile = new File("C:\\Users\\Nathan\\Desktop\\" + filename + "." + "html"); // Create html file on the desktop
		responseFile.createNewFile();
				
		// Ready to write
		FileWriter fw = new FileWriter(responseFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		// Deal with the response line
		handleResponseLine(inFromServer, bw);
		
		// Deal with the headers: print out and write away, and return a map containing key: header name and value: header value pairs
		HashMap<String, String> headersMap = handleHeaders(inFromServer, bw);
		
		// Only deal with websites using content-length
		if (!headersMap.containsKey("Content-Length:")){
			throw new Exception("Can only deal with websites using content-length header");
		}
		int counter = Integer.parseInt(headersMap.get("Content-Length:"));
		
		// Always write complete response to an html file and terminal
		// Store text in file and write to terminal. Make a string containing the message body for later use as well
		String messageBody = "";
				while (counter > 0){
					String serverBodyLine = inFromServer.readLine();
					counter -= (serverBodyLine.length() + 1); // adjust counter
					
					messageBody = messageBody + serverBodyLine;
//					System.out.println(messageBody);
					// TODO Decide whether extracting body in string is worth it
					System.out.println(serverBodyLine);
					bw.write(serverBodyLine);
					bw.newLine();
				}
				bw.close();
				
		// Take the appropriate action based on content-type: for now only text and images are supported
		String contentType = headersMap.get("Content-Type:");
		String type = contentType.split("/")[0];
		String subType = contentType.split("/")[1];
		
		// Handle appropriately according to type
		if (type.equals("text") ){
			handleText(responseFile, requestURI, subType, outToServer, inFromServer);
		} else if (type.equals("image")){
			handleImage(responseFile, requestURI, subType);
		}
		// Past the headers, should have a proper counter now, start dealing with message body
		
		
		//TODO Check for embedded stuff

		
		
		// 
		
		return;
		
		
		
	}
	// Stores the image in an appropriate file, based on the html file representing the server's answer
	private static void handleImage(File responseFile, String requestURI, String subType) throws IOException {
	
		//  Create imageFile
		// Create proper name for responseFile
		String filename = requestURI.replace("/", "-");
		filename = filename.replace(":", "_");
		File imageFile = new File("C:\\Users\\Nathan\\Desktop\\" + filename + "." + subType); // Create image file on the desktop
		imageFile.createNewFile();
		
		// Establish streams
		FileOutputStream fos = new FileOutputStream(responseFile);
		FileInputStream fis = new FileInputStream(imageFile);
			
		// skip headers
		
		
	}
	// Handles message body of type text appropriately
	private static void handleText(File responseFile, String requestURI, String subType, DataOutputStream outToServer, BufferedReader inFromServer) throws Exception {
		

		// For now only html subtype has a specialised handling
		if (subType.equals("html")) {
			handleHtml(responseFile, requestURI, outToServer, inFromServer);
		}
		else {
			return;
		}
	}
	
	// Method that checks for embedded images and stores those, still with the same connection
	private static void handleHtml(File responseFile, String requestURI, DataOutputStream outToServer, BufferedReader inFromServer) throws Exception{
		String scheme = requestURI.split("/")[0];
		String domain = requestURI.split("//")[1].split("/")[0];
		String baseURI = scheme + "//" + domain + "/";
		
		
		Document doc = Jsoup.parse(responseFile, "UTF-8", baseURI);
		Elements imageLinks = doc.getElementsByTag("img");
		System.out.println("imageLinks:" + imageLinks);
		
		// for each of the images, apply GET command
		for (Element imageLink: imageLinks){
			String link = imageLink.attr("src");
			System.out.println(link);
			handleGETResponse(outToServer, inFromServer, requestURI);
			
		}
			// TODO Finish up image retrieval
		
	}
	private static HashMap<String, String> handleHeaders(BufferedReader inFromServer, BufferedWriter bw) throws IOException {
		
		HashMap<String, String> headersMap = new HashMap<>();
		// Keep going until at first CRLF
		while (true) {
				String serverHeaderSentence = inFromServer.readLine();
				// Check if we're at empty line
				if (serverHeaderSentence.isEmpty()){
					// Write that empty line
					System.out.println(serverHeaderSentence);
					bw.write(serverHeaderSentence);
					bw.newLine();
					break;
				}; // Stop if at empty line
				
				// Get info
				String name = serverHeaderSentence.split("\\s+")[0];
				String value = serverHeaderSentence.split("\\s+", 2)[1];
				headersMap.put(name, value);
				
//				if (serverHeaderSentence.startsWith("Content-Length:")){
//					counter = Integer.parseInt(serverHeaderSentence.split("\\s+")[1]); // Get value from content-length header
//				}
				
				System.out.println(serverHeaderSentence);
				bw.write(serverHeaderSentence);
				bw.newLine();
		}
		
		return headersMap;
	}

	// Method that sends a GET request to the server in the correct format
	private static void sendGETRequest(String hTTPCommand, String requestURI, int port, DataOutputStream outToServer, String domain) throws IOException {
		// Deal with common stuff in separate method
				sendRequestLineAndHostHeader(hTTPCommand, requestURI, port, outToServer, domain);
				// Nothing but common stuff here
				return;	
	}

	// Method that checks whether the input is valid
	private static boolean isValidFirstInput(String[] argv) {

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
