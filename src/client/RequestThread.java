/*
 * RequestThread.java
 * 
 * This class handles the communication with a server, in the form of a single request, 
 * updating the UI once the request is serviced.
 * 
 * @author James Barnes (820946)
 */

package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import common.JSONConsts;

public class RequestThread extends Thread {
	private static final int CONNECT_TIMEOUT = 5000;

	private final DictionaryClient client;
	private final String ip;
	private final int port;
	private final String message;

	/*
	 * Creates a new RequestThread
	 * 
	 * @param client DictionaryCLient that will receive the request
	 * 
	 * @param ip String IP address used to connect to the server
	 * 
	 * @param port int to connect to server
	 */
	public RequestThread(DictionaryClient client, String ip, int port,
		String message) {
		this.client = client;
		this.ip = ip;
		this.port = port;
		this.message = message;
	}

	@Override
	public void run() {
		ClientUI ui = client.getUI();

		ui.setWaiting(true);

		Socket socket = null;
		DataOutputStream dos = null;
		DataInputStream dis = null;
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), CONNECT_TIMEOUT);
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());

			dos.writeUTF(message);
			String in = dis.readUTF();

			JSONObject json = new JSONObject(in);
			Object content = json.opt(JSONConsts.CONTENT);

			switch (json.optString(JSONConsts.COMMAND)) {
			case JSONConsts.COMMAND_ERROR:
				ui.showErrorDialog((String) content);
				break;
			case JSONConsts.COMMAND_ADD:
				ui.showAddedDialog((String) content);
				break;
			case JSONConsts.COMMAND_DELETE:
				ui.showDeletedDialog((String) content);
				break;
			case JSONConsts.COMMAND_QUERY:
				ui.showDefinition((JSONArray) content);
				break;
			}

			dos.close();
			dis.close();
		} catch (IllegalArgumentException iae) {
			ui.showErrorDialog(
				String.format(
					"Invalid port or IP (see Settings). Port must be between 0 and 65535",
					iae.getMessage()));
		} catch (UnknownHostException uhe) {
			ui.showErrorDialog(
				String.format(
					"Unknown host. Ensure the server is running "
						+ "and the IP address is correct (see Settings) (%s)",
					uhe.getMessage()));
		} catch (ConnectException ce) {
			ui.showErrorDialog(
				String.format(
					"Connect error. Ensure the server is running "
						+ "and the IP address is correct (see Settings) (%s)",
					ce.getMessage()));
		} catch (SocketTimeoutException ste) {
			ui.showErrorDialog(
				String.format(
					"Timed out. Ensure the server is running "
						+ "and the IP address is correct (see Settings) (%s)",
					ste.getMessage()));
		} catch (IOException ioe) {
			ui.showErrorDialog(
				String.format("IO error (%s)", ioe.getMessage()));
		} catch (JSONException je) {
			ui.showErrorDialog(
				String.format("Server sent bad JSON (%s)", je.getMessage()));
		} catch (Exception e) {
			ui.showErrorDialog(String.format("Error (%s)", e.getMessage()));
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}

		ui.setWaiting(false);
	}
}
