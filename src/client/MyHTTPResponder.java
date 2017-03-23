package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

// Class that handles a single client
public class MyHTTPResponder implements Runnable {

	private Socket clientSocket;

	public MyHTTPResponder(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	@Override
	public void run() {
		// Establish communication channels with client
		try {
			InputStream inFromClient = clientSocket.getInputStream();
			OutputStream outToClient = clientSocket.getOutputStream();
			Scanner inScanner = new Scanner(inFromClient);
			
			// Check if there's anything there. If not, return
			if (!inScanner.hasNextLine()){
				return;
			}
			String requestLine = inScanner.nextLine();;
			// Parse the clients first line to see what to do. Ignore empty lines before that
			while (requestLine.isEmpty()){
				requestLine = inScanner.nextLine();
			}
			
			// Checks if requestline is ok, and returns it's components
			String []requestLineElements = handleRequestline(requestLine);
			String protocolVersion = requestLineElements[2];
			String command = requestLineElements[0];
			String requestURI = requestLineElements[1];
			
			// Go to correct handler based on command
			if (command.equals("GET")){
				handleGETRequest(inFromClient, outToClient, requestURI, protocolVersion);
			
			} else if (command.equals("HEAD")){
				handleHEADRequest(inFromClient, outToClient, requestURI, protocolVersion);
			} else if (command.equals("PUT")){
				handlePUTRequest(inFromClient, outToClient, requestURI, protocolVersion);
			} else if (command.equals("POST")){
				handlePOSTRequest(inFromClient, outToClient, requestURI, protocolVersion);
			}
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// If things go south, close the connection in any case
			try {
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// TODO Auto-generated method stub
		
	}

	private void handlePOSTRequest(InputStream inFromClient, OutputStream outToClient, String requestURI,
			String protocolVersion) {
		// TODO Auto-generated method stub
		
	}

	private void handlePUTRequest(InputStream inFromClient, OutputStream outToClient, String requestURI,
			String protocolVersion) {
		// TODO Auto-generated method stub
		
	}
	
	
	// If the file can be found, this function prints to the client:
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
			String protocolVersion) {
		
		// parse the requestURI
			String scheme = requestURI.split("://")[0];
			String domain = requestURI.split("//")[1].split("/")[0];
			String baseURI = scheme + "://" + domain + "/";
			String path = requestURI.replace(baseURI, "");
			
		// try to go to the path
		
	}

	private void handleGETRequest(InputStream inFromClient, OutputStream outToClient, String requestURI,
			String protocolVersion) {
		// TODO Auto-generated method stub
		
	}

	private String[] handleRequestline(String requestLine) {
		// TODO Auto-generated method stub
		return null;
	}

}
