package waya.gui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import waya.engine.PersonManager;
import waya.engine.TagManager;

public class PersonListPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(PersonListPanel.class.getPackage().getName());
	private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("waya.gui.resources.messages");
	private static final String TAG_ALL = MESSAGES.getString("label_filter_all_tags");
	private PersonManager personManager;
	private TagManager tagManager;
	private JPanel listPanel;
	private String currentTag;
	private boolean displayAllPersons;
	private Comparator<String> personIdComparator;
	private MainFrame parent;
	private List<String> personIdsList;
	private JList<String> personListWidget;
	private DefaultListModel<String> personListData;
	private String selectedPersonId;
	
	public PersonListPanel(MainFrame mf, PersonManager pm, TagManager tm) {
		
		listPanel = new JPanel(new BorderLayout());
		personIdsList = new ArrayList<String>();
		parent = mf;
		personManager = pm;
		tagManager = tm;
		currentTag = TAG_ALL;
		displayAllPersons = true;
		selectedPersonId = "";
		
		// list
		personListData = new DefaultListModel<String>();
		personListWidget = new JList<>();
		personListWidget.setModel(personListData);
		personListWidget.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		personListWidget.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				JList<String> list = (JList<String>) event.getSource();
				if (!list.isSelectionEmpty()) {
					if (!event.getValueIsAdjusting()) {
						// to avoid multiple activation from a single user action
						int index = list.getMinSelectionIndex();
						String newSelectedPersonId = personIdsList.get(index);
						if (newSelectedPersonId != selectedPersonId) {
							selectedPersonId = newSelectedPersonId;
							parent.openPersonPanel(newSelectedPersonId);
						}
					}
				}
			}
		});
		
		// create a comparator to sort personIds after the real name
		personIdComparator = new Comparator<String>() {
			@Override
			public int compare(String id1, String id2) {
				try {
					String name1 = personManager.getPerson(id1).getName();
					String name2 = personManager.getPerson(id2).getName();
					return compare(name1, name2);
				} catch (IllegalArgumentException e) {
					return 0;
				}
			}
		};
		
		// main layout
		setLayout(new BorderLayout());
		listPanel.add(personListWidget, BorderLayout.CENTER);
		add(listPanel, BorderLayout.PAGE_START);
		
		// build content
		updateListContent();
	}
	
	
	public void filterByTag(boolean displayAll, String tag) {
		// check if filter has changed
		if (displayAll == displayAllPersons && tag == currentTag) {
			// nothing to do
			return;
		} else {
			displayAllPersons = displayAll;
			currentTag = tag;
			updateListContent();
		}
	}
	
	
	public void updateListContent() {
		
		personIdsList.clear();
		personListData.clear();
		
		// create the list of person Ids, sorted after the person name
		List<String> personIds; 
		if (displayAllPersons || !tagManager.hasTag(currentTag)) {
			// all persons
			personIds = new LinkedList<String>(personManager.getPersonIds());
		} else {
			// only persons matching the current tag
			personIds = new LinkedList<String>(tagManager.getContent(currentTag));
		}
		Collections.sort(personIds, personIdComparator);
		personIdsList.addAll(personIds);
		
		// 
		personListData = new DefaultListModel<>();
		for (String id : personIds) {
			// we add a space after the name so that the element is correctly rendered
			// even if the name is empty (by default empty name = empty/flat element)
			String name = personManager.getPerson(id).getName()+" ";
			personListData.addElement(name);
		}
		personListWidget.setModel(personListData);
		// update the selected person
		//selectPerson(selectedPersonId);
		
		revalidate();
		repaint();
	}
	
	
	/**
	 * Mark the person as selected in the panel
	 * @param personId
	 */
	/*
	public void selectPerson(String personId) {
		unselectPerson();
		if ((personId != null) && (personButtonMap.containsKey(personId))) {
			selectedPersonId = personId;
			personButtonMap.get(personId).setSelected(true);
		}
	}
	*/
	
	
	/**
	 * Unselect the person which is currently selected (if any)
	 */
	public void unselectPerson() {
		personListWidget.clearSelection();
	}
	
	
	/**
	 * Change the name displayed in the panel for the currently selected person
	 * @param newName
	 */
	public void changeSelectedPersonLabel(String newName) {
		if (!personListWidget.isSelectionEmpty()) {
			personListData.setElementAt(newName, personListWidget.getMinSelectionIndex());
		}			
	}
	
	
	/**
	 * Create a button for the person with given Id (Id cannot be null)
	 * @param personId
	 * @return button
	 */
	/*
	private JToggleButton createButton(String personId) {
		String personName = personManager.getPerson(personId).getName();
		JToggleButton button = new JToggleButton(personName);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedPersonId != personId) {
					// call method from parent main frame to open selected person
					unselectPerson();
					parent.openPersonPanel(personId);
				} else {
					// undo button's unselection
					selectPerson(personId);
				}
			}
		});
		return button;
	}
	*/
	
	
//	/**
//	 * Select a person and open its panel
//	 * @param personButton button associated to the person
//	 */
//	private void selectPersonAndOpen(PersonButton personButton) {
//		// unselect the person previously selected (if any)
//		unselectCurrentPerson();
//		
//		// mark the new person as selected
//		selectedPersonButton = personButton;
//		
//		// send order to open person's panel
//		if (personButton != null) {
//			parent.openPersonPanel(personButton.getPersonId());
//		}
//	}
	
//	/**
//	 * Unselect the person currently selected
//	 */
//	public void unselectCurrentPerson() {
//		if (selectedPersonButton != null)
//			selectedPersonButton.setSelected(false);
//	}
	
	
	
	
//	/**
//	 * Update the text of the selected person's button
//	 * @param newName
//	 */
//	public void renameSelectedPerson(String newName) {
//		if (selectedPersonButton != null) {
//			selectedPersonButton.setText(newName);
//		}
//	}
	
	

}
