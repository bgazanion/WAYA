package test;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import waya.engine.FileTools;

class FileToolsTest {

	private static String ROOT = System.getProperty("user.dir");
	private static FileSystem FS = FileSystems.getDefault();
	private static String FS_SEP = FS.getSeparator();
	private static File EXPORT_DIR = FS.getPath(ROOT, "test", "export").toFile();
	private static File DATA_DIR = FS.getPath(ROOT, "test", "data").toFile();
	
	@Test
	void testOverwriteTextFile() {
		File exportDir = EXPORT_DIR;
		String oldContent = "Content of old file";
		String newContent = "Content of new file";
		String fileName = exportDir + FS_SEP + "testOverwriteTextFile.txt";
		
		// create test file
		try (FileWriter writer = new FileWriter(fileName); ) {
			writer.write(oldContent);
		} catch (Exception e) {
			fail("cannot create test file");
		}
		
		try {
			FileTools.overwriteTextFile(fileName, newContent);
		} catch (Exception e) {
			fail("cannot overwite");
		}
		
		try (BufferedReader reader = new BufferedReader(new FileReader(fileName)); ) {
			String result = reader.readLine();
			assertEquals(result, newContent);
		} catch (Exception e) {
			fail("cannot read file");
		}
		
		// clean
		Path filePath = (new File(fileName)).toPath();  
		try {
			Files.delete(filePath);
		} catch (IOException e) {
			fail("cannot remove file");
		}
	}
	
	@Test
	void testImportAndResizeImage() {
		int width = 200;
		int height = 150;
		
		String[] imageNames = {"blue.jpg", "green.jpg", "orange.png", "red.png"};
		
		for (String name : imageNames) {
			
			// import
			File source = new File(DATA_DIR + FS_SEP + name);
			String outName = name.substring(0, name.lastIndexOf('.'))+".png";
			File destination = new File(EXPORT_DIR + FS_SEP + outName);
			FileTools.importAndResizeImage(source, width, height, destination);
			
			// check imported image
			assertTrue(destination.isFile());
			BufferedImage image = null;
			try {
				image = ImageIO.read(destination);
			} catch (Exception e) {
				// error: the image was not read
				fail("Cannot open image "+destination.toString());
			}
			assertTrue(image.getWidth() == width || image.getHeight() == height);
			assertTrue(image.getWidth() <= width);
			assertTrue(image.getHeight() <= height);
			
			// clean: delete export image
			Path filePath = destination.toPath();  
			try {
				Files.delete(filePath);
			} catch (IOException e) {
				fail("cannot remove file "+destination.toString());
			}
		}
	}
	
	
	@Test
	void testResizeImage() {
		int width = 200;
		int height = 150;
		
		String[] imageNames = {"blue.jpg", "green.jpg", "orange.png", "red.png"};
		
		for (String name : imageNames) {
			
			// load the test image
			File source = new File(DATA_DIR + FS_SEP + name);
			BufferedImage image = null;
			try {
				image = ImageIO.read(source);
			} catch (Exception e) {
				// error: the image was not read
				fail("Cannot open image "+source.toString());
			}
			
			// resize the image
			BufferedImage resizedImage = FileTools.resizeImage(image, width, height);
			assertTrue(resizedImage.getWidth() == width || resizedImage.getHeight() == height);
			assertTrue(resizedImage.getWidth() <= width);
			assertTrue(resizedImage.getHeight() <= height);
		}

	}

}
