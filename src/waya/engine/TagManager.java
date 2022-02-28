package waya.engine;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;


/**
 * A class to manage tags passed to strings objects.
 * A string objects can have multiple tags.
 * @author bertrand
 *
 */
public class TagManager {
	private static String ERROR_NO_TAG = "There is no tag: ";
	private static String ERROR_TAG_EXISTS = "Tag already exists: ";
	private static final String SAVE_FILE = "tags.json";
	private static final Genson GENSON_CONVERTER;
	
	private HashMap<String, ArrayList<String>> tags;
	private File dataDirectory;
	
	static {
		GensonBuilder builder = new GensonBuilder()
			.include("tags")
			.acceptSingleValueAsList(true)
			.useMethods(false);
		GENSON_CONVERTER = builder.create();
	}
	
	/**
	 * Constructor
	 * @param directory directory used to store the data (existing or created).
	 * @throws IOException 
	 */
	public TagManager(File directory) throws IOException {
		// create data directory if it does not exist
		if (directory.isDirectory() == false) {
			Files.createDirectories(directory.toPath());
		}
		tags = new HashMap<>();
		this.dataDirectory = directory;
	}
	
	
	public Set<String> getTags() {
		return tags.keySet();
	}
	
	
	public List<String> getContent(String tagName) throws IllegalArgumentException {
		if (hasTag(tagName)) {
			// return an unmodifiable view of the content of the tag
			List<String> output = Collections.unmodifiableList(tags.get(tagName));
			return output;
		} else {
			throw new IllegalArgumentException(ERROR_NO_TAG+tagName);
		}
	}
	
	
	public void addTag(String tagName) throws IllegalArgumentException {
		if (!hasTag(tagName)) {
			tags.put(tagName, new ArrayList<String>());
		} else {
			throw new IllegalArgumentException(ERROR_TAG_EXISTS+tagName);
		}
	}
	
	
	public void removeTag(String tagName) throws IllegalArgumentException {
		if (hasTag(tagName)) {
			tags.remove(tagName);
		} else {
			throw new IllegalArgumentException(ERROR_NO_TAG+tagName);
		}
	}
	
	
	public void renameTag(String oldName, String newName) throws IllegalArgumentException {
		if (newName == oldName) {
			return;
		}
		
		// handle errors
		// - <oldName> tag does not exist
		if (!hasTag(oldName)) {
			throw new IllegalArgumentException(ERROR_NO_TAG+oldName);
		}
		// - <newName> tag already exists
		if (hasTag(newName)) {
			throw new IllegalArgumentException(ERROR_TAG_EXISTS+newName);
		}
		
		// create a new tag, transfer content and remove old tag
		ArrayList<String> content = tags.get(oldName);
		tags.put(newName, content);
		tags.remove(oldName);
	}
	
	
	public void addToTag(String tagName, String item) throws IllegalArgumentException {
		if (hasTag(tagName)) {
			tags.get(tagName).add(item);
		} else {
			throw new IllegalArgumentException(ERROR_NO_TAG+tagName);
		}
	}

	
	public void removeFromTag(String tagName, String item) throws IllegalArgumentException {
		if (hasTag(tagName)) {
			ArrayList<String> tagContent = tags.get(tagName);
			try {
				boolean wasRemoved = tagContent.removeIf(item::equals);
				// warn if removal failed because there were no occurrences of item in the tag
				if (!wasRemoved) {
					throw new IllegalArgumentException();
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Cannot remove item "+item
						+" from tag "+tagName);
			}
			tags.get(tagName).remove(item);
			
		} else {
			throw new IllegalArgumentException(ERROR_NO_TAG+tagName);
		}
	}
	
	
	public void removeFromAllTags(String item) {
		for (String tag : tags.keySet()) {
			tags.get(tag).remove(item);
		}
	}
	
	
	public boolean hasTag(String tagName) {
		return tags.containsKey(tagName);
	}
	
	
	public void save() throws IOException {
		
		// create and write data as a JSON string
		String jsonString = GENSON_CONVERTER.serialize(this);
		String fileName = dataDirectory + File.separator + SAVE_FILE;
		FileTools.overwriteTextFile(fileName, jsonString);
	}
	
	
	public static TagManager load(File directory) throws IOException {		
		// parse file
		Map<String, Object> parseMap;
		Genson genson = new Genson();
		String fileName = directory + File.separator + SAVE_FILE;
		try ( FileReader reader = new FileReader(fileName); ) { 
			parseMap = genson.deserialize(reader, new GenericType<Map<String, Object>>(){});
		} catch (IOException e) {
			throw new IOException(e);
		}
		Map<String, List<String>> contentMap = (Map<String, List<String>>) parseMap.get("tags");
		
		// fill tag manager
		TagManager tm = new TagManager(directory);
		for (String tagName : contentMap.keySet()) {
			// create tag
			tm.addTag(tagName);
			// add person Ids
			for (String personId : contentMap.get(tagName)) {
				tm.addToTag(tagName, personId);
			}
		}
		
		return tm;
	}
}
