package client;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.commons.io.IOUtils;

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
		OutputStream outToServerStream = clientSocket.getOutputStream();
		InputStream inStream = clientSocket.getInputStream();
		
		DataOutputStream outToServer = new DataOutputStream(outToServerStream);
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(inStream));
		
		//Based on HTTP command, go to the correct functionality
		if (HTTPCommand.equals("GET")) {
			// send the get request
			sendGETRequest(HTTPCommand, requestURI, Port, outToServer, domain);
			// Handle the response: display on terminal, store in file, get embedded files if necessary. Any new GET requests are sent with the same outputstream.
			handleGETResponse(outToServerStream, inStream, requestURI, clientSocket);
		}
		else if (HTTPCommand.equals("HEAD")) {
			sendHEADRequest(HTTPCommand, requestURI, Port, outToServer, domain);
			// Handle the response: display on terminal, store in file
			handleHEADResponse(outToServer, inFromServer, requestURI);
		}
		if (HTTPCommand.equals("PUT") || HTTPCommand.equals("POST")) {
			sendPUTorPOSTRequest(HTTPCommand, requestURI, Port, clientSocket, outToServer);
			handlePUTorPOSTResponse(outToServerStream, inStream, requestURI, clientSocket);
		}
//		if (HTTPCommand.equals("POST")) {
//			sendPOSTRequest(HTTPCommand, requestURI, Port);
//		}
		
		 
			
		
		
		// Close the connection
		clientSocket.close();
		
		

	
	}
	// Prints the servers response to the terminal and stores it in a file
	private static void handlePUTorPOSTResponse(OutputStream outToServerStream, InputStream inStream, String requestURI,
			Socket clientSocket) throws Exception {
		// Create proper name for responseFile
			String filename = requestURI.replace("/", "-");
			filename = filename.replace(":", "_");
			File responseFile = new File("C:\\Users\\Nathan\\Desktop\\" + filename + "." + "html"); // Create html file on the desktop
			responseFile.createNewFile();
					
			// Ready to write
			FileWriter fw = new FileWriter(responseFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			// Ready to read text
			BufferedReader textInFromServer = new BufferedReader(new InputStreamReader(inStream));
			// Deal with the response line
			// Deal with the response line
			String[] responseLine = handleResponseLine(textInFromServer, bw);
			String protocolversion = responseLine[0];
			String statusCode = responseLine[1];
			String phrase = responseLine[2];
			
			// based on response, do correct stuff
			if (statusCode.startsWith("1")) {
				handleResponseLine(textInFromServer, bw);
				// If 200, go right on
			} else if (statusCode.startsWith("2")) {
					// Deal with the headers: print out and write away, and return a map containing key: header name and value: header value pairs
					HashMap<String, String> headersMap = handleHeaders(textInFromServer, bw);
					
					// Only deal with websites using content-length
					if (!headersMap.containsKey("Content-Length:")){
						throw new Exception("Can only deal with websites using content-length header");
					}
					int counter = Integer.parseInt(headersMap.get("Content-Length:"));
					
					// Always write complete response to an html file and terminal
					// Store text in file and write to terminal.
					
					handleBody(counter, textInFromServer, bw);
		
							
					// Take the appropriate action based on content-type: for now only text and images are supported
					String contentType = headersMap.get("Content-Type:");
					String type = contentType.split("/")[0];
					String subType = contentType.split("/")[1];
					
					// Handle appropriately according to type. No longer give bufferedStream as argument, since it needn't be sequential anymore.
					if (type.equals("text") ){
						handleText(responseFile, requestURI, subType);
					// Past the headers, should have a proper counter now, start dealing with message body
					}
			
			}else {
				throw new Exception("Can't deal with this yet: " + statusCode +" "+ phrase);
			}
			
			
			// 
			
			return;
		
	}
	// Sends a put request
	private static void sendPUTorPOSTRequest(String HTTPCommand, String requestURI, int port, Socket clientSocket, DataOutputStream outToServer) throws IOException {
		
		// parse the requestURI
		String scheme = requestURI.split("://")[0];
		String domain = requestURI.split("//")[1].split("/")[0];
		String baseURI = scheme + "://" + domain + "/";
		String path = requestURI.replace(baseURI, "");
		
		//Get additional user input as string
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		// Initiate entity string
		String entity = "";
		System.out.println("Enter data: ");
		
		// Keep on reading until empty line
		boolean noMoreLines = false;
		while (!noMoreLines){
			String line = br.readLine();
			if (line.length() == 0){
				noMoreLines = true;
				
			} else {
				entity += URLEncoder.encode(line, "UTF-8");
			}	
		}
		
		// Data collected, now send request to server
		
		String requestLineAndHostHeader = HTTPCommand + " " + "/" + path + " " + "HTTP/1.1 " + "\r\n" + "Host:" + " " + domain + "\r\n";
		
		String contentLengthHeader = "Content-Length: " + entity.length() + "\r\n\r\n";
		String request = requestLineAndHostHeader + contentLengthHeader + entity;
		System.out.println(request);
		outToServer.writeBytes(request + "\r\n\r\n");
		
		
	}
	
	
	// Prints out the server response to the terminal and stores it in a .txt file
	private static void handleHEADResponse(DataOutputStream outToServer, BufferedReader inFromServer,
			String requestURI) throws Exception {
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
	// Prints out and stores the ResponseLine, and returns the protocol version, status code and its associated textual phrase
	private static String[] handleResponseLine(BufferedReader textInFromServer, BufferedWriter bw) throws Exception {
		String serverResponseLine = textInFromServer.readLine();
		System.out.println(serverResponseLine);
		String protocolVersion = serverResponseLine.split("\\s+")[0];
		String statusCode = serverResponseLine.split("\\s+")[1];
		String phrase = serverResponseLine.split("\\s+", 3)[2];
		

		System.out.println(serverResponseLine);
		bw.write(serverResponseLine);
		bw.newLine();
		return new String[]{protocolVersion, statusCode, phrase};
		
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
	private static void handleGETResponse(OutputStream outStream, InputStream inStream, String requestURI, Socket clientSocket) throws Exception {
		
		// Create proper name for responseFile
		String filename = requestURI.replace("/", "-");
		filename = filename.replace(":", "_");
		File responseFile = new File("C:\\Users\\Nathan\\Desktop\\" + filename + "." + "html"); // Create html file on the desktop
		responseFile.createNewFile();
				
		// Ready to write
		FileWriter fw = new FileWriter(responseFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		// Ready to read text
		BufferedReader textInFromServer = new BufferedReader(new InputStreamReader(inStream));
		// Deal with the response line
		String[] responseLine = handleResponseLine(textInFromServer, bw);
		String protocolversion = responseLine[0];
		String statusCode = responseLine[1];
		String phrase = responseLine[2];
		
		// based on response, do correct stuff
		if (statusCode.startsWith("1")) {
			handleResponseLine(textInFromServer, bw);
			// If 200 or 300, go right on
			} else  {
			
				// Deal with the headers: print out and write away, and return a map containing key: header name and value: header value pairs
				HashMap<String, String> headersMap = handleHeaders(textInFromServer, bw);
				
				// Only deal with websites using content-length
				if (!headersMap.containsKey("Content-Length:")){
					throw new Exception("Can only deal with websites using content-length header");
				}
				int counter = Integer.parseInt(headersMap.get("Content-Length:"));
				
				// Always write complete response to an html file and terminal
				// Store text in file and write to terminal.
				handleBody(counter, textInFromServer, bw);
		
				
				if (headersMap.containsKey("Content-Type:")){
				// Take the appropriate action based on content-type: for now only text and images are supported
				String contentType = headersMap.get("Content-Type:");
				String type = contentType.split("/")[0];
				String subType = contentType.split("/")[1].split(";")[0];
				
				// Handle appropriately according to type. No longer give bufferedStream as argument, since it needn't be sequential anymore.
				if (type.equals("text") ){
					handleText(responseFile, requestURI, subType);
				// Past the headers, should have a proper counter now, start dealing with message body
				}
				} else if (!headersMap.get("Content-Length:").equals("0")){ // No content type, assume text/html
					handleText(responseFile, requestURI, "html");
				}
			
			}
		
		return;
		
		
		
	}

	// Method that writes the body from the server response to the same html file as the header, prints it to the terminal
	private static void handleBody(int counter, BufferedReader textInFromServer, BufferedWriter bw) throws IOException {
		
		
		while (counter > 0){
			if (textInFromServer.ready()){
			String serverBodyLine = textInFromServer.readLine();
			counter -= (serverBodyLine.length() + 1); // adjust counter
			bw.write(serverBodyLine + "\r\n");
			bw.newLine();
			}
			else {
				break;
				
			}
		}
		bw.close();
	}

	// Handles message body of type text appropriately
	private static void handleText(File responseFile, String requestURI, String subType) throws Exception {
		

		// For now only html subtype has a specialised handling
		if (subType.equals("html")) {
			handleHtml(responseFile, requestURI, subType);
		}
		else {
			return;
		}
		return;
	}
	
	// Method that checks for embedded images and stores those, still with the same connection
	private static void handleHtml(File responseFile, String requestURI, String subType) throws Exception{
		String scheme = requestURI.split("/")[0];
		String domain = requestURI.split("//")[1].split("/")[0];
		String baseURI = scheme + "//" + domain + "/";
		
		
		Document doc = Jsoup.parse(responseFile, "UTF-8", baseURI);
		Elements imageLinks = doc.getElementsByTag("img");
		System.out.println("imageLinks:" + imageLinks);
		
		// for each of the images, retrieve them in a similar fashion to the handleGETResponse method
		for (Element imageLink: imageLinks){
			
			
			// Create new connection to server for every GET-request
			Socket clientSocket = new Socket(domain, 80);
			OutputStream outStream = clientSocket.getOutputStream();
			BufferedInputStream inStream = new BufferedInputStream(clientSocket.getInputStream());
			
			
			//Extract path
			String uri = imageLink.attr("src");
			String path = uri.replace(baseURI, "");
			
			// Send request
			DataOutputStream dataOutStream = new DataOutputStream(outStream);
			String request = "GET" + " " + uri + " " + "HTTP/1.1 " + "\r\n" + "Host:" + " " + domain; // Can send complete uri?
			dataOutStream.writeBytes(request + "\r\n\r\n");
			
			System.out.println(inStream.markSupported());
			inStream.mark(0);
			
			
			
			System.out.println("Out to server imageRequest: " + "\r\n" + request);
			// Ready to read text
			BufferedReader textInFromServer = new BufferedReader( new InputStreamReader(inStream) );
			
			// Deal with status line
			String statusLine = textInFromServer.readLine();
			
			
			
			// Deal with the headers: print out and write away, and return a map containing key: header name and value: header value pairs
			HashMap<String, String> headersMap = new HashMap<>();
			// Keep going until at first CRLF
			while (true) {

					String serverHeaderSentence = textInFromServer.readLine();
					System.out.println(serverHeaderSentence);
					// Check if we're at empty line
					if (serverHeaderSentence.isEmpty()){
						break;
					}; // Stop if at empty line
					
					// Get info
					String name = serverHeaderSentence.split("\\s+")[0];
					String value = serverHeaderSentence.split("\\s+", 2)[1];
					headersMap.put(name, value);
			}
			
		
			
			
			// Only deal with websites using content-length
			if (!headersMap.containsKey("Content-Length:")){
				throw new Exception("Can only deal with websites using content-length header");
			}
			int counter = Integer.parseInt(headersMap.get("Content-Length:"));
			
			
			// Take the appropriate action based on content-type: for now only text and images are supported
			String contentType = headersMap.get("Content-Type:");
			String type = contentType.split("/")[0];
			String imageSubType = contentType.split("/")[1];
			
			// Create proper name for imageFile
			String filename = uri.replace("/", "-");
			filename = filename.replace(":", "_");
			File imageFile = new File("C:\\Users\\Nathan\\Desktop\\" + filename); // Create image file on the desktop
			imageFile.createNewFile();
			
			// Write to our image file
			// Establish stream to file
			FileOutputStream fos = new FileOutputStream(imageFile);
			
			// Reset inStream, since bufferedReader moves it's position
			inStream.reset();
			
			boolean endOfHeadersReached = false;
			int length;
			int bufferSize = 99999;
			byte[] b = new byte[bufferSize]; 

			while ((length = inStream.read(b)) != -1){
				System.out.println(length);
				
				// Write bytes to the file
				if (endOfHeadersReached){
					fos.write(b, 0, length);
				} else {
					// Check if ended by checking if double CRLF
					for (int i=0; i < length - 3; i++){
						if ((b[i] == 13 ) && (b[i+1] == 10) && (b[i+2] == 13) && b[i+3] == 10){
							endOfHeadersReached = true;
							System.out.println(Arrays.toString(Arrays.copyOfRange(b, i, i+8)));
							System.out.println(Arrays.toString(Arrays.copyOfRange(b, length -i-6, length - i)));						
							fos.write(b, i+4, length - i - 4);
							break;
						}
					}
				}
			}
			
		    textInFromServer.close();	
			fos.close();
			

			outStream.close();
			dataOutStream.close();
			clientSocket.close();	
			
		}
		return;
	}
	private static HashMap<String, String> handleHeaders(BufferedReader textInFromServer, BufferedWriter bw) throws IOException {
		
		HashMap<String, String> headersMap = new HashMap<>();
		// Keep going until at first CRLF
		while (true) {
				String serverHeaderSentence = textInFromServer.readLine();
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
				(argv[1].matches("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]") ||
						(argv[1].matches("localhost((/){1}[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*)*")))
						&&
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
