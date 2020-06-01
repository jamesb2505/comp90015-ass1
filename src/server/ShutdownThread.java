/*
 * ShutdownThread.java
 * 
 * Helper thread that writes the server's dictionary to a file when ran.
 * 
 * @author James Barnes (820946)
 */

package server;

import java.io.FileWriter;
import java.io.IOException;

public class ShutdownThread extends Thread {
	private final String filename;
	private final Dictionary dictionary;
	private final WorkerThreadPool workers;

	/*
	 * Create a ShutdownThread for various resources
	 * 
	 * @param fillename String where to save the Dictionary
	 * 
	 * @param dictionary Dictionary to save to file
	 * 
	 * @param workers WorkerThreadPool to interrupt the running of
	 */
	public ShutdownThread(String filename, Dictionary dictionary,
		WorkerThreadPool workers) {
		this.filename = filename;
		this.dictionary = dictionary;
		this.workers = workers;
	}

	public void run() {
		System.out.println("Shutting down server.");

		workers.interrupt();

		try (FileWriter writer = new FileWriter(filename)) {
			System.out.println("Saving dictionary to file.");
			writer.write(dictionary.getJSONString());
			System.out.println("Dictionary saved to file.");
		} catch (IOException ioe) {
			System.err.println(
				"There was an error that occured saving the dictionary file.");
			ioe.printStackTrace();
		}

		System.out.println("Server shut down.");
	}
}
