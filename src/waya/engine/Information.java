package waya.engine;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;

public class Information {
	@SuppressWarnings("unused")
	private static final long VERSION = 1L;
	
	private String name;
	private List<String> values;
	
	/**
	 * Constructor
	 * @param name
	 */
	public Information(String name) {
		this.name = name;
		this.values = new LinkedList<String>();
	}
	
	
	/**
	 * Copy constructor
	 * @param otherInfo information object copied
	 */
	public Information(Information otherInfo) {
		this.name = otherInfo.getName();
		this.values = new LinkedList<String>();
		for (int i=0; i<otherInfo.getNumberOfValues(); i++) {
			addValue(otherInfo.getValue(i));
		}
	}
	
	
	/**
	 * Set the information name
	 * @param name information name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * Return the information name
	 * @return name
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Return the number of values
	 * @return number of values
	 */
	public int getNumberOfValues() {
		return values.size();
	}
	
	
	/**
	 * Return the value at index
	 * @param index index accessed
	 * @return value the value at given index
	 * @throws IndexOutOfBoundsException
	 */
	public String getValue(int index) throws IndexOutOfBoundsException {
		try {
			return values.get(index);
		} catch (IndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException();
		}
	}
	
	
	public List<String> getValues() {
		return Collections.unmodifiableList(values);
	}
	
	
	/**
	 * Change the value at index
	 * @param index index set
	 * @param value value set at given index
	 * @throws IndexOutOfBoundsException
	 */
	public void changeValue(int index, String value) throws IndexOutOfBoundsException {
		try {
			values.set(index, value);
		} catch (IndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException();
		}
	}
	
	
	/**
	 * Add a new value at the end of the list of values
	 * @param value value appended
	 */
	public void addValue(String value) {
		values.add(value);
	}
	
	
	/**
	 * Insert a new value at given index and shift subsequent values
	 * @param index insertion index
	 * @param value value inserted
	 */
	public void insertValue(int index, String value) {
		values.add(index, value);
	}
	
	
	/**
	 * Swap the values at two indices
	 * @param index1 index of the first value
	 * @param index2 index of the seconds value
	 * @throws IndexOutOfBoundsException
	 */
	public void swapValues(int index1, int index2) throws IndexOutOfBoundsException {
		// Handle index errors
		if (0 > index1 || index1 >= values.size()) {
			throw new IndexOutOfBoundsException("index1 out of bounds");
		} else if (0 > index2 || index2 >= values.size()) {
			throw new IndexOutOfBoundsException("index2 out of bounds");
		}
		
		String value1 = values.get(index1);
		String value2 = values.get(index2);
		changeValue(index1, value2);
		changeValue(index2, value1);
	}
	
	
	/**
	 * Remove value at index
	 * @param index index the value removed
	 * @throws IndexOutOfBoundsException
	 */
	public void removeValue(int index) throws IndexOutOfBoundsException {
		try {
			values.remove(index);
		} catch (IndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException();
		}
	}
	
	
	/**
	 * Return the information formatted in a string
	 * @return formatted string
	 */
	public String toJson() {
		GensonBuilder builder = new GensonBuilder();
		builder.include("VERSION");
		builder.include("values");
		builder.exclude("numberOfValues");
		Genson genson = builder.create();
		return genson.serialize(this);
	}
	
	@Override
	public String toString() {
		GensonBuilder builder = new GensonBuilder();
		builder.include("VERSION");
		builder.include("values");
		builder.exclude("numberOfValues");
		Genson genson = builder.create();
		return genson.serialize(this);
	}
}
