package waya.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import waya.engine.FileTools;
import waya.engine.Information;
import waya.engine.Person;
import waya.engine.PersonManager;
import waya.engine.TagManager;

public class PersonPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(PersonPanel.class.getPackage().getName());
	private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("waya.gui.resources.messages");
	private static final String ADD_INFO_BUTTON_LABEL = MESSAGES.getString("button_add_info");
	private static final int INFO_NAME_COLUMNS = 20;
	private static final int IMAGE_WIDTH = 150;
	private static final int IMAGE_HEIGHT = 150;
	private static final String IMAGE_END = "_image.png";
	
	private PersonManager personManager;
	private TagManager tagManager;
	private Person person;
	private JPanel headerPanel;
	private JPanel infoPanel;
	private JScrollPane infoScrollPane;
	private JTextField nameArea;
	private ArrayList<ArrayList<Integer>> infoComponentsIndices;
	private AddInformationButton addInformationButton;
	private MainFrame mainFrame;
	private File imageFile;
	private String oldName;
	private File dataDirectory;
	private JPanel bottomPanel;
	
	
	public PersonPanel(MainFrame frame, Person person, PersonManager pm, TagManager tm, 
			File dataDir) {
		this.person = person;
		this.personManager = pm;
		this.tagManager = tm;
		this.mainFrame = frame;
		this.dataDirectory = dataDir;
		this.imageFile = new File(dataDir, person.getId()+IMAGE_END);
		this.oldName = this.person.getName();
		infoComponentsIndices = new ArrayList<ArrayList<Integer>>();
		
		// 
		setLayout(new BorderLayout());
		
		// --------------------------------------------------------------------
		// header panel: name & edit/done button
		// --------------------------------------------------------------------
		headerPanel = new JPanel(new GridBagLayout());
		
		// image
		GridBagConstraints headerConstraints = new GridBagConstraints();
		headerConstraints.gridy = 0;
		headerConstraints.gridx = 0;
		headerConstraints.weightx = 0;
		headerConstraints.fill = GridBagConstraints.VERTICAL;
		headerConstraints.gridheight = 3;
		ImageViewer imageViewer = new ImageViewer(imageFile, IMAGE_WIDTH, IMAGE_HEIGHT);
		headerPanel.add(imageViewer, headerConstraints);
		headerConstraints.gridheight = 1;
		
		// name
		headerConstraints.gridx = 1;
		headerConstraints.weightx = 1;
		headerConstraints.anchor = GridBagConstraints.LINE_START;
		headerConstraints.fill = GridBagConstraints.HORIZONTAL;
		headerConstraints.gridwidth = GridBagConstraints.REMAINDER;
		nameArea = new JTextField(person.getName());
		nameArea.setFont(nameArea.getFont().deriveFont(24.0f));
		headerPanel.add(nameArea, headerConstraints);
		headerConstraints.gridwidth = 1;
		
		// close button
		headerConstraints.gridy = 1;
		headerConstraints.gridx = 3;
		headerConstraints.weightx = 0;
		headerConstraints.anchor = GridBagConstraints.LINE_START;
		headerConstraints.fill = GridBagConstraints.NONE;
		JButton closeButton = new JButton(MESSAGES.getString("button_close_person"));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainFrame.closePersonPanel(false);
			}
		});
		headerPanel.add(closeButton, headerConstraints);
		
		// tags button
		headerConstraints.gridy = 2;
		headerConstraints.gridx = 3;
		headerConstraints.weightx = 0;
		headerConstraints.anchor = GridBagConstraints.LINE_START;
		headerConstraints.fill = GridBagConstraints.NONE;
		JButton tagsButton = new JButton(MESSAGES.getString("button_set_tag_person"));
		tagsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PersonTagDialog(mainFrame, tagManager, person.getId());
			}
		});
		headerPanel.add(tagsButton, headerConstraints);
		
		// delete person button
		headerConstraints.gridy = 2;
		headerConstraints.gridx = 4;
		headerConstraints.weightx = 0;
		headerConstraints.anchor = GridBagConstraints.LINE_END;
		headerConstraints.fill = GridBagConstraints.NONE;
		JButton deleteButton = new JButton(MESSAGES.getString("button_delete_person"));
		deleteButton.setBackground(Color.RED);
		deleteButton.setContentAreaFilled(true);
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openDeletePersonDialog();
			}
		});
		headerPanel.add(deleteButton, headerConstraints);

		// image selector
		headerConstraints.gridy = 3;
		headerConstraints.gridx = 0;
		headerConstraints.weightx = 0;
		headerConstraints.fill = GridBagConstraints.HORIZONTAL;
		headerPanel.add(
				new ImageSelectorButton(mainFrame, imageViewer, imageFile), headerConstraints);
		
		
		add(headerPanel, BorderLayout.PAGE_START);
		
		
		// --------------------------------------------------------------------
		// center panel: information
		// --------------------------------------------------------------------
		infoPanel = new JPanel(new GridBagLayout());
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(infoPanel, BorderLayout.PAGE_START);
		
		infoScrollPane = new JScrollPane(centerPanel);
		infoScrollPane.repaint();
		infoScrollPane.revalidate();
		
		add(infoScrollPane, BorderLayout.CENTER);
		
		updateInformationPanel();
		
		// --------------------------------------------------------------------
		// bottom panel
		// --------------------------------------------------------------------
		
		bottomPanel = new JPanel();
		add(bottomPanel, BorderLayout.PAGE_END);

		addInformationButton = new AddInformationButton();
		bottomPanel.add(addInformationButton);
		
		
		// debug TODO
		//centerPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
