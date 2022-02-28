package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import waya.engine.Information;
import waya.engine.Person;

class PersonTest {

	@Test
	void testConstructorGettersSetters() {
		Person person;
		person = new Person("aaaa");
		assertEquals("aaaa", person.getId());
		assertTrue(person.getName().isEmpty());
		
		person = new Person("bbbb", "paul");
		assertEquals("bbbb", person.getId());
		assertEquals("paul", person.getName());
		
		person = Person.buildEmpty();
		assertTrue(person.isEmpty());
		assertTrue(person.getId().isEmpty());
		assertTrue(person.getName().isEmpty());
	}
	
	
	@Test
	void testInformation() {
		Person person = new Person("idOfMe");
		Information information;
		
		// add information
		information = new Information("sport");
		information.addValue("golf");
		person.addInformation(information);
		
		information = new Information("website");
		information.addValue("waya.com");
		person.addInformation(information);
		assertEquals(2, person.getNumberOfInformations());
		assertEquals(person.getInformation(0).getName(), "sport");
		assertEquals(person.getInformation(0).getValue(0), "golf");
		assertEquals(person.getInformation(1).getName(), "website");	
		assertEquals(person.getInformation(1).getValue(0), "waya.com");	
		
		// replace information
		information = new Information("homepage");
		information.addValue("me.com");
		person.changeInformation(0, information);
		assertEquals(2, person.getNumberOfInformations());
		assertEquals(person.getInformation(0).getName(), "homepage");
		assertEquals(person.getInformation(0).getValue(0), "me.com");
		assertEquals(person.getInformation(1).getName(), "website");	
		assertEquals(person.getInformation(1).getValue(0), "waya.com");	
		
		final Information information2 = new Information("cat");
		information.addValue("Greebo");
		assertThrows(IndexOutOfBoundsException.class, 
					 () -> person.changeInformation(4, information2));
		

		// get information
		information = person.getInformation(0);
		assertEquals(information.getValue(0), "me.com");
		assertThrows(IndexOutOfBoundsException.class, 
					 () -> person.getInformation(4));
		
		// remove information
		person.removeInformation(0);
		assertEquals(1, person.getNumberOfInformations());
		assertEquals(person.getInformation(0).getName(), "website");	
		assertEquals(person.getInformation(0).getValue(0), "waya.com");	
		assertThrows(IndexOutOfBoundsException.class, 
					 () -> person.removeInformation(1));
		
		// swap information
		information = new Information("cat");
		information.addValue("Muta");
		person.addInformation(information);
		
		person.swapInformation(0, 1);
		assertEquals(person.getInformation(0).getName(), "cat");
		assertEquals(person.getInformation(0).getValue(0), "Muta");
		assertEquals(person.getInformation(1).getName(), "website");
		assertEquals(person.getInformation(1).getValue(0), "waya.com");
		
		assertThrows(IndexOutOfBoundsException.class, 
				 	 () -> person.swapInformation(1, 2));
		assertThrows(IndexOutOfBoundsException.class, 
			 	 () -> person.swapInformation(-1, 2));
		assertThrows(IndexOutOfBoundsException.class, 
			 	 () -> person.swapInformation(0, -1));
	}
	
	
	@Test
	void testCopyConstructor() {
		Person original = new Person("idMe");
		original.setName("A true original name");
		original.setImagePath("/home/user/Path");
		Information information = new Information("cat");
		information.addValue("Muta");
		information.addValue("Greebo");
		original.addInformation(information);
		
		Person copy = new Person(original);
		copy.setName("another name");
		copy.getInformation(0).addValue("Azrael");
		copy.setImagePath("/home/user/AnotherPath");
		
		// check that the original was not changed
		assertEquals(original.getId(), "idMe");
		assertEquals(original.getName(), "A true original name");
		assertEquals(original.getNumberOfInformations(), 1);
		assertEquals(original.getInformation(0).getNumberOfValues(), 2);
		assertEquals(original.getInformation(0).getValue(0), "Muta");
		assertEquals(original.getInformation(0).getValue(1), "Greebo");
		assertEquals(original.getImagePath(), "/home/user/Path");
		
	}
	
	
	@Test
	void testJson() {
		// create person
		Person person = new Person("ID_ME", "Vlad");
		person.setImagePath("/home/waya/vlad.png");
		
		Information infoSport = new Information("sport");
		infoSport.addValue("golf");
		infoSport.addValue("hockey");
		person.addInformation(infoSport);
		
		Information infoBook = new Information("books");
		infoBook.addValue("discworld");
		person.addInformation(infoBook);
		
		// export to string
		String json = person.toJson();
		assertFalse(json.isEmpty());
		assertTrue(json.contains("VERSION"));
		assertTrue(json.contains("id"));
		assertTrue(json.contains(person.getId()));
		assertTrue(json.contains("name"));
		assertTrue(json.contains("imagePath"));
		assertTrue(json.contains(person.getName()));
		assertTrue(json.contains("informations"));
		for (int n=0; n<person.getNumberOfInformations(); n++) {
			String informationJson = person.getInformation(n).toJson();
			assertTrue(json.contains(informationJson));
		}
		
		
		// load from file
		String rootDir = System.getProperty("user.dir");
		Path path = FileSystems.getDefault().getPath(rootDir, "data", "_testPerson.json");
		final String fileName = path.toString();
		// -> preliminary step: write file
		try ( FileWriter writer = new FileWriter(fileName); ) {
			writer.write(json);
		} catch (IOException e) {
			fail("Cannot write file");
		}
		try {
			Person personFromFile = Person.load(fileName);
			assertFalse(personFromFile.isEmpty());
			assertEquals(person.getName(), personFromFile.getName());
			assertEquals(person.getId(), personFromFile.getId());
			assertEquals(person.getNumberOfInformations(), 
						 personFromFile.getNumberOfInformations());
			assertEquals(person.getImagePath(), 
						 personFromFile.getImagePath());
		}
		catch(IOException e) {
			fail("Cannot read file");
		}
	}
}
