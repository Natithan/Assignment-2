package client;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;

import org.apache.commons.io.FilenameUtils;

import com.sun.org.apache.xml.internal.serialize.LineSeparator;

// Class that handles a single client
public class MyHTTPResponder implements Runnable {

	private Socket clientSocket;
	private static final String serverPtclVersion = "HTTP/1.1";
	
	
	public MyHTTPResponder(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	@Override
	public void run() {
		// Establish communication channels with client
		long startTime = System.currentTimeMillis(); //fetch starting time
		// Keep reading stuff 'till disconnected or timed out
		while(this.clientSocket.isConnected() && (System.currentTimeMillis()-startTime)<60000){
			try {
				
				InputStream inFromClient = clientSocket.getInputStream();
//				BufferedInputStream bufferedInFromClient = new BufferedInputStream(inFromClient);
//				
//				byte[] b = new byte[30];
//				bufferedInFromClient.mark(100);
				
				OutputStream outToClient = clientSocket.getOutputStream();
				
				
				
				BufferedReader inReader = new BufferedReader(new InputStreamReader(inFromClient));

				

				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outToClient));
				
				// Check if there's anything there. If not, return
				if (!inReader.ready()){
					return;
				}
				String requestLine = inReader.readLine();;
				// Parse the clients first line to see what to do. Ignore empty lines before that
				while (requestLine.isEmpty()){
					requestLine = inReader.readLine();
				}
				
				// Checks if requestline is ok, and returns it's components. Returns "HTTP/1.1 500 internal server error" if requestline can't be dealt with
				if (!isValidRequestLine(requestLine)){
					bw.write(MyHTTPResponder.serverPtclVersion + " 500 Internal server error");
					bw.close();
					return;
				}
				String []requestLineElements = handleRequestline(requestLine, outToClient);
				String protocolVersion = requestLineElements[2];
				String command = requestLineElements[0];
				String requestURI = requestLineElements[1];
				

				String scheme = requestURI.split("://")[0];
				String domain = requestURI.split("//")[1].split("/")[0];
				String baseURI = scheme + "://" + domain + "/";
				String path = requestURI.replace(baseURI, "");
				path = "src/" + path;
				

				// Store headers in hashMap
				HashMap<String, String> headersMap = new HashMap<>();
				// Checks if host header is provided if protocolVersion is HTTP/1.1
				String shouldBeHostHeader = inReader.readLine();
				if ((protocolVersion.equals("HTTP/1.1")) && (! shouldBeHostHeader.toLowerCase().matches("host:\\s" + domain))){

					bw.write(MyHTTPResponder.serverPtclVersion + "400 Bad request: Missing host header");
					bw.close();
					return;
				}
				headersMap.put(shouldBeHostHeader.split("\\s+")[0].toLowerCase(), shouldBeHostHeader.split("\\s+", 2)[1]);
				// Keep going until at first CRLF
				while (true) {
						String clientHeaderSentence = inReader.readLine();
						// Check if we're at empty line
						if (clientHeaderSentence.isEmpty()){
							break;
						}; // Stop if at empty line
						
						// Get info
						String name = clientHeaderSentence.split("\\s+")[0].toLowerCase();
						String value = clientHeaderSentence.split("\\s+", 2)[1];
						headersMap.put(name, value);
				}
//				
//				bw.flush();
//				bufferedInFromClient.reset();
//				bufferedInFromClient.read(b);
//				System.out.println(new String(b));
				
				// Go to correct handler based on command
				if (command.equals("GET")){
					handleGETRequest(inFromClient, outToClient, requestURI, protocolVersion, headersMap);
				
				} else if (command.equals("HEAD")){
					handleHEADRequest(inFromClient, outToClient, requestURI, protocolVersion, headersMap);
				} else if (command.equals("PUT")){
					handlePUTRequest(inReader, outToClient, requestURI, protocolVersion, headersMap);
				} else if (command.equals("POST")){
					handlePOSTRequest(inReader, outToClient, requestURI, protocolVersion, headersMap);
				}
				
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		System.out.println("Disconnected or timed out");
		return;
		// TODO Auto-generated method stub
		
	}
	
	
	/* Method that prints to the client:
	 * - A status line
	 * - Headers: Date, content-type and content-length headers
	 * 
	 * and updates a local text file with the client entity.
	 * If the file is not found, 404 is returned
	 * 
	 * 
	 * 
	 * 
	 * */
	private void handlePOSTRequest(BufferedReader inReader, OutputStream outToClient, String requestURI,
			String protocolVersion, HashMap<String, String> headersMap) throws Exception {

		// parse the requestURI
		String scheme = requestURI.split("://")[0];
		String domain = requestURI.split("//")[1].split("/")[0];
		String baseURI = scheme + "://" + domain + "/";
		String path = requestURI.replace(baseURI, "");
		path = "src/" + path;
		
		// Create writer to talk more easily to the client
		BufferedWriter writerToClient = new BufferedWriter(new OutputStreamWriter(outToClient));
		
		File file = new File(path);
		
		// Check if the file exists
		if(!file.exists()){
			writerToClient.write(serverPtclVersion + " 404 File not found");
		}
		
		// File exists, so we can safely overwrite it
		// reset stream to begin, since using bufferedreader moved it way far.
		// Get data length
		int entityLength = Integer.parseInt(headersMap.get("content-length:"));
		char[] entityChars = new char[entityLength];
		
		// Dealt with headers in run(), so should be at space between headers and body. Check this, and advance the reader past this space
		// This way of dealing with it means that
		char[] shouldBeEnterChars = new char[2];
		inReader.read(shouldBeEnterChars);
		String shouldBeEnterString = new String(shouldBeEnterChars);
		if (! (shouldBeEnterString.equals("\r\n") ) ){
			throw new Exception("No space between headers and body! (Or shot right past them)");
		}
		
		
		System.out.println(inReader.read(entityChars));
		System.out.println(new String(entityChars));
		// Create writer
		FileWriter writerToFile = new FileWriter(file);
		writerToFile.write(entityChars);
		writerToFile.close();
		
		

	}
	/* Method that prints to the client:
	 * - A status line
	 * - Headers: Date, content-type and content-length headers
	 * 
	 * and updates or creates a local text file with the client entity. 
	 * 
	 * 
	 * */
	private void handlePUTRequest(BufferedReader inReader, OutputStream outToClient, String requestURI,
			String protocolVersion, HashMap<String, String> headersMap) throws Exception {
		// parse the requestURI
			String scheme = requestURI.split("://")[0];
			String domain = requestURI.split("//")[1].split("/")[0];
			String baseURI = scheme + "://" + domain + "/";
			String path = requestURI.replace(baseURI, "");
			path = "src/" + path;
			
			// Create writer to talk more easily to the client
			BufferedWriter writerToClient = new BufferedWriter(new OutputStreamWriter(outToClient));
			
			File file = new File(path);
			
			// Check if the file exists
			file.createNewFile();
			
			// File exists, so we can safely overwrite it
			// reset stream to begin, since using bufferedreader moved it way far.
			// Get data length
			int entityLength = Integer.parseInt(headersMap.get("content-length:"));
			char[] entityChars = new char[entityLength];
			
			// Dealt with headers in run(), so should be at space between headers and body. Check this, and advance the reader past this space
			// This way of dealing with it means that
			char[] shouldBeEnterChars = new char[2];
			inReader.read(shouldBeEnterChars);
			String shouldBeEnterString = new String(shouldBeEnterChars);
			if (! (shouldBeEnterString.equals("\r\n") ) ){
				throw new Exception("No space between headers and body! (Or shot right past them)");
			}
			
			
			System.out.println(inReader.read(entityChars));
			System.out.println(new String(entityChars));
			// Create writer
			FileWriter writerToFile = new FileWriter(file);
			writerToFile.write(entityChars);
			writerToFile.close();
		
	}
	
	
	// This function prints to the client:
	// A status line, containing
	// - protocolVersion
	// - status code
	// - status phrase
	// Headers:
	// - date header
	// - Content length header
	// - content type header
	//
	//
	//
	private void handleHEADRequest(InputStream inFromClient, OutputStream outToClient, String requestURI,
			String protocolVersion, HashMap<String, String> headersMap) throws IOException {
		
		// parse the requestURI
			String scheme = requestURI.split("://")[0];
			String domain = requestURI.split("//")[1].split("/")[0];
			String baseURI = scheme + "://" + domain + "/";
			String path = requestURI.replace(baseURI, "");
			path = "src/" + path;
		
		// Create writer to talk more easily to the client
			BufferedWriter writerToClient = new BufferedWriter(new OutputStreamWriter(outToClient));
		// Make filename
			File file = new File(path);
		// try and connect a reader with the file at the path
			try {
				FileReader fr = new FileReader(file);
				
				// File found
				String serverPtclVersion = "HTTP/1.1";
				String statusCode = "200";
				String statusPhrase = "OK";
				// Store info about the file.
				// Length has to be length of what you would send in a GET-request.
				String contentLengthValue = Long.toString(file.length());
				String extension = FilenameUtils.getExtension(path);
				String contentTypeValue = "";
				String temp = Files.probeContentType(file.toPath());
				
				if (temp != null){
					contentTypeValue = temp;
				}
				String DateValue = dateHeader();
				
				String dHeader = "Date: " + DateValue;
				String clHeader = "Content-Length: " + contentLengthValue;
				String ctHeader = "Content-Type: " + contentTypeValue;
				String headers = dHeader + "\r\n" +  clHeader + "\r\n" +ctHeader + "\r\n";
				
				String statusLine = serverPtclVersion + " " + statusCode + " " + statusPhrase;
				// Write it all to the client
				String response = statusLine  + "\r\n" 
						+ headers + "\r\n";
				writerToClient.write(response);
				writerToClient.flush();
			} catch (FileNotFoundException e) {
				writerToClient.write("HTTP/1.1 404 File not found");
				return;
			}
		
	}
	
	// Creates a string representing the current date in the format described by RFC 1123
	private String dateHeader() {
		Calendar calendar = Calendar.getInstance();
	    SimpleDateFormat dateFormat = new SimpleDateFormat(
	        "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	    return dateFormat.format(calendar.getTime());
	}
	
	// This function prints to the client:
		// A status line, containing
		// - protocolVersion
		// - status code
		// - status phrase
		// Headers:
		// - date header
		// - Content length header
		// - content type header
		// Body:
		// - 
	private void handleGETRequest(InputStream inFromClient, OutputStream outToClient, String requestURI,
			String protocolVersion, HashMap<String, String> headersMap) throws IOException, ParseException {
		// parse the requestURI
			String scheme = requestURI.split("://")[0];
			String domain = requestURI.split("//")[1].split("/")[0];
			String baseURI = scheme + "://" + domain + "/";
			String path = requestURI.replace(baseURI, "");
			
			path = "src/" + path;
			
			String statusCode;
			String statusPhrase;
		
		// Create writer to talk more easily to the client
			BufferedWriter writerToClient = new BufferedWriter(new OutputStreamWriter(outToClient));
		// Make filename
			File file = new File(path);
		// try and connect a reader with the file at the path
			try {
				FileReader fr = new FileReader(file);
				
				//Look for if-modified-since header, store other headers (except host) in hashMap for good measure
				if (headersMap.containsKey("if-modified-since:")){
					// Store that date					
					String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
					SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
					
					String imsDateString = headersMap.get("if-modified-since:");					
					Date imsDate = sdf.parse(imsDateString);
					
					// Look for date that the file was lastly modified
					String lmDateString = sdf.format(file.lastModified());					
					Date lmDate = sdf.parse(imsDateString);
					
					if (lmDate.before(imsDate)){ //If last modified before if-modified-since date, send 304
						writerToClient.write("HTTP/1.1 304 Not modified");
						writerToClient.write(dateHeader());
						writerToClient.close();
						return;
					}
				}
				// No ims-header or was modified since: send file
				statusCode = "200";
				statusPhrase = "OK";

				String statusLine = serverPtclVersion + " " + statusCode + " " + statusPhrase;
				// Headers
				String contentLengthValue = Long.toString(file.length());
				String extension = FilenameUtils.getExtension(path);
				String contentTypeValue = "";
				String temp = Files.probeContentType(file.toPath());
				
				if (temp != null){
					contentTypeValue = temp;
					}
				
				String DateValue = dateHeader();				
				String dHeader = "Date: " + DateValue;
				String clHeader = "Content-Length: " + contentLengthValue;
				String ctHeader = "Content-Type: " + contentTypeValue;
				String headers = dHeader + "\r\n" +  clHeader + "\r\n" +ctHeader + "\r\n";
					
				// body
				int buffersize = (int) file.length();
				char[] fileCharacters = new char[buffersize];
				fr.read(fileCharacters);
				
				String body = new String(fileCharacters);	
					
				
				// Write it all to the client
				String response = statusLine  + "\r\n" 
						+ headers + "\r\n" + body+ "\r\n";
				System.out.println(response);
				writerToClient.write(response);
				writerToClient.flush();
					
			} catch (FileNotFoundException e) {
				writerToClient.write("HTTP/1.1 404 File not found");
				return;
			}
	}
	// TODO check if valid request line
	private String[] handleRequestline(String requestLine, OutputStream outToClient) throws Exception {
		if (isValidRequestLine(requestLine)){
			return requestLine.split("\\s+");
		} else {
			throw new Exception("Illegal requestline!");
		}
	}
	
	private boolean isValidRequestLine(String requestLine) {
				
		return requestLine.matches("(GET|HEAD|POST|PUT)\\s+((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])\\s+(HTTP/1.(1|0))\\s*");
	}

}
