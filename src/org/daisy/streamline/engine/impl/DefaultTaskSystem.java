package org.daisy.streamline.engine.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.daisy.streamline.api.media.FormatIdentifier;
import org.daisy.streamline.api.option.UserOption;
import org.daisy.streamline.api.tasks.CompiledTaskSystem;
import org.daisy.streamline.api.tasks.DefaultCompiledTaskSystem;
import org.daisy.streamline.api.tasks.TaskGroup;
import org.daisy.streamline.api.tasks.TaskGroupActivity;
import org.daisy.streamline.api.tasks.TaskGroupFactoryMakerService;
import org.daisy.streamline.api.tasks.TaskGroupInformation;
import org.daisy.streamline.api.tasks.TaskSystem;
import org.daisy.streamline.api.tasks.TaskSystemException;
import org.daisy.streamline.api.tasks.TaskSystemInformation;


/**
 * <p>Transforms XML into braille in PEF 2008-1 format.</p>
 * <p>Transforms documents into text format.</p>
 * 
 * <p>This TaskSystem consists of the following steps:</p>
 * <ol>
	 * <li>Input Manager. Validates and converts input to OBFL.</li>
	 * <li>OBFL to PEF converter.
	 * 		Translates all characters into braille, and puts the text flow onto pages.</li>
 * </ol>
 * <p>The result should be validated against the PEF Relax NG schema using int_daisy_validator.</p>
 * @author Joel Håkansson
 */
public class DefaultTaskSystem implements TaskSystem {
	private static final Logger logger = Logger.getLogger(DefaultTaskSystem.class.getCanonicalName());
	private final String inputFormat;
	private final String outputFormat;
	private final String context;
	private final String name;
	private final TaskGroupFactoryMakerService imf;

