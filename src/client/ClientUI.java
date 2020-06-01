/*
 * ClientUI.java
 * 
 * Controls and accepts user input from the UI, 
 * passing requests to DictionaryClient.
 * 
 * @author James Barnes (820946)
 */

package client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;

import org.json.JSONArray;
import org.json.JSONObject;

import common.JSONConsts;

public class ClientUI extends JFrame {
	private static final long serialVersionUID = 90015L;

	private static final String TITLE = "Distributed Dictionary";
	private static final String QUERY_BUTTON = "Query";
	private static final String ADD_BUTTON = "Add";
	private static final String DELETE_BUTTON = "Delete";
	private static final String HELP_BUTTON = "Help";
	private static final String SETTINGS_BUTTON = "Settings";
	private static final String UNKNOWN = "<unkown>";

	private final DictionaryClient client;

	private final JTextArea def;
	private final JTextArea newDef;
	private final JTextField author;
	private final JTextField word;
	private final JPanel settings;
	private final JTextField ip;
	private final JTextField port;

	public ClientUI(DictionaryClient client) {
		super(TITLE);

		this.client = client;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		JPanel controlPanel = new JPanel(new GridLayout(2, 1));
		JPanel buttonPanel = new JPanel();
		JPanel wordPanel = new JPanel();
		wordPanel.add(new JLabel("Word"));
		wordPanel.add(word = new JTextField(55));
		buttonPanel.add(new JLabel("Author"));
		buttonPanel.add(author = new JTextField(20));
		buttonPanel.add(makeHandledButton(QUERY_BUTTON));
		buttonPanel.add(makeHandledButton(ADD_BUTTON));
		buttonPanel.add(makeHandledButton(DELETE_BUTTON));
		buttonPanel.add(makeHandledButton(HELP_BUTTON));
		buttonPanel.add(makeHandledButton(SETTINGS_BUTTON));
		controlPanel.add(wordPanel);
		controlPanel.add(buttonPanel);

		def = new JTextArea(20, 90);
		def.setLineWrap(true);
		def.setWrapStyleWord(true);
		def.setEditable(false);
		newDef = new JTextArea(10, 90);
		newDef.setLineWrap(true);
		newDef.setWrapStyleWord(true);
		newDef.setEditable(true);

		JScrollPane defPane = new JScrollPane(def);
		defPane.setBorder(new TitledBorder("Definitions"));
		defPane.setVerticalScrollBarPolicy(
			ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		JScrollPane newDefPane = new JScrollPane(newDef);
		newDefPane.setBorder(new TitledBorder("New Definition"));
		newDefPane.setVerticalScrollBarPolicy(
			ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		getContentPane().add(BorderLayout.SOUTH, controlPanel);
		getContentPane().add(BorderLayout.NORTH, defPane);
		getContentPane().add(BorderLayout.CENTER, newDefPane);
		pack();

		settings = new JPanel(new GridLayout(0, 2));
		settings.add(new JLabel("IP"));
		settings.add(ip = new JTextField(15));
		settings.add(new JLabel("Port"));
		settings.add(port = new JTextField(15));

		setWaiting(false);
	}

	/*
	 * Makes a new button with button action handling
	 * 
	 * @param name the name of the button
	 */
	private JButton makeHandledButton(String name) {
		JButton button = new JButton(name);
		button.addActionListener(e -> handleEvent(e));
		return button;
	}

	/*
	 * Handles the button click events
	 * 
	 * @param ae ActionEvent from Button
	 */
	private void handleEvent(ActionEvent ae) {
		switch (ae.getActionCommand()) {
		case QUERY_BUTTON:
			client.queryDefinitions(word.getText());
			break;
		case ADD_BUTTON:
			client.addDefinition(
				word.getText(),
				newDef.getText(),
				author.getText());
			break;
		case DELETE_BUTTON:
			client.deleteWord(word.getText());
			break;
		case HELP_BUTTON:
			showHelpDialog();
			break;
		case SETTINGS_BUTTON:
			showSettings();
			break;
		}
	}

	/*
	 * Shows an error dialog
	 * 
	 * @param message String with specific message for dialog
	 */
	public void showErrorDialog(String message) {
		JOptionPane.showMessageDialog(
			this,
			String.format(
				"An error occured.\n\nError: %s",
				message == null || message.isEmpty() ? UNKNOWN : message),
			"Uh Oh!",
			JOptionPane.ERROR_MESSAGE);
	}

	/*
	 * Shows a word's definition, or a dialog if no definition is known
	 * 
	 * @param content JSONArray used to show correct dialog/definition
	 */
	public void showDefinition(JSONArray content) {
		StringBuilder defText = new StringBuilder();
		defText
			.append(String.format("Definitions for %s:\n\n", word.getText()));

		if (content != null && !content.isEmpty()) {
			content.forEach(object -> {
				JSONObject entry = (JSONObject) object;
				String definition = entry.optString(JSONConsts.WORD_DEFINITION);
				String author = entry.optString(JSONConsts.WORD_AUTHOR);
				defText.append(
					String.format(
						"%s\n - submitted by %s\n\n",
						definition == null ? UNKNOWN : definition,
						author == null || author.isEmpty() ? UNKNOWN : author));
			});
		} else {
			JOptionPane.showMessageDialog(
				this,
				String.format(
					"Couldn't find a definition for \"%s\".\nPlease check the spelling and try again.",
					word.getText()),
				"Warning",
				JOptionPane.WARNING_MESSAGE);

			defText.append(UNKNOWN);
		}

		def.setText(defText.toString());
	}

	/*
	 * Shows the added dialog, depending on whether a word was added or updated
	 * 
	 * @param json JSONObject used to show correct dialog
	 */
	public void showAddedDialog(String message) {
		if (JSONConsts.WORD_EMPTY.equals(message)) {
			JOptionPane.showMessageDialog(
				this,
				"You cannot enter an empty definition.\nTry Again.",
				"Definition Error",
				JOptionPane.ERROR_MESSAGE);
		} else if (JSONConsts.WORD_ADDED.equals(message)) {
			JOptionPane.showMessageDialog(
				this,
				String.format(
					"Successfully added the definition for \"%s\".",
					word.getText()),
				"Dictionary Update",
				JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(
				this,
				String.format(
					"It looks like there was already a definition for \"%s\" in the dictionary.\n"
						+ "Successfully added an additional definition for \"%s\".",
					word.getText(),
					word.getText()),
				"Warning",
				JOptionPane.WARNING_MESSAGE);
		}
	}

	/*
	 * Shows the deleted dialog, depending on whether a word was deleted or not
	 * 
	 * @param json JSONObject used to show correct dialog
	 */
	public void showDeletedDialog(String message) {
		if (JSONConsts.WORD_DELETED.equals(message)) {
			JOptionPane.showMessageDialog(
				this,
				String.format(
					"Successfully deleted the definitions of \"%s\".",
					word.getText()),
				"Dictionary Update",
				JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(
				this,
				String.format(
					"Couldn't delete the word as no definitions of \"%s\" was found.",
					word.getText()),
				"Warning",
				JOptionPane.WARNING_MESSAGE);
		}
	}

	/*
	 * Shows the help dialog
	 */
	private void showHelpDialog() {
		JOptionPane.showMessageDialog(
			this,
			"Welcome to the Distributed Dictionary!\n\n"
				+ "To get the definitions of a word, enter the word in the"
				+ " Word box and press \"" + QUERY_BUTTON + "\".\n"
				+ "To update/add/delete a word in the dictionary, enter the word"
				+ " and the new information and press the appropriate button.\n\n"
				+ "The server can only handle so many active clients at a time,"
				+ " so an unresponsive server may be busy with other clients\n\n"
				+ "\tBy James Barnes, 820946",
			"Help",
			JOptionPane.INFORMATION_MESSAGE);
	}

	/*
	 * Shows the user that the server is waiting or not
	 * 
	 * @param show true if waiting on the server
	 */
	public void setWaiting(boolean waiting) {
		setTitle(TITLE + (waiting ? " - Waiting for Server..." : ""));
		recursivelySetEnabled(getContentPane(), !waiting);
	}

	private void recursivelySetEnabled(Container container, boolean enabled) {
		container.setEnabled(enabled);

		for (Component component : container.getComponents()) {
			if (component instanceof Container) {
				recursivelySetEnabled((Container) component, enabled);
			}
			component.setEnabled(enabled);
		}
	}

	/*
	 * Shows the settings dialog
	 */
	public void showSettings() {
		ip.setText(client.getIP());
		port.setText(String.format("%d", client.getPort()));

		int option = JOptionPane.showOptionDialog(
			this,
			settings,
			"Settings",
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.INFORMATION_MESSAGE,
			null,
			null,
			0);

		if (option == JOptionPane.OK_OPTION) {
			String newIP = ip.getText().replace("[^\\d.]", "");
			int newPort = DictionaryClient.DEFAULT_PORT;
			try {
				newPort = Integer
					.parseInt(port.getText().replaceAll("[^\\d]", ""));
			} catch (NumberFormatException nfe) {
				showErrorDialog(
					String.format("Bad port. Defaulting to %d.", newPort));
			}
			client.setSettings(newIP, newPort);
		}
	}
}
