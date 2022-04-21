package waya.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class HelpPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getPackage().getName());
	private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("waya.gui.resources.messages");
	private static final String CLOSE_BUTTON_LABEL = MESSAGES.getString("button_close_help");

	
	private MainFrame mainFrame;
	private JEditorPane textPane;
	
	public HelpPanel(MainFrame frame) {
		mainFrame = frame;
		String localeString = getLocale().toString();
		URL url = HelpPanel.class.getResource("resources/help_"+localeString+".html");
		String text = "";
		InputStream inputStream = HelpPanel.class.getResourceAsStream("resources/help_"+localeString+".html");
		if (inputStream == null) {
			LOGGER.warning("No help file found for locale "+localeString);
		} else {
			try (InputStreamReader streamReader = new InputStreamReader(inputStream);
				 BufferedReader bufferedReader = new BufferedReader(streamReader)) {
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					text += line;
				}
			} catch (IOException e) {
				LOGGER.severe("Cannot read help file");
				e.printStackTrace();
			}
		}
		
		// handle image: replace by the path of the resource
		
//		URL personPanelScreenUrl = HelpPanel.class.getResource("resources/personPanelScreenshot_"+localeString+".png");
//		if (personPanelScreenUrl != null) {
//			LOGGER.info("person panel screenshot resource OK");
//			text = text.replace("__IMG_PERSON_PANEL_SCREENSHOT__", personPanelScreenUrl.toString());
//		}
//		URL homeScreenUrl = HelpPanel.class.getResource("resources/homeScreenshot_"+localeString+".png");
//		if (homeScreenUrl != null) {
//			LOGGER.info("Home screenshot resource OK");
//			text = text.replace("__IMG_HOME_SCREENSHOT__", homeScreenUrl.toString());
//			System.out.println(text);
//		}
//		URL personInfoScreenUrl = HelpPanel.class.getResource("resources/personInfoScreenshot_"+localeString+".png");
//		if (personInfoScreenUrl != null) {
//			LOGGER.info("person info screenshot resource OK");
//			text = text.replace("__IMG_PERSON_INFO_SCREENSHOT__", personInfoScreenUrl.toString());
//		}
			
		textPane = new JEditorPane();
		textPane.setContentType("text/html");
		textPane.setText(text);
		textPane.setEditable(false);
		
		JScrollPane scrollPane = new JScrollPane(textPane);
		
		JButton closeButton = new JButton(CLOSE_BUTTON_LABEL);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainFrame.displayHomePanel();
			}
		});
		
		JPanel bottomLine = new JPanel();
		bottomLine.add(closeButton);
		
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		add(bottomLine, BorderLayout.PAGE_END);
	}
	
}
