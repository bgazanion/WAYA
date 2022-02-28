package waya.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.formdev.flatlaf.FlatIntelliJLaf;

import waya.engine.Person;
import waya.engine.PersonManager;
import waya.engine.TagManager;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getPackage().getName());
	private ResourceBundle messages = ResourceBundle.getBundle("waya.gui.resources.messages");
	private static final String PROP_USER_CONFIG_FILE = "waya_user_config_file";
	private static final File DATA_DIR = new File("data");
	private static final int ID_LENGTH = 6;
	private static final int WINDOW_WIDTH = 800;
	private static final int WINDOW_HEIGHT = 600;
	private static final double SIDE_PANEL_RELATIVE_SIZE = 0.3;
	private static final int MIN_WIDTH = 1100;
	private static final int MIN_HEIGHT = 400;
	private static final int NUMBER_SERVICES = 2;
	private static PersonManager personManager;
	private static TagManager tagManager;
	
	private JPanel sidePanel;
	private JPanel centerPanel;
	private TagManager tm;
	private PersonManager pm;
	private PersonPanel currentPersonPanel;
	private PersonListPanel personListPanel;
	private TagsFilterPanel tagsFilterPanel;
	private File dataDirectory;
	
	public MainFrame(TagManager i_tm, PersonManager i_pm, File directory) {
		LOGGER.finest("MainFrame - constructor");
		tm = i_tm;
		pm = i_pm;
		dataDirectory = directory;

		LOGGER.finest("MainFrame: personListPanel");
		personListPanel = new PersonListPanel(this, pm, tm);
		LOGGER.finest("MainFrame: tagsFilterPanel");
		tagsFilterPanel = new TagsFilterPanel(this, tm);
		
		LOGGER.finest("MainFrame: sidePanel");
		sidePanel = new JPanel(new BorderLayout());
		sidePanel.add(tagsFilterPanel, BorderLayout.PAGE_END);
		JScrollPane personScrollPane = new JScrollPane(personListPanel, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sidePanel.add(personScrollPane, BorderLayout.CENTER);
		LOGGER.finest("MainFrame: sidePanel -> done");
		
		currentPersonPanel = null;
		
		centerPanel = new JPanel(new BorderLayout());
		
		LOGGER.finest("MainFrame: mainPanel");
		JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidePanel, centerPanel);
		mainPanel.setDividerLocation(SIDE_PANEL_RELATIVE_SIZE);
		setContentPane(mainPanel);
		LOGGER.finest("MainFrame: mainPanel -> done");
		
		// at opening, display the help panel
		displayHomePanel();
		
		// action on close
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		pack();
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		LOGGER.finest("MainFrame - constructor - end");
		LOGGER.fine("Waya ready");
	}
	
	
	@Override
	public void dispose() {
		LOGGER.finest("Exit main frame");
		for (Handler handler : LOGGER.getHandlers()) {
			handler.close();
		}
		super.dispose();
	}
	
	
	public void openPersonPanel(String personId) {		
		if (pm.containsPersonId(personId)) {
			closePersonPanel(false);
			Person person = pm.getPerson(personId);
			currentPersonPanel = new PersonPanel(this, person, pm, tm, dataDirectory);
			centerPanel.removeAll();
			centerPanel.add(currentPersonPanel, BorderLayout.CENTER);
			centerPanel.revalidate();
			centerPanel.repaint();
		}
	}
	
	
	public void closePersonPanel(boolean personIsDeleted) {
		if (currentPersonPanel != null) {
			// save content
			if (personIsDeleted == false) {
				currentPersonPanel.save();
			}
			
			// update the person list panel if the name has changed
			if (currentPersonPanel.nameHasChanged() || personIsDeleted) {
				refreshPersonList();
			}
			
			// unselect the person in the list panel
			personListPanel.unselectPerson();
			
			// delete panel and clean content
			centerPanel.removeAll();
			currentPersonPanel = null;
			centerPanel.revalidate();
			centerPanel.repaint();
			// display help panel
			displayHomePanel();
		}
	}
	
	
	public void refreshPersonList() {
		personListPanel.updateListContent();
	}
	
	
	public void refreshTagFilter() {
		tagsFilterPanel.updateTags();
	}
	
	
	public void setPersonFilterTag(boolean selectAll, String newTag) {
		personListPanel.filterByTag(selectAll, newTag);
	}
	
	
	public void createPerson() {
		// create person
		String personId = pm.createPersonId();
		Person person = new Person(personId, messages.getString("new_person_default_name"));
		pm.addPerson(person);
		
		// close the panel of the current person, if any, and open the panel of the new person
		closePersonPanel(false);
		refreshPersonList();
		openPersonPanel(personId);
	}
	
	
	public void displayHomePanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		
		// Intro text
		JLabel introLabel = new JLabel("Welcome in Waya");
		constraints.gridy = 0;
		panel.add(introLabel, constraints);
		
		// create a person
		JButton createPersonButton = new JButton(messages.getString("button_create_person"));
		createPersonButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createPerson();
			}
		});
		constraints.gridy = 1;
		panel.add(createPersonButton, constraints);
		
		// edit tags
		JButton editTagsButton = new JButton(messages.getString("button_open_edit_tags"));
		editTagsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayEditTagsPanel();
			}
		});
		constraints.gridy = 2;
		panel.add(editTagsButton, constraints);
		
		// help
		JButton helpButton = new JButton(messages.getString("button_open_help"));
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayHelpPanel();
			}
		});
		constraints.gridy = 3;
		panel.add(helpButton, constraints);
		
		// about
		JButton aboutButton = new JButton(messages.getString("button_open_about"));
		aboutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new AboutDialog(MainFrame.this);
			}
		});
		constraints.gridy = 4;
		panel.add(aboutButton, constraints);
		
		centerPanel.removeAll();
		centerPanel.add(panel, BorderLayout.CENTER);
		centerPanel.revalidate();
		centerPanel.repaint();
	}
	
	
	private void displayHelpPanel() {
		
		// create help panel and display it in the center panel
		HelpPanel helpPanel = new HelpPanel(this);
		
		centerPanel.removeAll();
		centerPanel.add(helpPanel, BorderLayout.CENTER);
		centerPanel.revalidate();
		centerPanel.repaint();
	}
	
	
	private void displayEditTagsPanel() {
		
		// create panel to edit tags and display it in the center panel
		TagEditor tagEditor = new TagEditor(tagManager, personManager, this, DATA_DIR);
		
		centerPanel.removeAll();
		centerPanel.add(tagEditor, BorderLayout.CENTER);
		centerPanel.revalidate();
		centerPanel.repaint();
	}
	
	
	private void saveData() {
		try {
			pm.save();
			tm.save();
		} catch (IOException e) {
			LOGGER.severe("Cannot save data");
			e.printStackTrace();
		}
	}
	
	
	
	
	
	private static Properties loadConfig() {

		Properties config = new Properties();
		
		// load configuration file embedded in the sources
		try {
			InputStream input = MainFrame.class.getResourceAsStream("config.properties");
			config.load(input);
			input.close();
			// debug
			LOGGER.finest("config:");
			for (Object key : config.keySet()) {
				String strKey = (String) key;
				LOGGER.finest("  "+strKey+": "+config.getProperty(strKey));
			}
		} catch (FileNotFoundException e) {
			LOGGER.severe("Cannot find config file in Jar");
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("Cannot load config from Jar");
			e.printStackTrace();
		}
		
		// load user configuration file
		String userConfigFile = System.getProperty(PROP_USER_CONFIG_FILE);
		if (userConfigFile != null) {
			try {
				FileInputStream userInput = new FileInputStream(userConfigFile);
				config.load(userInput);
				// debug
				LOGGER.finest("user config:");
				for (Object key : config.keySet()) {
					String strKey = (String) key;
					LOGGER.finest(strKey+": "+config.getProperty(strKey));
				}
			} catch (FileNotFoundException | SecurityException e) {
				LOGGER.info("INFO: No user config file");
			} catch (IOException | IllegalArgumentException e) {
				LOGGER.warning("WARNING: Cannot load user config file");
				e.printStackTrace();
			}
		}
		
		return config;
	}
	
	
	
	public static void main(String[] argv) {
		
		boolean statusOk = true;
		
		// set log manager
		InputStream logInputStream = MainFrame.class.getResourceAsStream("logging.properties");
		try {
			LogManager.getLogManager().readConfiguration(logInputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// set language to english for the logs
		System.setProperty("user.langage", "en");
		LOGGER.fine("Waya start");
		
		// load config
		LOGGER.finest("Load config");
		Properties config = loadConfig();
		Boolean openFullscreen = Boolean.parseBoolean(config.getProperty("fullscreen"));
		String language = config.getProperty("language", "english").toLowerCase();
		
		// set locale from the language set in config file
		LOGGER.finest("language = "+language+" in config.properties");
		if (language.equals("french")) {
			LOGGER.finest("locale selected: french");
			Locale.setDefault(Locale.FRANCE);
		} else if (language == "english") {
			LOGGER.finest("locale selected: english");
			Locale.setDefault(Locale.US);
		} else {
			// default: english
			LOGGER.finest("locale selected: default = english");
			Locale.setDefault(Locale.US);
		}
		LOGGER.finest("Load config -> done");
		
		// check that the data directory exists, or create it
		LOGGER.finest("Data path="+DATA_DIR.getAbsolutePath());
		if (DATA_DIR.isDirectory() == false) {
			LOGGER.info("Create data directory: "+DATA_DIR.getAbsolutePath());
			try {
				DATA_DIR.mkdirs();
			} catch (Exception e) {
				// TODO: open popup
				LOGGER.severe("Error: cannot create data directory "+DATA_DIR.getAbsolutePath());
				return;		// TODO: remove
			}
		}
		
		
		// set UI from flatlaf in a dedicated thread
		ExecutorService flatlafExecutor = Executors.newSingleThreadExecutor();
		Future<?> flatlafFuture = flatlafExecutor.submit(new Runnable() {
			@Override
			public void run() {
				LOGGER.finest("Set UI from flatlaf");
				FlatIntelliJLaf.setup();
			}
		});
		
		
		// executor service for data loading
		ExecutorService dataExecutorService = Executors.newFixedThreadPool(NUMBER_SERVICES);
		
		// load the person manager in a dedicated thread
		Future<?> pmLoadFuture = dataExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				LOGGER.finest("Load the person manager");
				try {
					personManager = PersonManager.load(DATA_DIR, ID_LENGTH);
				} catch (Exception e) {
					LOGGER.info("Cannot load person files");
					try {
						personManager = new PersonManager(ID_LENGTH, DATA_DIR);
					} catch (IOException e2) {
						LOGGER.severe("Cannot create a data dir at "+DATA_DIR.getPath());
						LOGGER.severe("Exiting");
						return;
					}
				}
			}
		});
		
		// load the tag manager in a dedicated thread
		Future<?> tmLoadFuture = dataExecutorService.submit(new Runnable() {
			@Override
			public void run() {
				LOGGER.finest("Load the tag manager");
				try {
					tagManager = TagManager.load(DATA_DIR);
				} catch (Exception e) {
					LOGGER.info("Cannot load tags file");
					try {
						tagManager = new TagManager(DATA_DIR);
					} catch (IOException e2) {
						// Error: we cannot access the data directory -> exit
						LOGGER.severe("Cannot create a data dir at "+DATA_DIR.getPath());
						LOGGER.severe("Exiting");
						return;
					}
				}
			}
		});

		
		// make sure that the tag manager does not reference person that don't exist
		// -> that would create problems for the creation of the list of persons in PersonListPanel
		
		// - wait until the person manager and the tag manager are loaded
		LOGGER.finest("join the data loading threads");
		dataExecutorService.shutdown();
		try {
			dataExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			LOGGER.finest("data loading threads are terminated");
		} catch (InterruptedException e) {
			LOGGER.severe("Error: cannot load or create the person manager or the tag manager. Exiting");
			e.printStackTrace();
			statusOk = false;
		}
		
