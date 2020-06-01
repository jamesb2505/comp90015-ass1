/*
 * RequestHandler.java
 * 
 * Request class for DictionaryServer
 * Handles connection with a single client, processing a single request
 * 
 * @author James Barnes (820946)
 */

package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONObject;

import common.JSONConsts;

public class RequestHandler implements Runnable {
	private final Socket socket;
	private final Dictionary dictionary;

	/*
	 * Creates a new RequestHandler
	 * 
	 * @param socket Socket to communicate to client with
	 * 
	 * @param dictionaary Dictionary to query
	 */
	public RequestHandler(Socket socket, Dictionary dictionary) {
		this.socket = socket;
		this.dictionary = dictionary;
	}

	/*
	 * Runs the RequestHandler. The Request reads from its socket, generates
	 * a response, then writes and closes the socket
	 */
	public synchronized void run() {
		try {
			DataOutputStream dos = new DataOutputStream(
				socket.getOutputStream());
			DataInputStream dis = new DataInputStream(socket.getInputStream());

			JSONObject json = new JSONObject(dis.readUTF());
			JSONObject out = null;
			switch (json.optString(JSONConsts.COMMAND)) {
			case JSONConsts.COMMAND_ADD:
				out = addDefiniton(json);
				break;
			case JSONConsts.COMMAND_DELETE:
				out = deleteWord(json);
				break;
			case JSONConsts.COMMAND_QUERY:
				out = queryDefinitons(json);
				break;
			default:
				out = badRequest();
			}

			dos.writeUTF(out.toString());

			System.out.println(
				String.format(
					"Serviced %s:%d",
					socket.getInetAddress().getHostAddress(),
					socket.getPort()));

			dos.close();
			dis.close();
		} catch (IOException ioe) {
			printError(ioe);
		} catch (Exception e) {
			printError(e);
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException ioe) {
					printError(ioe);
				}
			}
		}
	}

	/*
	 * Prints a formatted error message to the console
	 * 
	 * @param e Exception from the error
	 */
	private void printError(Exception e) {
		System.err.format(
			"%s:%d: %s\n",
			socket.getInetAddress().getHostAddress(),
			socket.getPort(),
			e.getMessage());
		e.printStackTrace();
	}

	/*
	 * Deletes a word and returns a JSON Object, detailing if the word was added
	 * or updated
	 * 
	 * @param json JSONObject with function parameters
	 */
	private JSONObject addDefiniton(JSONObject json) {
		JSONObject out = new JSONObject();
		out.put(JSONConsts.COMMAND, JSONConsts.COMMAND_ADD);

		String word = json.optString(JSONConsts.WORD);
		JSONObject content = json.optJSONObject(JSONConsts.CONTENT);

		if (content != null && word != null && !word.isEmpty()
			&& content.has(JSONConsts.WORD_DEFINITION)
			&& !content.getString(JSONConsts.WORD_DEFINITION).isEmpty()) {
			if (dictionary.addDefintion(word, content)) {
				out.put(JSONConsts.CONTENT, JSONConsts.WORD_UPDATED);
			} else {
				out.put(JSONConsts.CONTENT, JSONConsts.WORD_ADDED);
			}
		} else {
			out.put(JSONConsts.CONTENT, JSONConsts.WORD_EMPTY);
		}

		return out;
	}

	/*
	 * Deletes a word and returns a JSON Object, detailing if the word was
	 * deleted or not
	 * 
	 * @param json JSONObject with function parameters
	 */
	private JSONObject deleteWord(JSONObject json) {
		JSONObject out = new JSONObject();
		out.put(JSONConsts.COMMAND, JSONConsts.COMMAND_DELETE);

		String word = json.optString(JSONConsts.WORD);
		if (dictionary.deleteWord(word)) {
			out.put(JSONConsts.CONTENT, JSONConsts.WORD_DELETED);
		} else {
			out.put(JSONConsts.CONTENT, JSONConsts.WORD_UNKNOWN);
		}

		return out;
	}

	/*
	 * Creates a JSON Object with the definitions associated with word
	 * 
	 * @param json JSONObject with function parameters
	 */
	private JSONObject queryDefinitons(JSONObject json) {
		JSONObject out = new JSONObject();
		out.put(JSONConsts.COMMAND, JSONConsts.COMMAND_QUERY);

		JSONArray content = dictionary
			.getDefinitions(json.optString(JSONConsts.WORD));
		if (content == null) {
			out.put(JSONConsts.CONTENT, new JSONArray());
		} else {
			out.put(JSONConsts.CONTENT, new JSONArray(content.toString()));
		}

		return out;
	}

	/*
	 * Creates a bad request JSON Object
	 */
	private JSONObject badRequest() {
		JSONObject out = new JSONObject();
		out.put(JSONConsts.COMMAND, JSONConsts.COMMAND_ERROR)
			.put(JSONConsts.CONTENT, JSONConsts.BAD_REQUEST);

		return out;
	}
}
