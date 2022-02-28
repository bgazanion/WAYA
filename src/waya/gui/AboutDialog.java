package waya.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class AboutDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getPackage().getName());
	private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("waya.gui.resources.messages");
	private static final String WAYA_VERSION = "V0.1.0";
	private static final String WAYA_YEARS = "2021";
	private static final int MIN_WIDTH = 550;
	private static final int MIN_HEIGHT = 550;
	private String text;
	private boolean licenseLoaded;
	private JEditorPane textPane;
	
	public AboutDialog(Frame owner) {
		super(owner, true);
		licenseLoaded = false;
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		// text for Waya and dependencies		
		text = "<html>\n"+
				// waya
				"<h1>Waya "+WAYA_VERSION+"</h1>\n"+
				"Copyright "+WAYA_YEARS+" Bertrand Gazanion<br>\n"+
				// libs
				"<h1>"+MESSAGES.getString("label_about_libraries")+"</h1>"+
				"This software uses the following open-source libraries:\n"+
				// genson
				"<h2>Genson</h2>"+
				"<p>www.genson.io</p>\n"+
				"<p>Copyright 2011-2014 Genson - Cepoi Eugen</p>\n"+
				"<p>"+MESSAGES.getString("label_about_licensed_apache2")+"</p>\n"+
				// flatlaf
				"<h2>FlatLaf</h2>"+
				"<p>www.formdev.com/flatlaf</p>\n"+
				"<p>Copyright 2019-2021 FormDev Software GmbH</p>\n"+
				"<p>"+MESSAGES.getString("label_about_licensed_apache2")+"</p>\n";
		
		// text pane
		textPane = new JEditorPane();
		textPane.setContentType("text/html");
		textPane.setText(text);
		
		// button to display the Apache license
		JButton licenseButton = new JButton(MESSAGES.getString("button_show_license_apache"));
		licenseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (licenseLoaded == false) {
					text += "<h2>"+MESSAGES.getString("label_license_apache")+"</h2>";
					text += loadApacheLicense();
					licenseLoaded = true;
					textPane.setText(text);
					textPane.setCaretPosition(0);
					AboutDialog.this.revalidate();
				}
			}
		});
		
		
		JScrollPane scrollPane = new JScrollPane(textPane);
		mainPanel.add(scrollPane);
		mainPanel.add(licenseButton);
		mainPanel.setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		setContentPane(mainPanel);
		pack();
		setVisible(true);
//		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
//		setSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
//		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
	}
	
	
	private String loadApacheLicense() {
		InputStream licenseInputStream = MainFrame.class.getResourceAsStream("apache_license.txt");
		String licenseText = "See: http://www.apache.org/licenses/LICENSE-2.0";
		if (licenseInputStream == null) {
			LOGGER.severe("Cannot find the Apache license file");
		} else {
			try (InputStreamReader streamReader = new InputStreamReader(licenseInputStream);
				 BufferedReader bufferedReader = new BufferedReader(streamReader)) {
				String line;
				licenseText = "";
				while ((line = bufferedReader.readLine()) != null) {
					licenseText += line+"<br>";
				}
			} catch (IOException e) {
				LOGGER.severe("Error while reading the Apache license file");
				e.printStackTrace();
			}
		}
		return licenseText;
	}
}