//		try {
//			pmLoadFuture.get();
//			LOGGER.finest("Load the person manager -> done");
//		} catch (InterruptedException | ExecutionException e) {
//			LOGGER.severe("Error: cannot load or create the person manager. Exiting");
//			e.printStackTrace();
//			return;
//		}
//		
//		try {
//			tmLoadFuture.get();
//			LOGGER.finest("Load the tag manager -> done");
//		} catch (InterruptedException | ExecutionException e) {
//			LOGGER.severe("Error: cannot load or create the tag manager. Exiting");
//			e.printStackTrace();
//			return;
//		}
		
		
		LOGGER.finest("Check the tags");
		Set<String> personIds = personManager.getPersonIds();
		List<String> personIdToRemove = new LinkedList<String>();
		for (String tag : tagManager.getTags()) {
			List<String> personIdsInTag = tagManager.getContent(tag);
			for (String idInTag : personIdsInTag) {
				if (!personIds.contains(idInTag)) {
					if (!personIdToRemove.contains(idInTag)) {
						personIdToRemove.add(idInTag);
					}
				}
			}
		}
		if (personIdToRemove.size() > 0) {
			for (String personId : personIdToRemove) {
				LOGGER.finest("DEBUG: remove non-existing person Id from the tag manager: "+personId);
				tagManager.removeFromAllTags(personId);
			}
			try {
				tagManager.save();
			} catch (Exception e) {
				LOGGER.severe("Cannot save tag manager");
				e.printStackTrace();
			}
		}
		LOGGER.finest("Check the tags -> done");
		
//		// set UI from flatlaf
//		LOGGER.finest("Set UI from flatlaf");
//		try {
//			UIManager.setLookAndFeel(new FlatIntelliJLaf());
//		} catch (Exception e) {
//			LOGGER.warning("Cannot use FlatLaf look and feel");
//		}
		
		// wait until flatlaf is loaded
		try {
			flatlafFuture.get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.warning("Cannot use FlatLaf look and feel");
			e.printStackTrace();
		}
		flatlafExecutor.shutdown();
		
		// open frame
		// - future improvement: use config
		//   MainFrame frame = new MainFrame(tm, pm, DATA_DIR, config);
		if (statusOk) {
			LOGGER.finest("Open the frame");
			MainFrame frame = new MainFrame(tagManager, personManager, DATA_DIR);
			frame.setVisible(true);
			// set preferred size
			frame.setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
			// maximize
			if (openFullscreen) {
				frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			}
		}
	}
	
}
