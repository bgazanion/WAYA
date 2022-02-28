package waya.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import waya.engine.PersonManager;
import waya.engine.TagManager;


public class TagAllPersonsDialogBuilder {
	
	private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getPackage().getName());
	private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("waya.gui.resources.messages");
	private static final String CLOSE = MESSAGES.getString("button_close_edit_tags_for_all");
	private static final int PREFERRED_WIDTH = 400;
	private static final int PREFERRED_HEIGHT = 400;
	
	
	private Frame owner;
	private TagManager tagManager;
	private PersonManager personManager;
	private LinkedList<String> personIdList;
	private LinkedList<String> personNameList;
	
	
	/**
	 * Constructor for a dialog builder
	 * @param owner frame owning the dialog built
	 * @param tm tag manager
	 * @param pm person manager
	 * @param dataDirectory directory where the tag manager's data is saved
	 */
	public TagAllPersonsDialogBuilder(Frame owner, TagManager tm, PersonManager pm) {

		this.owner = owner;
		tagManager = tm;
		personManager = pm;
		
		// list of person Ids, sorted by person name
		personIdList = new LinkedList<String>(personManager.getPersonIds());		
		Collections.sort(personIdList, new Comparator<String>() {
			public int compare(String id1, String id2) {
				try {
					String name1 = personManager.getPerson(id1).getName();
					String name2 = personManager.getPerson(id2).getName();
					return compare(name1, name2);
				} catch (IllegalArgumentException e) {
					return 0;
				}
			}
		});
		
		// list of person names, sorted by construction
		personNameList = new LinkedList<String>();
		for (String personId : personIdList) {
			personNameList.add(personManager.getPerson(personId).getName());
		}
	
	}

	
	/**
	 * Create a dialog to assign/unassign persons to a given tag
	 * @param tag name of the tag
	 * @return dialog
	 * @throws IllegalArgumentException
	 */
	public JDialog buildDialogForTag(String tag) throws IllegalArgumentException {
		if (!tagManager.hasTag(tag)) {
			throw new IllegalArgumentException("Tag '"+tag+"' does not exist");
		}
		return new TagAllPersonsDialog(tag);
	}
	

	/**
	 * Dialog to set or unset a specific tag for all the persons by simply checking/unchecking 
	 */
	private class TagAllPersonsDialog extends JDialog {
	
		private static final long serialVersionUID = 1L;
		
		private String tag;
		private List<JCheckBox> checkBoxList;
		
		/**
		 * Dialog displaying the persons, with a checkbox to set/unset the tag for each
		 * @param tag name of the tag
		 */
		public TagAllPersonsDialog(String tag) {
			super(owner, true);
			this.tag = tag;
			checkBoxList = new LinkedList<>();
			
			// panel containing the name of persons with a checkbox for each
			JPanel listPanel = new JPanel(new GridLayout(0, 1));
			listPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));;
			for (int index=0; index < personIdList.size(); index++) {
				String personId = personIdList.get(index);
				String name = personNameList.get(index);
				// checkbox
				JCheckBox checkBox = new JCheckBox();
				if (tagManager.getContent(tag).contains(personId)) {
					checkBox.setSelected(true);
				}
				checkBoxList.add(checkBox);
				// insert item in GUI
				JPanel itemPanel = new JPanel(new BorderLayout());
				itemPanel.add(new JLabel(name), BorderLayout.LINE_START);
				itemPanel.add(checkBox, BorderLayout.LINE_END);
				listPanel.add(itemPanel);
			}
			
			// button to close the dialog and save the modification on the tag
			JButton closeButton = new JButton(CLOSE);
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			
			// main panel
			JPanel dialogPanel = new JPanel(new BorderLayout());
			dialogPanel.add(new JScrollPane(listPanel), BorderLayout.CENTER);
			setContentPane(dialogPanel);
			pack();
			setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
			setSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
			setVisible(true);
		}
		
		
		/**
		 * Update the tag from the GUI and save the tag manager
		 */
		public void saveTag() {
			// update the content of the tag
			String personId;
			for (int index=0; index<checkBoxList.size(); index++) {
				personId = personIdList.get(index);
				if (checkBoxList.get(index).isSelected()) {
					// checked: add person to tag
					try {
						tagManager.addToTag(tag, personId);
					} catch (IllegalArgumentException e) {
						// tag already contains this person
					}
				} else {
					// unchecked: remove person from tag
					try {
						tagManager.removeFromTag(tag, personId);
					} catch (IllegalArgumentException e) {
						// tag does not contain this person
					}
				}
			}
			
			// save the tag manager
			try {
				tagManager.save();
			} catch (IOException e) {
				LOGGER.severe("TagAllPersonsDialog: cannot save tag manager");
				e.printStackTrace();
			}
		}
		
		
		/**
		 * Override method dispose to save modifications of tags before closing
		 */
		@Override
		public void dispose() {
			saveTag();
			super.dispose();
		}
	}

}
