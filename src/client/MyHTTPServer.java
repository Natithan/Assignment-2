package client;


import java.net.*;

public class MyHTTPServer {
	public static void main(String argv[]) throws Exception {
		int port = 0;
		if (argv.length == 0){
			port = 80;
		} else if (argv.length == 1) {
			try{
				port = Integer.parseInt(argv[0]);
				} catch (NumberFormatException e) {
				System.out.println(e);
				return;
				}
		}	else {
				System.out.println("Only port number or nothing as input");
			}
		ServerSocket serverSocket = new ServerSocket(port);
		
		// Keep on listening
		while(true){
			Socket clientSocket = serverSocket.accept();
			MyHTTPResponder responder = new MyHTTPResponder(clientSocket);
			Thread t = new Thread(responder);
			t.start();
		}
	}
}
