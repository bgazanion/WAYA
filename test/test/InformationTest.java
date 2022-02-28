package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import waya.engine.Information;

class InformationTest {

	@Test
	void testAll() {
		// constructor
		Information info = new Information("dogs");
		assertEquals(info.getName(), "dogs");
		assertEquals(info.getNumberOfValues(), 0);
		assertThrows(IndexOutOfBoundsException.class, () -> { info.getValue(0); });
		assertThrows(IndexOutOfBoundsException.class, () -> { info.getValue(1); });
	
		// set name
		info.setName("not dogs");
		assertEquals(info.getName(), "not dogs");
		info.setName("dogs");
		
		// add values
		info.addValue("carl");
		info.addValue("joe");
		info.addValue("edd");
		assertEquals(info.getValue(0), "carl");
		assertEquals(info.getValue(1), "joe");
		assertEquals(info.getValue(2), "edd");
		assertEquals(info.getNumberOfValues(), 3);
		assertThrows(IndexOutOfBoundsException.class, () -> { info.getValue(3); });
		
		// copy constructor
		Information copy = new Information(info);
		copy.setName("copy");
		copy.removeValue(2);
		copy.changeValue(1, "no more joe");
		assertEquals(info.getName(), "dogs");
		assertEquals(info.getNumberOfValues(), 3);
		assertEquals(info.getValue(0), "carl");
		assertEquals(info.getValue(1), "joe");
		assertEquals(info.getValue(2), "edd");
		
		// insert value
		info.insertValue(1, "bart");
		assertEquals(info.getNumberOfValues(), 4);
		assertEquals(info.getValue(0), "carl");		
		assertEquals(info.getValue(1), "bart");
		assertEquals(info.getValue(2), "joe");
		assertEquals(info.getValue(3), "edd");
		assertThrows(IndexOutOfBoundsException.class, () -> { info.getValue(4); });
		
		// swap values
		info.swapValues(0, 2);
		assertEquals(info.getNumberOfValues(), 4);
		assertEquals(info.getValue(0), "joe");		
		assertEquals(info.getValue(1), "bart");
		assertEquals(info.getValue(2), "carl");
		assertEquals(info.getValue(3), "edd");
		assertThrows(IndexOutOfBoundsException.class, () -> { info.getValue(4); });
		assertThrows(IndexOutOfBoundsException.class, () -> { info.swapValues(0, 4); });
		assertThrows(IndexOutOfBoundsException.class, () -> { info.swapValues(4, 1); });
		
		// remove values
		info.removeValue(0);
		info.removeValue(0);
		assertEquals(info.getNumberOfValues(), 2);
		assertEquals(info.getValue(0), "carl");
		assertEquals(info.getValue(1), "edd");
		assertThrows(IndexOutOfBoundsException.class, () -> { info.getValue(2); });
		assertThrows(IndexOutOfBoundsException.class, () -> { info.removeValue(2); });
		assertThrows(IndexOutOfBoundsException.class, () -> { info.removeValue(-1); });
		
		// to json
		String json = info.toJson();
		assertEquals(json, "{\"name\":\"dogs\",\"values\":[\"carl\",\"edd\"],\"VERSION\":1}");
	}
}