//		headerPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
//		infoPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
//		infoScrollPane.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
		
		//revalidate();
		
		//setPreferredSize(new Dimension(800, 600));
		//setMinimumSize(new Dimension(1000, 300));
	}
	
	
	public boolean nameHasChanged() {
		return (!oldName.equals(nameArea.getText()));
	}
	
	
	public void save() {
		LOGGER.finest("save");
		// TODO: save only if things changed
		//
		// update object
		updatePersonObjectFromGui();
		// save updated person in the person manager
		personManager.setPerson(person.getId(), person);
		// save to file
		try {
			personManager.saveSinglePerson(person.getId());
		} catch (IOException e) {
			LOGGER.severe("Cannot save to file");
			e.printStackTrace();
		}
		LOGGER.finest("save -> done");
	}
	
	
	public void deletePerson() {
		LOGGER.finest("delete");
		
		// update objects
		personManager.removePerson(person.getId());
		tagManager.removeFromAllTags(person.getId());
		try {
			tagManager.save();
		} catch (Exception e) {
			LOGGER.severe("Cannot save tag manager");
			e.printStackTrace();
		}
		
		// refresh UI
		mainFrame.refreshPersonList();
		mainFrame.closePersonPanel(true);
		
		// update files
		try {
			personManager.removePersonFile(dataDirectory, person.getId());
		} catch (IOException e) {
			LOGGER.severe("Cannot delete person's file");
			e.printStackTrace();
		}
		LOGGER.finest("delete -> done");
	}
	
	
	/**
	 * Open the dialog to request confirmation before deleting the person
	 */
	private void openDeletePersonDialog() {
		int optionType = JOptionPane.YES_NO_OPTION;
		int messageType = JOptionPane.WARNING_MESSAGE;
		String message = MESSAGES.getString("dialog_delete_person");
		int result = JOptionPane.showConfirmDialog(mainFrame, message, "", optionType, messageType);
		if (result == JOptionPane.YES_OPTION) {
			deletePerson();
		}
	}
	
	
	/**
	 * Store the content of the information panel in the person object
	 */
	private void updatePersonObjectFromGui() {
		
		person.setName(nameArea.getText());
		
		for (int infoIndex=0; infoIndex<person.getNumberOfInformations(); infoIndex++) {
			
			// get the indices of info components from infoComponentsIndices array
			// -> name, value0, value1, ...
			LOGGER.finest("savePersonInformation");
			LOGGER.finest("index: "+infoIndex);
			int numberOfValues = infoComponentsIndices.get(infoIndex).size() - 1;
			
			// get name and create information
			int nameIndex = infoComponentsIndices.get(infoIndex).get(0);
			JTextArea nameComponent = (JTextArea) infoPanel.getComponent(nameIndex);
			Information information = new Information(nameComponent.getText());
			
			// get values
			for (int valueIndex=0; valueIndex<numberOfValues; valueIndex++) {
				int componentIndex = infoComponentsIndices.get(infoIndex).get(valueIndex+1);
				JTextArea valueComponent = (JTextArea) infoPanel.getComponent(componentIndex);
				information.addValue(valueComponent.getText());
			}
			
			// store information in the person object, replacing existing one at given index
			person.changeInformation(infoIndex, information);			
		}
	}
	
	
	private void addInformation() {
		// save the panel's content
		updatePersonObjectFromGui();
		
		// create new information
		Information info = new Information("");
		person.addInformation(info);
		
		// update panel
		updateInformationPanel();
	}
	
	
	private void removeInformation(int infoIndex) {
		// save the panel's content
		updatePersonObjectFromGui();
		
		// remove information from person object
		person.removeInformation(infoIndex);
		
		// update information panel
		updateInformationPanel();
	}
	
	
	private void addInformationValue(int infoIndex, String value) {
		// save the panel's content
		updatePersonObjectFromGui();
		
		// get Information object and add value
		Information info = person.getInformation(infoIndex);
		info.addValue(value);
		
		// update panel
		updateInformationPanel();
	}
	
	
	private void removeInformationValue(int infoIndex, int infoValueIndex) {
		// save the panel's content
		updatePersonObjectFromGui();
		
		// get Information object and remove value
		Information info = person.getInformation(infoIndex);
		info.removeValue(infoValueIndex);
		
		// update panel
		updateInformationPanel();
	}
	
	
	private void updateInformationPanel() {
		// remove existing elements from the panel
		infoPanel.removeAll();
		
		// reset the list of components' index
		infoComponentsIndices.clear();
		
		// create grid constraints 
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		//constraints.weighty = 1;
		//constraints.anchor = GridBagConstraints.NORTH;
		constraints.insets = new java.awt.Insets(2, 2, 2, 2);
		
		// build elements for each information
		for (int infoIndex=0; infoIndex<person.getNumberOfInformations(); infoIndex++) {
			
			// add entry for the components of this info
			ArrayList<Integer> componentsIndices = new ArrayList<Integer>();
			
			// get information
			Information info = person.getInformation(infoIndex);
			String name = info.getName();
			
			// button to remove information
			constraints.gridx = 0;
			constraints.weightx = 0;
			constraints.fill = GridBagConstraints.NONE;
			infoPanel.add(new RemoveInformationButton(infoIndex), constraints);
			
			// info name
			constraints.gridx = 1;
			constraints.weightx = 0;
			constraints.fill = GridBagConstraints.NONE;
			LOGGER.finest("Name: "+name);
			JTextArea nameArea = new JTextArea(name);
			nameArea.setColumns(INFO_NAME_COLUMNS);
			nameArea.setLineWrap(true);
			infoPanel.add(nameArea, constraints);
			componentsIndices.add(infoPanel.getComponentCount()-1);
			
			// info values & related buttons
			List<String> values = info.getValues();
			if (values.isEmpty()) {
				// create an empty value
				info.addValue("");
				values = info.getValues();
			}

			// NB: decrement lineNumber to simplify addition of new lines
			constraints.gridy--;
			for(int valueIndex=0; valueIndex<values.size(); valueIndex++) {
				constraints.gridy++;
				
				// text area containing the value
				constraints.gridx = 2;
				constraints.weightx = 1;
				constraints.fill = GridBagConstraints.HORIZONTAL;
				JTextArea textArea = new JTextArea(values.get(valueIndex));
				textArea.setLineWrap(true);
//				textArea.setColumns(INFO_VALUE_COLUMNS);
				infoPanel.add(textArea, constraints);
				componentsIndices.add(infoPanel.getComponentCount()-1);
				
				// button to remove the value
				constraints.gridx = 3;
				constraints.weightx = 0;
				constraints.fill = GridBagConstraints.NONE;
				infoPanel.add(new RemoveInfoValueButton(infoIndex, valueIndex), constraints);
			}
			
			// button to add values
			constraints.gridx = 4;
			constraints.weightx = 0;
			constraints.fill = GridBagConstraints.NONE;
			infoPanel.add(new AddInfoValueButton(infoIndex), constraints);
			
			// store the components indices for the info
			infoComponentsIndices.add(componentsIndices);
			
			// prepare next line
			constraints.gridy++;
		}
		
		// refresh panel object
		infoPanel.revalidate();
		infoPanel.repaint();
	}
	
	
	private class AddInformationButton extends JButton {
		
		private static final long serialVersionUID = 1L;
		
		public AddInformationButton() {
			super(ADD_INFO_BUTTON_LABEL);
			this.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					addInformation();
				}
			});
		}
	}
	
	
	private class RemoveInformationButton extends JButton {
		
		private static final long serialVersionUID = 1L;
		private int infoIndex;
		
		public RemoveInformationButton(int inputInfoIndex) {
			super(MESSAGES.getString("button_remove_info"));
			infoIndex = inputInfoIndex;
			this.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// remove information
					LOGGER.finest("Button pressed: remove info #"+infoIndex);
					int input = JOptionPane.showConfirmDialog(getRootPane(), 
							MESSAGES.getString("dialog_remove_info"),
							MESSAGES.getString("dialog_confirm_title"), 
							JOptionPane.OK_CANCEL_OPTION);
					if (input == JOptionPane.OK_OPTION) {
						removeInformation(infoIndex);
					}
					
					LOGGER.finest("Button pressed: remove info #"+infoIndex+" -> over");
				}
			});
		}
	}
	
	
	private class AddInfoValueButton extends JButton {
		
		private static final long serialVersionUID = 1L;
		private int infoIndex;
		
		public AddInfoValueButton(int inputInfoIndex) {
			super(MESSAGES.getString("button_add_value"));
			infoIndex = inputInfoIndex;
			this.addActionListener(new ActionListener() {		
				@Override
				public void actionPerformed(ActionEvent e) {
					addInformationValue(infoIndex, "");
				}
			});
		}
	}
	
	
	private class RemoveInfoValueButton extends JButton {
		
		private static final long serialVersionUID = 1L;
		private int infoIndex;
		private int infoValueIndex;
		
		public RemoveInfoValueButton(int inputInfoIndex, int inputInfoValueIndex) {
			super(MESSAGES.getString("button_remove_value"));
			infoIndex = inputInfoIndex;
			infoValueIndex = inputInfoValueIndex;
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int input = JOptionPane.showConfirmDialog(getRootPane(), 
							MESSAGES.getString("dialog_remove_info"),
							MESSAGES.getString("dialog_confirm_title"),
							JOptionPane.OK_CANCEL_OPTION);
					if (input == JOptionPane.OK_OPTION) {
						removeInformationValue(infoIndex, infoValueIndex);
					}
				}
			});
		}
	}
	
	
	private class ImageSelectorButton extends JButton {
		private static final long serialVersionUID = 1L;
		private ImageViewer imageViewer;
		private JFrame parentFrame; 
		private File imageFile;
		
		public ImageSelectorButton(JFrame parent, ImageViewer iImageViewer, File importFile) {
			super(MESSAGES.getString("button_select_text"));
			imageViewer = iImageViewer;
			parentFrame = parent;
			imageFile = importFile;
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// file dialog
					FileDialog dialog = new FileDialog(parentFrame, 
							MESSAGES.getString("dialog_file_open_text"), FileDialog.LOAD);
					dialog.setVisible(true);
					
					// update using the image
					if (dialog.getFile() != null && dialog.getDirectory() != null) {
						File source = new File(dialog.getDirectory(), dialog.getFile());
						LOGGER.finest("ImageSelectorButton - source = "+source.toString());
						int width = imageViewer.getWidth();
						int height = imageViewer.getHeight();
						LOGGER.finest("ImageSelectorButton - import file = "+imageFile.toString());
						FileTools.importAndResizeImage(source, width, height, imageFile);
						imageViewer.updateImage();
					}
				}
			});
		}
		
	}
}
