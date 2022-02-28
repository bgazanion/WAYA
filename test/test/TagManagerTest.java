package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import waya.engine.TagManager;

class TagManagerTest {

	@Test
	void testAll() {
		// setup test directory
		String rootDir = System.getProperty("user.dir");
		File exportDir = FileSystems.getDefault().getPath(rootDir, "data", "test").toFile();		
		if (!exportDir.isDirectory()) {
			try {
				Files.createDirectories(exportDir.toPath());
			} catch (IOException e) {
				fail("cannot create directory: "+exportDir);
			}
		} else {
			// clean path from files created previously
			for (File tmpFile : exportDir.listFiles()) {
				try {
					Files.deleteIfExists(tmpFile.toPath());
				} catch (IOException e) {
					fail("cannot delete file: "+tmpFile.toString());
				}
			}
		}
		
		// constructor
		/*
		final File wrongDir = FileSystems.getDefault().getPath(rootDir, "no", "tags").toFile();
		assertThrows(IOException.class, () -> new TagManager(wrongDir));
		*/
		// TODO: create a test case where it is not possible to create the data dir
		
		TagManager manager = null;
		try {
			manager = new TagManager(exportDir);
		} catch (IOException e) {
			fail("Cannot create tag manager");
		}
		
		// add tag
		manager.addTag("work");
		manager.addTag("friends");
		manager.addTag("family");
		final TagManager tmpFinalManager = manager;
		assertThrows(IllegalArgumentException.class, () -> { tmpFinalManager.addTag("work"); });
		assertTrue(manager.hasTag("work"));
		assertTrue(manager.hasTag("friends"));
		assertTrue(manager.hasTag("family"));
		assertTrue(manager.getTags().contains("work"));
		assertTrue(manager.getTags().contains("friends"));
		assertTrue(manager.getTags().contains("family"));
		assertEquals(manager.getTags().size(), 3);
		
		// add to tag
		manager.addToTag("work", "bart");
		manager.addToTag("work", "bob");
		manager.addToTag("work", "bill");
		manager.addToTag("work", "bill");
		manager.addToTag("work", "joe");
		manager.addToTag("work", "joe");
		assertEquals(manager.getContent("work").size(), 6);
		assertTrue(manager.getContent("work").contains("bart"));
		assertTrue(manager.getContent("work").contains("bob"));
		assertTrue(manager.getContent("work").contains("bill"));
		assertTrue(manager.getContent("work").contains("joe"));
		final TagManager tmpFinalManager2 = manager;
		assertThrows(IllegalArgumentException.class, 
				() -> { tmpFinalManager2.addToTag("ex work", "brian"); });
		
		// remove from tag
		manager.removeFromTag("work", "joe");
		assertEquals(manager.getContent("work").size(), 4);
		assertTrue(manager.getContent("work").contains("bart"));
		assertTrue(manager.getContent("work").contains("bob"));
		assertTrue(manager.getContent("work").contains("bill"));
		assertFalse(manager.getContent("work").contains("joe"));
		final TagManager tmpFinalManager3 = manager;
		assertThrows(IllegalArgumentException.class, 
				() -> { tmpFinalManager3.removeFromTag("work", "joe"); });
		assertThrows(IllegalArgumentException.class, 
				() -> { tmpFinalManager3.removeFromTag("friends", "joe"); });
		assertThrows(IllegalArgumentException.class, 
				() -> { tmpFinalManager3.removeFromTag("cats", "joe"); });
		
		// remove tag
		manager.removeTag("friends");
		assertFalse(manager.hasTag("friends"));
		assertTrue(manager.hasTag("work"));
		assertTrue(manager.hasTag("family"));
		assertFalse(manager.getTags().contains("friends"));
		assertTrue(manager.getTags().contains("work"));
		assertTrue(manager.getTags().contains("family"));
		assertEquals(manager.getTags().size(), 2);		
		final TagManager tmpFinalManager4 = manager;
		assertThrows(IllegalArgumentException.class, () -> { tmpFinalManager4.removeTag("friends"); });
		
		// rename tag
		manager.renameTag("work", "pub");
		assertFalse(manager.hasTag("work"));
		assertTrue(manager.hasTag("pub"));
		assertTrue(manager.hasTag("family"));
		assertFalse(manager.getTags().contains("work"));
		assertTrue(manager.getTags().contains("pub"));
		assertTrue(manager.getTags().contains("family"));
		assertEquals(manager.getTags().size(), 2);
		final TagManager tmpFinalManager5 = manager;
		assertThrows(IllegalArgumentException.class, 
				() -> { tmpFinalManager5.renameTag("friends", "colleagues"); });
		assertThrows(IllegalArgumentException.class, 
				() -> { tmpFinalManager5.renameTag("pub", "family"); });
		assertEquals(manager.getContent("pub").size(), 4);
		assertTrue(manager.getContent("pub").contains("bart"));
		assertTrue(manager.getContent("pub").contains("bob"));
		assertTrue(manager.getContent("pub").contains("bill"));
		
		// remove from all tags
		manager.addToTag("family", "jeff");
		manager.removeFromAllTags("jeff");
		assertFalse(manager.getContent("pub").contains("jeff"));
		assertFalse(manager.getContent("family").contains("jeff"));
		
		//
		// save tag
		//
		
		try {
			manager.save();
		} catch (IOException e) {
			fail("export to file: "+e.getMessage());
		}
		
		// export to existing file - overwriting 
		try {
			manager.save();
			manager.save();
			manager.save();
		} catch (IOException e) {
			fail("export to file - overwriting: "+e.getMessage());
		}
		
		// load tag
		TagManager loadedManager = null;
		try {
			loadedManager = TagManager.load(exportDir);
		} catch (Exception e) {
			e.printStackTrace();
			fail("load from file");
		}
		assertEquals(manager.getTags(), loadedManager.getTags());
		for (String tagName : loadedManager.getTags()) {
			assertEquals(manager.getContent(tagName), loadedManager.getContent(tagName));
		}
	}
}
