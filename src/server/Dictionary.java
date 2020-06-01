/*
 * Dictionary.java
 * 
 * This class maintains a simple dictionary, built upon a JSONObject.
 * It is thread-safe.
 * 
 * @author James Barnes (820946)
 */

package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Dictionary {
	private final ReentrantLock lock;

	private JSONObject dictionary = null;

	/*
	 * Creates a Dictionary from the file pointed to by filename
	 * 
	 * @param filename file to read dictionary from
	 */
	public Dictionary(String filename) {
		lock = new ReentrantLock();

		try (InputStream is = new FileInputStream(filename)) {
			dictionary = new JSONObject(new JSONTokener(is));
			System.out.println("Dictionary successfully read from file.");
		} catch (JSONException je) {
			dictionary = new JSONObject();
			System.out.println(
				"There was an error in the dictionary file.\n"
					+ "Resorting to an empty dictionary.");
		} catch (IOException ioe) {
			System.err.println(
				String.format(
					"There was an error when reading the dictionary file (%s).",
					ioe.getMessage()));
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * Gets the definitions of word found in the Dictionary
	 * 
	 * @param word String to get definitions of
	 */
	public JSONArray getDefinitions(String word) {
		JSONArray definitions = null;

		lock.lock();
		try {
			definitions = dictionary.optJSONArray(word);
		} finally {
			lock.unlock();
		}

		return definitions;
	}

	/*
	 * Adds a definition to word in the dictionary, returning true if the word
	 * was already in the Dicitionary
	 * 
	 * @param word String word to add definition to
	 * 
	 * @param definiton JSONObjet containing the definition (not checked for
	 * validity)
	 */
	public boolean addDefintion(String word, JSONObject definition) {
		boolean isNew = false;
		JSONArray definitions = null;

		lock.lock();
		try {
			isNew = dictionary.has(word);
			definitions = isNew ? dictionary.optJSONArray(word)
				: new JSONArray();
			List<Object> newDefinitions = definitions.toList();
			newDefinitions.add(definition);
			dictionary.put(word, new JSONArray(newDefinitions));
		} finally {
			lock.unlock();
		}

		return isNew;
	}

	/*
	 * Deletes a word and all associated definitions from the dictionary,
	 * returning true if the word was in the Dictionary
	 * 
	 * @param word String to delete from Dictionary
	 */
	public boolean deleteWord(String word) {
		boolean deleted = false;

		lock.lock();
		try {
			deleted = dictionary.remove(word) != null;
		} finally {
			lock.unlock();
		}

		return deleted;
	}

	/*
	 * Returns the current state of the dictionary in JSON format
	 */
	public String getJSONString() {
		String dictString = "{}";

		lock.lock();
		try {
			dictString = dictionary.toString();
		} finally {
			lock.unlock();
		}

		return dictString;
	}
}
