package utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;




public class DatasetBuilder {

	private static Random rng = new Random(System.nanoTime());
	private static int rngCallCounter = 0;
	private static final int RNG_MAX_CALL_COUNTER = 50;
	private static final int PARENT_CHILD_MIN_AGE_OFFSET = 17;
	private static final int PARENT_CHILD_MAX_AGE_OFFSET = 45;
	
	public enum AGE_CATEGORY {CHILD, YOUTH, ADULT, SENIOR};
	
	
	private synchronized static void updateRngSeed() {		
		rngCallCounter += 1;
		if (rngCallCounter > RNG_MAX_CALL_COUNTER) {
			rng = new Random(System.nanoTime());
			rngCallCounter = 0;
		}
	}
	
	/**
	 * Return a randomly selected element from the input array
	 * @param array input array
	 */
	private static String pickRandomFrom(String[] array) {
		String result;
		if (array.length == 0) {
			// input error: this should not happen
			result = "";
		} else {
			int index = rng.nextInt(array.length);
			result = array[index];
		}
		updateRngSeed();
		return result;
	}
	
	/**
	 * Return a randomly number between two values
	 */
	private static int pickRandomBetween(int min, int max) {
		int result = rng.nextInt(max - min + 1) + min;
		updateRngSeed();
		return result;
	}
	
	
	/**
	 * Export the information to a JSON
	 * @param name
	 * @param fields
	 */
	private String toJson(String name, List<String> fields) {
		String output = "\""+name+"\":";
		if (fields.size() == 0) {
			output += "\"\"";	
		} else if (fields.size() == 1) {
			output += '"'+fields.get(0)+'"';
		} else {
			output = output + "[" + String.join(", ", fields) + "]";
		}
		return output;
	}
	
	
	public static String createManFirstName() {
		// most common names based on US census
		String[] values = {
			"James",
			"Robert",
			"John",
			"Michael",
			"William",
			"David",
			"Richard",
			"Joseph",
			"Thomas",
			"Charles",
			"Christopher",
			"Daniel",
			"Matthew",
			"Anthony",
			"Mark",
			"Donald",
			"Steven",
			"Paul",
			"Andrew",
			"Joshua",
			"Kenneth",
			"Kevin",
			"Brian",
			"George",
			"Edward",
			"Ronald",
			"Timothy",
			"Jason",
			"Jeffrey",
			"Ryan"
		};
		return pickRandomFrom(values);
	}
	
	
	public static String createWomanFirstName() {
		// most common names based on US census
		String[] values = {
			"Mary",
			"Patricia",
			"Jennifer",
			"Linda",
			"Elizabeth",
			"Barbara",
			"Susan",
			"Jessica",
			"Sarah",
			"Karen",
			"Nancy",
			"Lisa",
			"Betty",
			"Margaret",
			"Sandra",
			"Ashley",
			"Kimberly",
			"Emily",
			"Donna",
			"Michelle",
			"Dorothy",
			"Carol",
			"Amanda",
			"Melissa",
			"Deborah",
			"Stephanie",
			"Rebecca",
			"Sharon",
			"Laura",
			"Cynthia"
		};
		return pickRandomFrom(values);
	}
	
	
	public static String createFamilyName() {
		// most common names based on US census
		String[] values = {
			"Smith",
			"Johnson",
			"Williams",
			"Brown",
			"Jones",
			"Garcia",
			"Miller",
			"Davis",
			"Rodriguez",
			"Martinez",
			"Hernandez",
			"Lopez",
			"Gonzalez",
			"Wilson",
			"Anderson",
			"Thomas",
			"Taylor",
			"Moore",
			"Jackson",
			"Martin",
			"Lee",
			"Perez",
			"Thompson",
			"White",
			"Harris",
			"Sanchez",
			"Clarck",
			"Ramirez",
			"Lewis",
			"Robinson",
			"Walker",
			"Young",
			"Allen",
			"King",
			"Wright",
			"Scott",
			"Torres",
			"Nguyen",
			"Hill",
			"Flores",
			"Green",
			"Adams",
			"Nelson",
			"Baker",
			"Hall",
			"Rivera",
			"Campbell",
			"Mitchell",
			"Carter",
			"Roberts"
		};
		return pickRandomFrom(values);
	}
	
	
	public static String createPetName() {
		// take the name from one of the following categories
		String[] categories = {"man", "woman", "family"};		
		String selectedCategory = pickRandomFrom(categories);
		if (selectedCategory == "man") {
			return createManFirstName();
		} else if (selectedCategory == "woman") {
			return createWomanFirstName();
		} else if (selectedCategory == "family"){
			return createFamilyName();
		} else {
			return "";
		}
	}
	
	
	/**
	 * Create an age from an age category
	 * @param category age category from AGE_CATEGORY
	 * @return age
	 */
	public static int createAge(AGE_CATEGORY category) {
		int result;
		if (category == AGE_CATEGORY.CHILD) {
			result = pickRandomBetween(1, 14);
		} else if (category == AGE_CATEGORY.YOUTH) {
			result = pickRandomBetween(15, 24);
		} else if (category == AGE_CATEGORY.ADULT) {
			result = pickRandomBetween(25, 64);
		} else if (category == AGE_CATEGORY.SENIOR) {
			result = pickRandomBetween(65, 100);
		} else {
			result = 200;
		}
		return result;
	}
	
	
	/**
	 * Create the age of the person's parent
	 * @param ageOfPerson age of the person - must come from function createAge
	 * @return age of the parent
	 */
	private static int createAgeOfParent(int ageOfPerson) {
		int min_age = ageOfPerson + PARENT_CHILD_MIN_AGE_OFFSET;
		int max_age = ageOfPerson + PARENT_CHILD_MAX_AGE_OFFSET;
		return pickRandomBetween(min_age, max_age);
	}
	
	
	/**
	 * Create the age of the person's child
	 * @param ageOfPerson age of the person - must come from function createAge
	 * @return age of the child
	 */
	private static int createAgeOfChild(int ageOfPerson) {
		int min_age = ageOfPerson - PARENT_CHILD_MAX_AGE_OFFSET;
		if (min_age <= 0) {
			min_age = 1;
		}
		int max_age = ageOfPerson - PARENT_CHILD_MIN_AGE_OFFSET;
		if (max_age <= 0) {
			max_age = 1;
		}
		return pickRandomBetween(min_age, max_age);
	}
	
	
	public static void main(String[] argv) {
		System.out.println("Man first name: "+DatasetBuilder.createManFirstName());
		System.out.println("Woman first name: "+DatasetBuilder.createWomanFirstName());
		System.out.println("Family name: "+DatasetBuilder.createFamilyName());
		System.out.println("Pet name: "+DatasetBuilder.createPetName());
		System.out.println("Child age: "+DatasetBuilder.createAge(AGE_CATEGORY.CHILD));
		System.out.println("Youth age: "+DatasetBuilder.createAge(AGE_CATEGORY.YOUTH));
		System.out.println("Adult age: "+DatasetBuilder.createAge(AGE_CATEGORY.ADULT));
		System.out.println("Senior age: "+DatasetBuilder.createAge(AGE_CATEGORY.SENIOR));
		int age = DatasetBuilder.createAge(AGE_CATEGORY.ADULT);
		System.out.println("Age of child of person of "+age+": "+DatasetBuilder.createAgeOfChild(age));
		System.out.println("Age of parent of person of "+age+": "+DatasetBuilder.createAgeOfParent(age));
	}
}
