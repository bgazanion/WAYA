package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Set;

import org.junit.jupiter.api.Test;

import waya.engine.Information;
import waya.engine.Person;
import waya.engine.PersonManager;

class PersonManagerTest {

	@Test
	void test() {
		
		int idLength = 6;
		
		// create data directory
		String rootDir = System.getProperty("user.dir");
		File exportDir = FileSystems.getDefault().getPath(rootDir, "data", "test").toFile();
		// setup test directory
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
		final File wrongDir = FileSystems.getDefault().getPath("/").toFile();
		assertThrows(IOException.class, () -> new PersonManager(idLength, wrongDir));
		*/
		// TODO: create a test case where it is not possible to create the data dir
		
		PersonManager pm = null;
		try {
			pm = new PersonManager(idLength, exportDir);
		} catch (IOException e) {
			fail("Constructor: wrong data directory");
		}
		
		
		// create a personId
		String id = pm.createPersonId();
		assertEquals(id.length(), idLength);
		assertFalse(id.isEmpty());
		
		// add a person
		String idBill = pm.createPersonId();
		Person bill = new Person(idBill, "Bill");
		Information info = new Information("cat");
		info.addValue("Greebo");
		info.addValue("Muta");
		bill.addInformation(info);
		pm.addPerson(bill);
		assertTrue(pm.containsPersonId(idBill));
		
		String idBob = pm.createPersonId();
		Person bob = new Person(idBob, "Bob");
		info = new Information("sports");
		info.addValue("golf");
		info.addValue("badminton");
		bob.addInformation(info);
		pm.addPerson(bob);
		assertTrue(pm.containsPersonId(idBob));
		
		// assert that you cannot modify a person in the PersonManager like a thief
		Person thief = pm.getPerson(idBill);
		thief.setName("Not bill");
		info = new Information("cars");
		info.addValue("an ugly car");
		info.addValue("another ugly car");
		thief.addInformation(info);
		
		Person billUpdate = pm.getPerson(idBill);
		assertEquals(billUpdate.getName(), "Bill");
		assertEquals(billUpdate.getNumberOfInformations(), 1);
		assertEquals(billUpdate.getInformation(0).getNumberOfValues(), 2);
		assertEquals(billUpdate.getInformation(0).getValue(0), "Greebo");
		assertEquals(billUpdate.getInformation(0).getValue(1), "Muta");
		
		// get person
		Person cloneOfBill = pm.getPerson(idBill);
		assertEquals(cloneOfBill.getName(), bill.getName());
		assertEquals(cloneOfBill.getNumberOfInformations(), bill.getNumberOfInformations());
		
		// content of the structure
		Set<String> personIds = pm.getPersonIds();
		assertEquals(2, personIds.size());
		assertTrue(personIds.contains(idBob));
		assertTrue(personIds.contains(idBill));
		
		// modify an existing person
		bob.setName("Bob with another name");
		pm.setPerson(idBob, bob);
		final PersonManager finalPm = pm;
		assertEquals(pm.getPerson(idBob).getName(), "Bob with another name");
		assertThrows(IllegalArgumentException.class, () -> {finalPm.setPerson(idBill, bob);});
		assertThrows(IllegalArgumentException.class, () -> {finalPm.setPerson("**invalidId", bob);});
		
		// remove an person
		boolean success = pm.removePerson(idBob);
		final PersonManager finalPm2 = pm;
		assertTrue(success);
		assertThrows(IllegalArgumentException.class, () -> { finalPm2.getPerson(idBob); });
		assertFalse(pm.containsPersonId(idBob));
		personIds = pm.getPersonIds();
		assertEquals(1, personIds.size());
		assertFalse(personIds.contains(idBob));
		assertTrue(personIds.contains(idBill));
		
		// add a person
		try {
			pm.addPerson(bob);
		} catch (IllegalArgumentException e) {
			fail(e);
		}
		assertTrue(pm.getPersonIds().contains(idBob));
		assertEquals(bob.getName(), pm.getPerson(idBob).getName());
		assertEquals(bob.getId(), pm.getPerson(idBob).getId());
		assertEquals(bob.getNumberOfInformations(), pm.getPerson(idBob).getNumberOfInformations());
		
		// export to string
		String json = pm.toJson();
		assertFalse(json.isEmpty());
		assertTrue(json.contains("VERSION"));
		assertTrue(json.contains("personIds"));
		assertTrue(json.contains("idLength"));
		
		
		// save single person
		try {
			pm.saveSinglePerson(idBob);
		} catch (IOException e) {
			fail("export person to file: "+e.getMessage());
		}
		
		
		// delete single person
		try {
			pm.removePersonFile(exportDir, idBob);
		} catch (IOException e) {
			fail("remove person's file: "+e.getMessage());
		}
		
		// save all
		try {
			pm.save();
		} catch (IOException e) {
			fail("export to file: "+e.getMessage());
		}
		
		// export to existing file - overwriting 
		try {
			pm.save();
			pm.save();
			pm.save();
		} catch (IOException e) {
			fail("export to file - overwriting: "+e.getMessage());
		}
		
		// load from a file
		Set<String> personIdsRef = pm.getPersonIds();
		PersonManager loadedPm = null;
		try {
			loadedPm = PersonManager.load(exportDir, idLength);
		} catch (IOException e) {
			fail("load from file");
		}
		for (String refId : personIdsRef) {
			assertTrue(loadedPm.containsPersonId(refId));
			assertEquals(pm.getPerson(refId).getName(), loadedPm.getPerson(refId).getName());
			assertEquals(pm.getPerson(refId).getId(), loadedPm.getPerson(refId).getId());
			assertEquals(pm.getPerson(refId).getNumberOfInformations(),
						 loadedPm.getPerson(refId).getNumberOfInformations());
		}
	}
}
