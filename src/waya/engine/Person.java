package waya.engine;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;

public class Person {
	@SuppressWarnings("unused")
	private static final long VERSION = 1L;
	
	private String id;
	private String name;
	private LinkedList<Information> informations;
	private String imagePath;

	/**
	 * Constructor
	 * @param firstName
	 * @param lastName
	 * @param known
	 */
	public Person(String id, String name) {
		this.id = id;
		this.name = name;
		this.informations = new LinkedList<Information>();
		this.imagePath = "";
	}
	
	
	/**
	 * Constructor
	 */
	public Person(String id) {
		this(id, "");
	}
	

	/**
	 * Copy constructor
	 * @param otherPerson person copied
	 */
	public Person(Person otherPerson) {
		this(otherPerson.getId(), otherPerson.getName());
		setImagePath(otherPerson.getImagePath());
		for (int i=0; i<otherPerson.getNumberOfInformations(); i++) {
			Information infoCopy = new Information(otherPerson.getInformation(i));
			informations.add(infoCopy);
		}
	}
	
	
	/**
	 * Builder for empty person
	 */
	public static Person buildEmpty() {
		return new Person("");
	}
	
	
	/**
	 * Test if a person object if empty
	 * @return true if empty
	 */
	public boolean isEmpty() {
		return (id.isEmpty());
	}
	
	
	/**
	 * Return the id
	 * @return id
	 */
	public String getId() {
		return id;
	}
	
	
	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * Get the number of informations
	 * @return number of informations
	 */
	public int getNumberOfInformations() {
		return informations.size();
	}
	
	
	/**
	 * Set the path of the image file
	 * @param imagePath path of the image file
	 */
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	
	
	/**
	 * Get the path of the image file
	 * @return path of the image file
	 */
	public String getImagePath() {
		return imagePath;
	}
	
	
	/**
	 * Add an information
	 * @param information
	 */
	public void addInformation(Information information) {
		informations.add(information);
	}
	
	
	/**
	 * Replace information at given index
	 */
	public void changeInformation(int index, Information information) 
	throws IndexOutOfBoundsException {
		informations.set(index, information);
	}

	
	/**
	 * Remove the information at given index
	 * @param index of the information
	 * @return information removed
	 */
	public Information removeInformation(int index) {
		return informations.remove(index);
	}
	

	/**
	 * Get the information at given index
	 * @param index of the information
	 * @return information
	 */
	public Information getInformation(int index) {
		return informations.get(index);
	}
	
	
	public void swapInformation(int index1, int index2)
	throws IndexOutOfBoundsException {
		// Handle index errors
		if (index1 < 0 || index1 >= informations.size()) {
			throw new IndexOutOfBoundsException("index1 out of bounds");
		} else if (index2 < 0 || index2 >= informations.size()) {
			throw new IndexOutOfBoundsException("index2 out of bounds");
		}
		
		Information info1 = informations.get(index1);
		Information info2 = informations.get(index2);
		informations.set(index1, info2);
		informations.set(index2, info1);
	}
	
	
	/**
	 * Serialize the object as a JSON
	 * @return JSON string
	 */
	public String toJson() {		
		GensonBuilder builder = new GensonBuilder();
		builder.include("id");
		builder.include("name");
		builder.include("imagePath");
		builder.include("informations");
		builder.include("VERSION");
		builder.exclude("numberOfInformations");
		// prevent creation of field "numberOfValues" for Information objects
		builder.exclude("numberOfValues");	
		builder.acceptSingleValueAsList(true);
		Genson genson = builder.create();	
		return genson.serialize(this);
		
	}
	
	
	public static Person load(String fileName) throws IOException {
		
		Genson genson = new Genson();
		Map<String, Object> contentMap;
		try ( FileReader reader = new FileReader(fileName); ) { 
			contentMap = genson.deserialize(reader, new GenericType<Map<String, Object>>(){});
		} catch (IOException e) {
			throw new IOException(e);
		}
		
		// parse fields
		String mapId = contentMap.get("id").toString();
		String mapName = contentMap.get("name").toString();
		String mapImagePath = contentMap.get("imagePath").toString();
		// - informations
		List<Map<String, Object>> listRawInfo = 
				(List<Map<String, Object>>) contentMap.get("informations");
		LinkedList<Information> listInformations = new LinkedList<Information>();
		for (Map<String, Object> rawInfo : listRawInfo) {
			String name = rawInfo.get("name").toString();
			List<String> listValues = (ArrayList<String>) rawInfo.get("values");
			Information info = new Information(name);
			for (String value : listValues) {
				info.addValue(value);
			}
			listInformations.add(info);
		}
		
		// build result
		Person result = new Person(mapId, mapName);
		for (Information information : listInformations)
			result.addInformation(information);
		result.setImagePath(mapImagePath);
		return result;
	}
}