	/**
	 * Creates a new Dotify task system with the specified parameters.
	 * @param name the name of the task system
	 * @param inputFormat the input format
	 * @param outputFormat the output format
	 * @param context the context locale
	 * @param imf a task group factory maker service
	 */
	public DefaultTaskSystem(String name, String inputFormat, String outputFormat, String context, TaskGroupFactoryMakerService imf) {
		this.context = context;
		this.inputFormat = inputFormat;
		this.outputFormat = outputFormat;
		this.name = name;
		this.imf = imf;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public CompiledTaskSystem compile(Map<String, Object> pa) throws TaskSystemException {
		
		CompiledTaskSystemImpl setup = new CompiledTaskSystemImpl(name, getOptions());
		setup.addOption(new UserOption.Builder("route-by").description("Route by the specified formats").build());
		Object routeObj = pa.get("route-by");
		String[] route = null;
		if (routeObj!=null) {
			 route = routeObj.toString().split(",\\s*");
		}
		List<FormatIdentifier> stops = new ArrayList<>();
		stops.add(FormatIdentifier.with(inputFormat));
		if (route!=null) {
			Arrays.asList(route).stream()
				.map(FormatIdentifier::with)
				.forEach(v->stops.add(v));
		}
		stops.add(FormatIdentifier.with(outputFormat));
		for (int i=0; i<stops.size()-1; i++) {
			setup.addTaskGroups(getPath(imf, new TaskSystemInformation.Builder(stops.get(i), stops.get(i+1)).build(), context), pa);
		}

		return setup;
	}
	
	class CompiledTaskSystemImpl extends DefaultCompiledTaskSystem {

		public CompiledTaskSystemImpl(String name, List<UserOption> options) {
			super(name, options);
		}
		
		public void addTaskGroups(List<TaskGroupInformation> specs, Map<String, Object> h) throws TaskSystemException {
			for (TaskGroupInformation spec : specs) {
				addTaskGroup(spec, h);
			}
		}
		
		public void addTaskGroup(TaskGroupInformation spec, Map<String, Object> h) throws TaskSystemException {
			if (spec.getActivity()==TaskGroupActivity.ENHANCE) {
				// For enhance, only include the options required to enable the task group. Once enabled,
				// additional options may be presented
				for (UserOption o : spec.getRequiredOptions()) {
					addOption(o);
				}
			}
			if (spec.getActivity()==TaskGroupActivity.CONVERT || matchesRequiredOptions(spec, h, false)) {
				TaskGroup g = imf.newTaskGroup(spec, context);
				//TODO: these options should be on the group level instead of on the system level
				List<UserOption> opts = g.getOptions();
				if (opts!=null) {
					for (UserOption o : opts) {
						addOption(o);
					}
				}
				addAll(g.compile(h));
			}
		}
		
	}
	
	/**
	 * Finds a path for the given specifications
	 * @param input the input format
	 * @param output the output format
	 * @param locale the target locale
	 * @param parameters the parameters
	 * @return returns a list of task groups
	 * @throws TaskSystemException 
	 */
	static List<TaskGroupInformation> getPath(TaskGroupFactoryMakerService imf, TaskSystemInformation def, String locale) throws TaskSystemException {
		logger.info("Finding path...");
		Set<TaskGroupInformation> specs = imf.list(locale);
		Map<String, List<TaskGroupInformation>> byInput = byInput(specs);

		return getPathSpecifications(def.getInputType().getIdentifier(), def.getOutputType().getIdentifier(), byInput);
	}
	
	/**
	 * Gets the shortest path that matches the specification (breadth-first search)
	 * @param input the input format
	 * @param output the output format
	 * @param locale the locale
	 * @param inputs a list of specifications ordered by input format
	 * @return returns the shortest path
	 */
	static List<TaskGroupInformation> getPathSpecifications(String input, String output, Map<String, List<TaskGroupInformation>> _inputs) throws TaskSystemException {
		// Changed this to an unmodifiable map, just to make sure that it is not modified.
		Map<String, List<TaskGroupInformation>> inputs = Collections.unmodifiableMap(_inputs);

		// queue roots
		List<PathInfo> queue = new ArrayList<>();
		PathInfo.makePaths(inputs.get(input), Collections.emptyList(), Collections.emptyList())
				.forEachOrdered(v->queue.add(v));
		
		while (!queue.isEmpty()) {
			PathInfo current = queue.remove(0);
			TaskGroupInformation candidate = current.getConvert();
			logger.fine("Evaluating " + candidate.getInputType() + " -> " + candidate.getOutputType());
			if (candidate.getOutputType().getIdentifier().equals(output)) {
				List<TaskGroupInformation> ret = new ArrayList<>(current.getPath());
				ret.addAll(current.getEnhance());
				ret.add(candidate);
				List<TaskGroupInformation> enhancers = inputs.get(candidate.getOutputType().getIdentifier());
				if (enhancers!=null) {
					ret.addAll(PathInfo.getEnhancers(enhancers));
				}
				return ret;
			} else {
				// add for later evaluation
				List<TaskGroupInformation> excl = new ArrayList<>(current.getExclude());
				excl.add(candidate);
				List<TaskGroupInformation> x = inputs.get(candidate.getOutputType().getIdentifier());
				if (x!=null) {
					List<TaskGroupInformation> info = new ArrayList<>(current.getPath());
					info.addAll(current.getEnhance());
					info.add(candidate);
					PathInfo.makePaths(x, info, excl)
						.forEachOrdered(v->queue.add(v));
				}
			}
		}
		throw new TaskSystemException("Cannot find path " + input + "->" + output);
	}
	
	static boolean matchesRequiredOptions(TaskGroupInformation candidate, Map<String, Object> parameters, boolean emptyReturn) {
		if (candidate.getRequiredOptions().isEmpty()) {
			return emptyReturn;
		}
		for (UserOption option : candidate.getRequiredOptions()) {
			String key = option.getKey();
			if (!parameters.containsKey(key)) {
				return false;
			} else {
				Object value = parameters.get(key);
				if (!option.acceptsValue(value.toString())) {
					return false;
				}
			}
		}
		return true;
	}
	
	static Map<String, List<TaskGroupInformation>> byInput(Set<TaskGroupInformation> specs) {
		Map<String, List<TaskGroupInformation>> ret = new HashMap<>();
		for (TaskGroupInformation spec : specs) {
			List<TaskGroupInformation> group = ret.get(spec.getInputType().getIdentifier());
			if (group==null) {
				group = new ArrayList<>();
				ret.put(spec.getInputType().getIdentifier(), group);
			}
			group.add(spec);
		}
		return ret;
	}

}