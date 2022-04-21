package waya.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import waya.engine.PersonManager;
import waya.engine.TagManager;

public class TagEditor extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getPackage().getName());
	private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("waya.gui.resources.messages");
	private static final String DELETE_TAG_LABEL = MESSAGES.getString("button_tags_editor_delete_tag");
	private static final String RENAME_TAG_LABEL = MESSAGES.getString("button_tags_editor_rename_tag");
	private static final String EDIT_PERSONS_LABEL = MESSAGES.getString("button_tags_editor_edit_persons");
	private static final String EDIT_PERSONS_ERROR = MESSAGES.getString("button_tags_editor_edit_persons_error");
	private static final String CLOSE = MESSAGES.getString("button_tags_editor_close");
	private static final String CREATE_TAG = MESSAGES.getString("button_tags_editor_create_tag");
	
	private static final int LABEL_PREF_WIDTH = 300;
	private static final int BORDER = 10;
	
	private JPanel tagPanel;
	private TagManager tagManager;
	private PersonManager personManager;
	private MainFrame mainFrame;
	private GridBagConstraints tagConstraints;
	private List<String> personIdList;
	private TagAllPersonsDialogBuilder tagPersonDialogBuilder;
	private File dataDir;

	
	public TagEditor(TagManager tm, PersonManager pm, MainFrame parentFrame, File dataDirectory) {
		tagManager = tm;
		personManager = pm;
		mainFrame = parentFrame;
		tagPanel = new JPanel(new GridBagLayout());
		tagConstraints = new GridBagConstraints();
		dataDir = dataDirectory;
		
		// button to close the editor
		JButton closeButton = new JButton(CLOSE);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainFrame.displayHomePanel();
			}
		});
		
		// button to create a tag
		JButton createTagButton = new JButton(CREATE_TAG);
		createTagButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String dialog_message = MESSAGES.getString("dialog_create_tag");
				String error_message = MESSAGES.getString("dialog_tag_exists");
				int messageType = JOptionPane.PLAIN_MESSAGE;
				String newTag = JOptionPane.showInputDialog(mainFrame, dialog_message, "", messageType);
				if (newTag != null) {
					// check that the input tag does not already exist
					if (tagManager.hasTag(newTag)) {
						// open a warning dialog
						JOptionPane.showMessageDialog(mainFrame, error_message, "", JOptionPane.WARNING_MESSAGE);
					} else {
						tagManager.addTag(newTag);
						LOGGER.fine("Create tag '"+newTag+"'");
						updateTagListPanel();
						mainFrame.refreshTagFilter();
						saveTagManager();
					}
				}
			}
		});
		
		
		// builder instance to create the dialogs to assign persons to a given tag
		tagPersonDialogBuilder = new TagAllPersonsDialogBuilder(mainFrame, tagManager, personManager);
		
		// bottom panel
		JPanel bottomPanel = new JPanel();
		JPanel innerBottomPanel = new JPanel(new GridLayout(0, 1));
		innerBottomPanel.add(createTagButton);
		innerBottomPanel.add(closeButton);
		bottomPanel.add(innerBottomPanel);
		
		// center panel: tags
		// -> ugly workarounds below to ensure top-left align
		JPanel innerInnerPanel = new JPanel(new BorderLayout());
		innerInnerPanel.add(tagPanel, BorderLayout.LINE_START);
		JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.add(innerInnerPanel, BorderLayout.PAGE_START);
		JScrollPane centerScrollPane = new JScrollPane(innerPanel);
		centerScrollPane.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
		
		// global layout
		setLayout(new BorderLayout());
		add(centerScrollPane, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.PAGE_END);
		revalidate();
		updateTagListPanel();
		
	}
	
	
	private void updateTagListPanel() {
		tagPanel.removeAll();
		
		List<String> tags = new LinkedList<>(tagManager.getTags());		
		Collections.sort(tags);
		
		tagConstraints.gridy = 0;
		for (String tag : tags) {
			
			// display label and checkbox
			tagConstraints.gridx = 0;
			tagConstraints.weightx = 0;
			tagConstraints.fill = GridBagConstraints.HORIZONTAL;
			tagPanel.add(new JLabel(tag), tagConstraints);
			
			tagConstraints.gridx = 1;
			tagConstraints.weightx = 0;
			tagConstraints.fill = GridBagConstraints.NONE;
			tagPanel.add(new DeleteTagButton(tag), tagConstraints);
			
			tagConstraints.gridx = 2;
			tagConstraints.weightx = 0;
			tagPanel.add(new RenameTagButton(tag), tagConstraints);
			
			tagConstraints.gridx = 3;
			tagConstraints.weightx = 0;
			tagPanel.add(new SetTagPersonsButton(tag), tagConstraints);
			
			tagConstraints.gridy += 1;
		}
		
		// add an invisible box to constrain the size of the first column (tag names)
		tagConstraints.gridx = 0;
		tagPanel.add(Box.createHorizontalStrut(LABEL_PREF_WIDTH), tagConstraints);
		
		revalidate();
		repaint();
	}
	
	
	/**
	 * Save the tag manager
	 */
	private void saveTagManager() {
		try {
			tagManager.save();
		} catch (IOException e) {
			LOGGER.severe("TagEditor: cannot save the tag manager");
			e.printStackTrace();
		}
	}

	
	/**
	 * Custom button to delete a tag
	 */
	private class DeleteTagButton extends JButton {
		
		private static final long serialVersionUID = 1L;
		private final String DIALOG_MESSAGE = MESSAGES.getString("dialog_delete_tag");

		private String tag;

		public DeleteTagButton(String targetTag) {
			super(DELETE_TAG_LABEL);
			tag = targetTag;
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					openDialog();
				}
			});
		}
		
		private void openDialog() {
			int optionType = JOptionPane.YES_NO_OPTION;
			int messageType = JOptionPane.WARNING_MESSAGE;
			int result = JOptionPane.showConfirmDialog(mainFrame, DIALOG_MESSAGE, "", optionType, messageType);
			if (result == JOptionPane.YES_OPTION) {
				tagManager.removeTag(tag);
				LOGGER.fine("Remove tag: '"+tag+"'");
				updateTagListPanel();
				mainFrame.refreshTagFilter();
				saveTagManager();
			}	
		}
	}
	
	
	/**
	 * Custom button to rename a tag
	 */
	private class RenameTagButton extends JButton {
		
		private static final long serialVersionUID = 1L;
		private final String DIALOG_MESSAGE = MESSAGES.getString("dialog_rename_tag");
		private final String ERROR_MESSAGE = MESSAGES.getString("dialog_tag_exists");

		private String tag;
		
		public RenameTagButton(String targetTag) {
			super(RENAME_TAG_LABEL);
			tag = targetTag;
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					openDialog();
				}
			});
		}
		
		private void openDialog() {
			int messageType = JOptionPane.PLAIN_MESSAGE;
			String newTag = JOptionPane.showInputDialog(mainFrame, DIALOG_MESSAGE, "", messageType);
			if (newTag != null) {
				// check that the input tag does not already exist
				if (tagManager.hasTag(newTag)) {
					// open a warning dialog
					JOptionPane.showMessageDialog(mainFrame, ERROR_MESSAGE, "", JOptionPane.WARNING_MESSAGE);
				} else {
					tagManager.renameTag(tag, newTag);
					LOGGER.fine("Rename tag: '"+tag+"' -> '"+newTag+"'");
					updateTagListPanel();
					mainFrame.refreshTagFilter();
					saveTagManager();
				}
			}	
		}
	}
	
	
	private class SetTagPersonsButton extends JButton {
		
		private static final long serialVersionUID = 1L;
		private String tag;
		
		public SetTagPersonsButton(String targetTag) {
			super(EDIT_PERSONS_LABEL);
			tag = targetTag;
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					openSetPersonsDialog();
				}
			});
		}
		
		private JDialog openSetPersonsDialog() {
			JDialog dialog = null;
			try {
				dialog = tagPersonDialogBuilder.buildDialogForTag(tag);
			} catch (Exception e) {
				LOGGER.severe("Cannot open dialog to assign persons to tag '"+tag+"'");
				JOptionPane.showMessageDialog(mainFrame, EDIT_PERSONS_ERROR, "", JOptionPane.ERROR_MESSAGE);
			}
			return dialog;				
		}
	}
}
