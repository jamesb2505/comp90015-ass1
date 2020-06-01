/*
 * DictionaryClient.java
 * 
 * Main class for the Dictionary Client. 
 * Handles all requests to the dictionary server. 
 * 
 * @author James Barnes (820946)
 */

package client;

import org.json.JSONObject;

import common.JSONConsts;

public class DictionaryClient {
	public static final int DEFAULT_PORT = 9015;

	private ClientUI ui = null;

	private String ip;
	private int port;

	public static void main(String args[]) {
		if (args.length != 2) {
			System.err.println("usage: <server-address> <port>");
			System.exit(1);
		}

		String ip = args[0];
		int port = DEFAULT_PORT;
		try {
			port = Integer.parseInt(args[1]);
		} catch (NumberFormatException nfe) {
			System.err.format(
				"An error occurred reading the port number. Defaulting to %d.\n",
				port);
		}

		System.out.println("Starting client.");
		new DictionaryClient(ip, port);
		System.out.println("Client started.");
	}

	/*
	 * Creates a new DictionaryClient, with starting IP and port
	 * 
	 * @param ip String IP address for the server to connect to
	 * 
	 * @param port int port number for server
	 */
	public DictionaryClient(String ip, int port) {
		this.ip = ip;
		this.port = port;

		this.ui = new ClientUI(this);
		ui.setVisible(true);
	}

	/*
	 * Sends a request to the server using a RequestThread
	 * 
	 * @param message String to send to server
	 */
	public synchronized void sendRequest(String message) {
		// this must be done in the background to allow UI to remain
		// "functional"
		new RequestThread(this, ip, port, message).start();
	}

	/*
	 * Queries the server for definitions of word
	 * 
	 * @param word String to query the definitions of
	 */
	public void queryDefinitions(String word) {
		JSONObject out = new JSONObject()
			.put(JSONConsts.COMMAND, JSONConsts.COMMAND_QUERY)
			.put(JSONConsts.WORD, word);
		sendRequest(out.toString());
	}

	/*
	 * Adds the definition to the server
	 * 
	 * @param word String to add the definition to
	 * 
	 * @param defintion String definition to add to word
	 * 
	 * @param author String author of word
	 */
	public void addDefinition(String word, String definition, String author) {
		if (word == null || word.isEmpty() || definition == null
			|| definition.isEmpty()) {
			ui.showAddedDialog(JSONConsts.WORD_EMPTY);
			return;
		}

		JSONObject out = new JSONObject()
			.put(JSONConsts.COMMAND, JSONConsts.COMMAND_ADD)
			.put(JSONConsts.WORD, word).put(
				JSONConsts.CONTENT,
				new JSONObject().put(JSONConsts.WORD_DEFINITION, definition)
					.put(JSONConsts.WORD_AUTHOR, author));
		sendRequest(out.toString());
	}

	/*
	 * Deletes a word and definitions from a server
	 * 
	 * @param word String to delete from server
	 */
	public void deleteWord(String word) {
		JSONObject out = new JSONObject()
			.put(JSONConsts.COMMAND, JSONConsts.COMMAND_DELETE)
			.put(JSONConsts.WORD, word);
		sendRequest(out.toString());
	}

	/*
	 * Gets the DictionaryClient's current server's IP
	 */
	public synchronized String getIP() {
		return ip;
	}

	/*
	 * Gets the DictionaryClient's current server's port
	 */
	public synchronized int getPort() {
		return port;
	}

	/*
	 * Sets the DictionaryClient's current server settings (IP, port)
	 * 
	 * @param ip String IP of the server
	 * 
	 * @param port int port to connect to server on
	 */
	public synchronized void setSettings(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	/*
	 * Gets the DictionaryClient's UI
	 */
	public ClientUI getUI() {
		return ui;
	}
}
