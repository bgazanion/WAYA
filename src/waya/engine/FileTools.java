package waya.engine;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

public class FileTools {
	
	private static ResourceBundle MESSAGES = ResourceBundle.getBundle("waya.gui.resources.messages");
	
	/**
	 * Write the string content to a file, and safely overwrite if the file exists
	 * @param fileName name (absolute path) of the target file
	 * @param content string content
	 * @throws IOException
	 */
	public static void overwriteTextFile(String fileName, String content) throws IOException {
		Path filePath = Paths.get(fileName);
		boolean overwritingExistingFile = (Files.exists(filePath));
		Path backupPath = Paths.get(fileName+".save");
		
		// create a backup file if the target file already exists
		if (overwritingExistingFile) {
			try {
				Files.move(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
		
		// write content to file
		try (FileWriter writer = new FileWriter(fileName); ) {
			writer.write(content);
		} catch (Exception e) {
			if (overwritingExistingFile) {
				// the existing file is restored
				Files.move(backupPath, filePath);
			}
			throw new IOException(e);
		}
		
		// remove the backup file, if needed
		if (overwritingExistingFile) {
			try {
				Files.deleteIfExists(backupPath);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}
	
	
	public static void importAndResizeImage(File source, int width, int height, File destination) 
	{
		// TODO: handle errors & throw stuffs
		
		// check that the extension of the destination is valid
		if (destination.getName().endsWith(".png") == false) {
			System.out.println(MESSAGES.getString("error_image_extension"));
			return;
		}
		
		// load the image from the file
		BufferedImage image = null;
		try {
			image = ImageIO.read(source);
		} catch (Exception e) {
			// error: the image was not read
			System.out.println(MESSAGES.getString("error_image_cannot_open"));
			return;
		}
		
		// resize the image to fit the viewer's dimensions
		image = resizeImage(image, width, height);
		
		// copy image to destination
		try {
			ImageIO.write(image, "png", destination);
		} catch (IOException e) {
			// error: cannot export the image
			System.out.println(MESSAGES.getString("error_image_cannot_save"));
			return;
		}
	}
	
	
	public static BufferedImage resizeImage(BufferedImage image, int width, int height) {
		
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		
		// find scaling ratio
		float widthRatio = ((float) width)/imageWidth;
		float heightRatio = ((float) height)/imageHeight;
		float ratio = widthRatio<heightRatio ? widthRatio : heightRatio;
		
		// build scaling operation
		AffineTransform transform = new AffineTransform();
		transform.scale(ratio, ratio);
		AffineTransformOp scaleTransformOp = 
				new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		
		// build scaled image
		int newWidth = (int) Math.floor(imageWidth*ratio);	// TODO: find simpler
		int newHeight = (int) Math.floor(imageHeight*ratio);	// TODO: same
		BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, image.getType());
		scaledImage = scaleTransformOp.filter(image, scaledImage);
		
		return scaledImage;
	}
}
