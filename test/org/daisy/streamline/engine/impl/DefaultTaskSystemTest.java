package org.daisy.streamline.engine.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.streamline.api.tasks.TaskGroupInformation;
import org.daisy.streamline.api.tasks.TaskSystemException;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class DefaultTaskSystemTest {

	static Map<String, List<TaskGroupInformation>> inputs = new HashMap<>();
	static Map<String, List<TaskGroupInformation>> inputsE = new HashMap<>();
	static String loc = "sv-SE";
	static {
		inputs.put("A", buildSpecs(loc, "A", false, "B", "C"));
		inputs.put("B", buildSpecs(loc, "B", false, "D"));
		inputs.put("C", buildSpecs(loc, "C", false, "D", "E"));
		inputs.put("D", buildSpecs(loc, "D", false, "E", "G"));
		inputs.put("E", buildSpecs(loc, "E", false, "F"));
		inputsE.put("A", buildSpecs(loc, "A", true, "B", "C"));
		inputsE.put("B", buildSpecs(loc, "B", true, "D"));
		inputsE.put("C", buildSpecs(loc, "C", true, "D", "E"));
		inputsE.put("D", buildSpecs(loc, "D", true, "E", "G"));
		inputsE.put("E", buildSpecs(loc, "E", true, "F"));
		List<TaskGroupInformation> sp = new ArrayList<>();
		sp.add(TaskGroupInformation.newEnhanceBuilder("G").build());
		inputsE.put("G", sp);
	}
	
	@Test
	public void testPath_01() throws TaskSystemException {
		List<TaskGroupInformation> ret = DefaultTaskSystem.getPathSpecifications("A", "E", inputs);
		assertEquals(2, ret.size());
		assertEquals("A -> C (sv-SE)", asString(ret.get(0)));
		assertEquals("C -> E (sv-SE)", asString(ret.get(1)));
	}
	
	@Test
	public void testPath_02() throws TaskSystemException {
		List<TaskGroupInformation> ret = DefaultTaskSystem.getPathSpecifications("A", "F", inputs);
		assertEquals(3, ret.size());
		assertEquals("A -> C (sv-SE)", asString(ret.get(0)));
		assertEquals("C -> E (sv-SE)", asString(ret.get(1)));
		assertEquals("E -> F (sv-SE)", asString(ret.get(2)));
	}
	
	@Test
	public void testPath_03() throws TaskSystemException {
		List<TaskGroupInformation> ret = DefaultTaskSystem.getPathSpecifications("A", "G", inputs);
		assertEquals(3, ret.size());
		assertEquals("A -> B (sv-SE)", asString(ret.get(0)));
		assertEquals("B -> D (sv-SE)", asString(ret.get(1)));
		assertEquals("D -> G (sv-SE)", asString(ret.get(2)));
	}
	
	@Test (expected=TaskSystemException.class)
	public void testPathLoop_01() throws TaskSystemException {
		Map<String, List<TaskGroupInformation>> inps = new HashMap<>();
		inps.put("A", buildSpecs(loc, "A", false, "B"));
		inps.put("B", buildSpecs(loc, "B", false, "A"));
		DefaultTaskSystem.getPathSpecifications("A", "C", inps);
	}
	
	@Test (expected=TaskSystemException.class)
	public void testPathLoop_02() throws TaskSystemException {
		Map<String, List<TaskGroupInformation>> inps = new HashMap<>();
		inps.put("A", buildSpecs(loc, "A", false, "B"));
		inps.put("B", buildSpecs(loc, "B", false, "C"));
		inps.put("C", buildSpecs(loc, "C", false, "A"));
		DefaultTaskSystem.getPathSpecifications("A", "D", inps);
	}
	
	@Test (expected=TaskSystemException.class)
	public void testPathLoop_03() throws TaskSystemException {
		Map<String, List<TaskGroupInformation>> inps = new HashMap<>();
		inps.put("A", buildSpecs(loc, "A", false, "B"));
		inps.put("B", buildSpecs(loc, "B", false, "C"));
		inps.put("C", buildSpecs(loc, "C", false, "B"));
		inps.put("D", buildSpecs(loc, "D", false, "E"));
		DefaultTaskSystem.getPathSpecifications("A", "E", inps);
	}
	
	@Test
	public void testPathEnhance_01() throws TaskSystemException {
		List<TaskGroupInformation> ret = DefaultTaskSystem.getPathSpecifications("A", "G", inputsE);
		assertEquals(7, ret.size());
		assertEquals("A -> A (sv-SE)", asString(ret.get(0)));
		assertEquals("A -> B (sv-SE)", asString(ret.get(1)));
		assertEquals("B -> B (sv-SE)", asString(ret.get(2)));
		assertEquals("B -> D (sv-SE)", asString(ret.get(3)));
		assertEquals("D -> D (sv-SE)", asString(ret.get(4)));
		assertEquals("D -> G (sv-SE)", asString(ret.get(5)));
		assertEquals("G -> G (sv-SE)", asString(ret.get(6)));
	}
	
	@Test
	public void testPathDistance_01() throws TaskSystemException {
		Map<String, List<TaskGroupInformation>> in = new HashMap<>();
		List<TaskGroupInformation> specs = new ArrayList<>();
		// Using evaluation distance to extend path P1 (A -> C), so that it
		// becomes A -> _ -> C. It is now equal in length compared with path P2 (A -> B -> C).
		// P2 is now evaluated first and therefore selected.
		specs.add(TaskGroupInformation.newConvertBuilder("A", "B").build());
		specs.add(TaskGroupInformation.newConvertBuilder("A", "C").evaluationDistance(1).build());
		in.put("A", specs);
		in.put("B", buildSpecs(loc, "B", false, "C"));
		assertABC(DefaultTaskSystem.getPathSpecifications("A", "C", in));
	}
	
	@Test
	public void testPathDistance_02() throws TaskSystemException {
		Map<String, List<TaskGroupInformation>> in = new HashMap<>();
		List<TaskGroupInformation> specs = new ArrayList<>();
		// Using evaluation distance to extend path P1 (A -> C), so that it
		// becomes A -> _ -> C. It is now equal in length compared with path P2 (A -> B -> C).
		// However, unlike the test above, P1 is evaluated first and therefore selected anyway.
		specs.add(TaskGroupInformation.newConvertBuilder("A", "C").evaluationDistance(1).build());
		specs.add(TaskGroupInformation.newConvertBuilder("A", "B").build());
		in.put("A", specs);
		in.put("B", buildSpecs(loc, "B", false, "C"));
		assertAC(DefaultTaskSystem.getPathSpecifications("A", "C", in));
	}
	
	@Test
	public void testPathDistance_03() throws TaskSystemException {
		Map<String, List<TaskGroupInformation>> in = new HashMap<>();
		List<TaskGroupInformation> specs = new ArrayList<>();
		// Using evaluation distance to extend path P1 (A -> C), so that it
		// becomes A -> _ -> _ -> C. It is now longer than path P2 (A -> B -> C).
		// In this case, it doesn't matter which path is evaluated first, P2 is selected.
		specs.add(TaskGroupInformation.newConvertBuilder("A", "C").evaluationDistance(2).build());
		specs.add(TaskGroupInformation.newConvertBuilder("A", "B").build());
		in.put("A", specs);
		in.put("B", buildSpecs(loc, "B", false, "C"));
		assertABC(DefaultTaskSystem.getPathSpecifications("A", "C", in));
	}
	
	@Test
	public void testPathDistanceReduction_01() throws TaskSystemException {
		Map<String, List<TaskGroupInformation>> in = new HashMap<>();
		List<TaskGroupInformation> specs = new ArrayList<>();
		specs.add(TaskGroupInformation.newConvertBuilder("A", "B").evaluationDistance(10001).build());
		specs.add(TaskGroupInformation.newConvertBuilder("A", "C").evaluationDistance(10002).build());
		in.put("A", specs);
		in.put("B", buildSpecs(loc, "B", false, "C"));
		assertABC(DefaultTaskSystem.getPathSpecifications("A", "C", in));
	}
	
	private static void assertAC(List<TaskGroupInformation> ret) {
		assertEquals(1, ret.size());
		assertEquals("A -> C (sv-SE)", asString(ret.get(0)));		
	}
	
	private static void assertABC(List<TaskGroupInformation> ret) {
		assertEquals(2, ret.size());
		assertEquals("A -> B (sv-SE)", asString(ret.get(0)));
		assertEquals("B -> C (sv-SE)", asString(ret.get(1)));		
	}
	
	private static List<TaskGroupInformation> buildSpecs(String locale, String input, boolean withEnhance, String ... outputs) {
		return buildSpecs(locale, input, withEnhance, 0, outputs);
	}
	
	private static List<TaskGroupInformation> buildSpecs(String locale, String input, boolean withEnhance, int d, String ... outputs) {
		List<TaskGroupInformation> specs = new ArrayList<>();
		for (String r : outputs) {
			specs.add(TaskGroupInformation.newConvertBuilder(input, r).evaluationDistance(d).build());
		}
		if (withEnhance) {
			specs.add(TaskGroupInformation.newEnhanceBuilder(input).build());
		}
		return specs;
	}
	
	private static String asString(TaskGroupInformation spec) {
		return spec.getInputType() + " -> " + spec.getOutputType() + " (sv-SE)";
	}

}
