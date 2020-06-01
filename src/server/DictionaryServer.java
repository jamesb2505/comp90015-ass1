/*
 * DictionaryServer.java
 * 
 * Main class for the Dictionary Server. 
 * Accepts client requests and spans new threads per client, 
 * while maintaining the central dictionary
 * 
 * @author James Barnes (820946)
 */

package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class DictionaryServer {
	private static final int MAX_POOL_SIZE = 8;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("usage: <port> <dictionary-file>");
			System.exit(1);
		}

		String portString = args[0];
		String dictionaryFile = args[1];

		Dictionary dictionary = new Dictionary(dictionaryFile);

		WorkerThreadPool workers = new WorkerThreadPool(MAX_POOL_SIZE);

		try (ServerSocket serverSocket = new ServerSocket(
			Integer.parseInt(portString))) {

			// add hook to save dictionary on Ctrl-C
			Runtime.getRuntime().addShutdownHook(
				new ShutdownThread(dictionaryFile, dictionary, workers));
			System.out.println(
				"Press Ctrl-C to quit and save the dictionary to file.");

			System.out.println("Server is now running.");
			System.out
				.println("IP:\t" + InetAddress.getLocalHost().getHostAddress());
			System.out.println("Port:\t" + serverSocket.getLocalPort());
			System.out.println("Waiting for new connections...");

			while (true) {
				try {
					Socket clientSocket = serverSocket.accept();
					System.out.format(
						"Request from %s:%d\n",
						clientSocket.getInetAddress().getHostAddress(),
						clientSocket.getPort());

					
					workers.add(new RequestHandler(clientSocket, dictionary));
				} catch (IOException e) {
					System.err.println(
						"There was an error accepting a request: "
							+ e.getMessage());
				}
			}
		} catch (NumberFormatException nfe) {
			System.err
				.println("There was an error parsing the supplied port number");
		} catch (IOException ioe) {
			System.err.println(
				"There was an error that occured when setting up the server socket: "
					+ ioe.getMessage());
			System.exit(1);
		}
	}
}
