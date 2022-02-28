package waya.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


public class ImageViewer extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(ImageViewer.class.getPackage().getName());
	private Image image;
	private int width;
	private int height;
	private File imageFile;
	private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("waya.gui.resources.messages");
	private static final String NO_PHOTO_STRING = MESSAGES.getString("label_no_photo");
	private static JLabel noPhotoLabel;
	
	static {
		noPhotoLabel = new JLabel(NO_PHOTO_STRING, SwingConstants.CENTER);
	}
	
	public ImageViewer(File inputImageFile, int inputWidth, int inputHeight) {
		super();
		imageFile = inputImageFile;
		width = inputWidth;
		height = inputHeight;
		setPreferredSize(new Dimension(width, height));
		setLayout(new BorderLayout());
		setBorder(new JButton().getBorder());
		updateImage();
	}
	
	
	public int getWidth() {
		return width;
	}
	
	
	public int getHeight() {
		return height;
	}
	
	
	public void updateImage() {
		LOGGER.finest("Draw "+imageFile);
		try {
			image = ImageIO.read(imageFile);
		} catch (Exception e) {
			image = null;
		}
		
		removeAll();
		if (image == null) {
			LOGGER.finest(" -> image = null");
			add(noPhotoLabel, BorderLayout.CENTER);
		} else
			LOGGER.finest(" -> image OK");	
		repaint();
	}
	
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (image != null) {
			g.drawImage(image, 0, 0, this);
		}
	}
}
