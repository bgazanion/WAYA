package waya.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import waya.engine.TagManager;

public class TagsFilterPanel extends JPanel {

	private static final Logger LOGGER = Logger.getLogger(TagsFilterPanel.class.getPackage().getName());
	private static final long serialVersionUID = 1L;
	private JComboBox<String> tagSelector;
	private TagManager tagManager;
	private MainFrame mainFrame;
	private String tagSelected;
	private static ResourceBundle MESSAGES = ResourceBundle.getBundle("waya.gui.resources.messages");
	private static String TAG_ALL = MESSAGES.getString("label_filter_all_tags");
	
	public TagsFilterPanel(MainFrame mf, TagManager tm) {
		// fields
		mainFrame = mf;
		tagManager = tm;
		tagSelected = TAG_ALL;
		
		// layout
		setLayout(new GridBagLayout());
		GridBagConstraints filterConstraints = new GridBagConstraints();
		
		// title
		LOGGER.finest("TagsFilterPanel constructor: title");
		JLabel titleLabel = new JLabel(MESSAGES.getString("label_filter_tag_title")+" :   ");
		filterConstraints.gridx = 0;
		filterConstraints.gridy = 0;
		add(titleLabel, filterConstraints);
		LOGGER.finest("TagsFilterPanel constructor: title -> done");
		
		// tag selector
		LOGGER.finest("TagsFilterPanel constructor: tag selector");
		LOGGER.finest("TagsFilterPanel constructor: JComboBox");
		tagSelector = new JComboBox<String>();
		LOGGER.finest("TagsFilterPanel constructor: JComboBox -> done");
		tagSelector.setEditable(false);
		updateTags();
		filterConstraints.fill = GridBagConstraints.HORIZONTAL;
		filterConstraints.weightx = 1;
		filterConstraints.gridx = 0;
		filterConstraints.gridy = 1;
		add(tagSelector, filterConstraints);
		LOGGER.finest("TagsFilterPanel constructor: tag selector -> done");
		
		// apply button
		LOGGER.finest("TagsFilterPanel constructor: applyFilterButton");
		JButton applyFilterButton = new JButton(MESSAGES.getString("button_apply_filter"));
		applyFilterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processSelection();
				mainFrame.setPersonFilterTag(false, tagSelected);
			}
		});
		filterConstraints.fill = GridBagConstraints.NONE;
		filterConstraints.weightx = 0;
		filterConstraints.gridx = 2;
		filterConstraints.gridy = 1;
		add(applyFilterButton, filterConstraints);
		LOGGER.finest("TagsFilterPanel constructor: applyFilterButton -> done");
	}
	
	
	private void processSelection() {
		if (tagSelector.getSelectedIndex() > 0) {
			tagSelected = (String) tagSelector.getSelectedItem();
			LOGGER.finest("Tags filter: select '"+tagSelected+"'");
		} else {
			tagSelected = TAG_ALL;
			LOGGER.finest("Tags filter: select all");
		}
	}
	
	
	public void updateTags() {
		LOGGER.finest("TagsFilterPanel.updateTags - start");
		// new tags
		List<String> tags = new LinkedList<String>(tagManager.getTags());
		Collections.sort(tags);
		
		// update the content of the tag selector
		tagSelector.removeAllItems();
		// - add the empty tag with index 0: "- all -" tag used to select everything
		tagSelector.addItem(MESSAGES.getString("label_filter_all_tags"));
		// - add the tags from the tag manager
		for (String tagName : tags) {
			tagSelector.addItem(tagName);
		}
		
		// if the tag currently selected is not in the new list, use default "ALL" tag
		if (tagSelected != TAG_ALL && tags.contains(tagSelected) == false) {
			tagSelected = TAG_ALL;
			mainFrame.setPersonFilterTag(false, tagSelected);
		}
		LOGGER.finest("TagsFilterPanel.updateTags - end");
	}
}
