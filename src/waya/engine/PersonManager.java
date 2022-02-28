package waya.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;

public class PersonManager {
	@SuppressWarnings("unused")
	private static final long VERSION = 1L;
	
	private static final String DATA_END = "_data";
	private static final String SAVE_EXTENSION = ".json";
	
	HashMap<String, Person> persons;
	private int idLength;
	private File dataDirectory;
	
	/**
	 * Constructor
	 * @param idLength number of characters of the Id string
	 * @param directory directory used to store the data (existing or created).
	 * @throws IOException 
	 */
	public PersonManager(int idLength, File directory) throws IOException {
		// create the directory if it does not exist
		if (directory.isDirectory() == false) {
			Files.createDirectories(directory.toPath());
		}
		this.persons = new HashMap<>();
		this.idLength = idLength;
		this.dataDirectory = directory;
	}
	
	
	/**
	 * Generate an Id string
	 * @param random Random object
	 * @return Id string
	 */
	private String generateId(Random random) {
		char[] id = new char[idLength];
		char[] symbols = "0123456789ABCDEF".toCharArray();
		int index;
		for (int i=0; i<idLength; ++i) {
			index = random.nextInt(symbols.length);
			id[i] = symbols[index];
		}
		return new String(id);
	}
	
	
	/**
	 * Generate a unique Id string regarding a set of existing Ids
	 * @return unique Id string
	 */
	public String createPersonId() {
		Random random = new Random(System.nanoTime());
		Set<String> IdSet = persons.keySet();
		String id;
		do
			id = generateId(random); 
		while (IdSet.contains(id));
		return id;
	}
	
	
	/**
	 * Add a person
	 * @param personId Id of the person
	 * @param person Person object
	 */
	public void addPerson(Person person) throws IllegalArgumentException {
		String personId = person.getId();
		
		// check id length
		if (personId.length()!=idLength) {
			throw new IllegalArgumentException("Person object with wrong id length");
		}
		// check that this Id is not already taken
		if (persons.containsKey(personId)) {
			throw new IllegalArgumentException("The input person's Id is already taken");
		}
		
		persons.put(personId, person);
	}
	
	
	/**
	 * Set the new person for given Id
	 * @param personId Id of the person modified
	 * @param person new value of the person
	 */
	public void setPerson(String personId, Person person) {
		// check that personId matches the person's id
		if (personId != person.getId()) {
			throw new IllegalArgumentException("Person Id and input Id mismatch");
		}
		// check that the person exists in the manager
		if (persons.containsKey(personId) == false) {
			throw new IllegalArgumentException("No person with such Id");
		}
		
		persons.put(personId, person);
	}
	
	
	/**
	 * Return a set of Ids of the persons
	 * @return set of Ids
	 */
	public Set<String> getPersonIds() {
		return persons.keySet();
	}
	
	
	/**
	 * Remove a person
	 * @param personId Id of the person removed
	 * @return true if the operation succeeded
	 */
	public boolean removePerson(String personId) throws IllegalArgumentException {
		if (!persons.containsKey(personId)) {
			throw new IllegalArgumentException("No person with such Id");
		} else {
			persons.remove(personId);
			return true;
		}
	}
	
	
	/**
	 * Return a copy of the person object with requested Id
	 * @param personId Id of the person
	 * @return Person object
	 */
	public Person getPerson(String personId) throws IllegalArgumentException  {
		if (!persons.containsKey(personId)) {
			throw new IllegalArgumentException("No person with such Id");
		} else {
			Person copy = new Person(persons.get(personId));
			return copy;
		} 
	}
	
	
	/**
	 * Tells if the manager contains a person with the given Id
	 * @param personId input person Id
	 * @return true if the manager contains a person with the given Id
	 */
	public boolean containsPersonId(String personId) {
		return persons.containsKey(personId);
	}
	
	
	/**
	 * Serialize the object as a JSON
	 * @return JSON string
	 */
	public String toJson() {
		GensonBuilder builder = new GensonBuilder()
			.include("VERSION")
			.include("idLength")
			.exclude("persons")
			.acceptSingleValueAsList(true)
			.useMethods(true);
		Genson genson = builder.create();
		return genson.serialize(this);
	}
	
	
	/**
	 * Save a single person to a file
	 * @param personId person ID
	 * @throws IOException
	 */
	public void saveSinglePerson(String personId) throws IOException {
		String fileName;
		String pathSeparator = FileSystems.getDefault().getSeparator();
		fileName = dataDirectory + pathSeparator + personId+DATA_END+SAVE_EXTENSION;
		FileTools.overwriteTextFile(fileName, persons.get(personId).toJson());
	}
	
	/**
	 * Remove the save file of a single person
	 * @param directory save directory
	 * @param personId person ID
	 * @throws IOException
	 */
	public void removePersonFile(File directory, String personId) throws IOException {
		// check that directory exists
		if (!directory.isDirectory()) {
			throw new IOException("Directory does not exist");
		}
		String fileName;
		String pathSeparator = FileSystems.getDefault().getSeparator();
		fileName = directory + pathSeparator + personId+DATA_END+SAVE_EXTENSION;
		File file = new File(fileName);
		file.delete();
	}
	
	
	/**
	 * Save the manager's content to a file
	 * @param directory save directory
	 * @throws IOException 
	 */
	public void save() throws IOException {
		// write a file for each Person object in persons
		String fileName;
		String pathSeparator = FileSystems.getDefault().getSeparator();
		for (String personId : persons.keySet()) {
			fileName = dataDirectory + pathSeparator + personId+DATA_END+SAVE_EXTENSION;
			FileTools.overwriteTextFile(fileName, persons.get(personId).toJson());
		}
	}
	
	
	/**
	 * Load a person manager from a directory
	 * @param directoryPath
	 * @return person manager object
	 * @throws IOException
	 */
	public static PersonManager load(File directory, int idLength) throws IOException {
		// check that path exists
		if (!directory.isDirectory()) {
			System.out.println("dir not exist");
			throw new IOException("Directory does not exist");
		}
		
		// list person files
		String pathSeparator = FileSystems.getDefault().getSeparator();
		List<String> personFiles = new LinkedList<String>();
		for (String file : directory.list()) {
			if (file.endsWith(DATA_END+SAVE_EXTENSION)) {
				// TODO: check ID LENGTH using regex
				personFiles.add(directory + pathSeparator + file);
			}
		}
		
		// create PersonManager
		PersonManager pm = new PersonManager(idLength, directory);
		for (String fileName : personFiles) {
			Person personLoaded = Person.load(fileName);
			String personFile = (new File(fileName).getName());
			if (personLoaded.isEmpty()) {
				// skip empty person
				System.out.println("Skip file "+personFile+": empty person");
				continue;
			}
			
			// check ID length
			if (personLoaded.getId().length() != idLength) {
				System.out.println("Skip file "+personFile+": ID with wrong length");
				continue;
			}
			
			// add valid person
			pm.addPerson(personLoaded);	
		}
		
		return pm;
	}
}
