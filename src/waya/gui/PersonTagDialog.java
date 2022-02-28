package waya.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import waya.engine.TagManager;

public class PersonTagDialog extends JDialog {

	private static final Logger LOGGER = Logger.getLogger(PersonTagDialog.class.getPackage().getName());
	private static final long serialVersionUID = 1L;
	private TagManager tagManager;
	private String personId;
	private JPanel tagListPanel;
	private boolean tagManagerNeedsUpdate;
	private final static int TEXT_FIELD_WIDTH = 20;
	private JButton newTagButton;
	private JTextField newTagField;

	public PersonTagDialog(Frame owner, TagManager tagManager, String personId) {
		super(owner, true);
		this.tagManager = tagManager;
		this.personId = personId;
		this.tagManagerNeedsUpdate = false;
		
		JPanel panel = new JPanel(new GridBagLayout());
		
		// button and text field to create a new tag
		newTagButton = new JButton("Create new");
		newTagButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				createTag();
			}
		});
		newTagField = new JTextField("", TEXT_FIELD_WIDTH);
		
		// tags panel
		tagListPanel = new JPanel();
		tagListPanel.setLayout(new GridLayout(0, 1));
		tagListPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		
		JScrollPane tagListScroll = new JScrollPane(tagListPanel);
		tagListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		tagListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		// close button
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		
		// insert elements in the panels
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.LINE_START;
		
		constraints.gridy = 0;
		constraints.gridx = 0;
		constraints.weightx = 0;
		constraints.fill = GridBagConstraints.NONE;
		panel.add(newTagButton, constraints);
		
		constraints.gridy = 0;
		constraints.gridx = 1;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(newTagField, constraints);

		constraints.gridy = 1;
		constraints.gridx = 0;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(tagListScroll, constraints);
		
		constraints.gridy = 2;
		constraints.gridx = 0;
		constraints.weightx = 0;
		constraints.anchor = GridBagConstraints.CENTER;

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(panel, BorderLayout.PAGE_START);
		mainPanel.add(closeButton, BorderLayout.PAGE_END);
		
		setContentPane(mainPanel);
		
		updateTagListPanel();
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		pack();
		setLocationRelativeTo(owner);
		//setSize(200, 200);
		setVisible(true);
	}
	
	
	/**
	 * Overridden method dispose: save tags before closing
	 */
	@Override
	public void dispose() {
		if (tagManagerNeedsUpdate) {
			try {
				tagManager.save();
			} catch (IOException e) {
				LOGGER.finest("Error: cannot save tags file");
				e.printStackTrace();
			}
		}
		super.dispose();
	}
	
	
	/**
	 * Action performed when the "new tag" button is used
	 */
	private void createTag() {
		String tag = newTagField.getText();
		if (! tag.isEmpty() && ! tag.matches("^\\s*$")) {	//TODO: use tag.isBlank() for Java 11+
			// create the tag if it doesn't already exist
			if (! tagManager.getTags().contains(tag)) {
				tagManager.addTag(tag);
			}
			// check the tag for this person
			tagManager.addToTag(tag, personId);
			// update tag's panel
			updateTagListPanel();
		}
		// clear text field
		newTagField.setText("");
	}
	
	
	/**
	 * Redraw the tags and their checkboxes
	 */
	private void updateTagListPanel() {
		tagListPanel.removeAll();
		
		List<String> tags = new LinkedList<>(tagManager.getTags());
		
		Collections.sort(tags);
		for (String tag : tags) {
			
			// create check box
			JCheckBox checkBox = new JCheckBox();
			// check box if the person has this tag
			if (tagManager.getContent(tag).contains(personId)) {
				checkBox.setSelected(true);
			}
			checkBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent event) {
					if (event.getStateChange() == ItemEvent.SELECTED) {
						tagManager.addToTag(tag, personId);
						tagManagerNeedsUpdate = true;
						LOGGER.finest("Add "+personId+" to tag "+tag);
					} else {
						tagManager.removeFromTag(tag, personId);
						tagManagerNeedsUpdate = true;
						LOGGER.finest("Remove "+personId+" from tag "+tag);
					}
				}
			});
			
			// display label and checkbox
			JPanel itemPanel = new JPanel(new BorderLayout());
			itemPanel.add(new JLabel(tag), BorderLayout.LINE_START);
			itemPanel.add(checkBox, BorderLayout.LINE_END);
			
			tagListPanel.add(itemPanel);
		}
		revalidate();
		repaint();
	}
}
