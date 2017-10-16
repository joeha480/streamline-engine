package org.daisy.streamline.engine.impl;

import org.daisy.streamline.api.tasks.TaskGroupFactoryMaker;
import org.daisy.streamline.api.tasks.TaskGroupFactoryMakerService;
import org.daisy.streamline.api.tasks.TaskGroupInformation;
import org.daisy.streamline.api.tasks.TaskSystem;
import org.daisy.streamline.api.tasks.TaskSystemFactory;
import org.daisy.streamline.api.tasks.TaskSystemFactoryException;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

/**
 * Provides a default task system factory. The default task system
 * collects possible steps from the task group factory and finds a
 * path through the exisiting task groups, if possible.
 * 
 * @author Joel Håkansson
 */
@Component
public class DefaultTaskSystemFactory implements TaskSystemFactory {
	private TaskGroupFactoryMakerService imf;

	@Override
	public boolean supportsSpecification(String inputFormat, String outputFormat, String locale) {
		boolean inputMatch = false;
		boolean outputMatch = false;
		for (TaskGroupInformation info : imf.list(locale)) {
			if (info.getInputFormat().equals(inputFormat)) {
				inputMatch = true;
			}
			if (info.getOutputFormat().equals(outputFormat)) {
				outputMatch = true;
			}
			if (inputMatch && outputMatch) {
				return true;
			}
		}
		return false;
	}

	@Override
	public TaskSystem newTaskSystem(String inputFormat, String outputFormat, String locale)
			throws TaskSystemFactoryException {
		if (supportsSpecification(inputFormat, outputFormat, locale)) {
			return new DefaultTaskSystem("Default Task System", inputFormat, outputFormat, locale, imf);
		}
		throw new TaskSystemFactoryException("Unsupported specification: " + locale + "(" + inputFormat + "->" + outputFormat + ")");
	}

	@Override
	public int getPriority() {
		// Only use this when there are no other factories defined.
		// Note that the absolute minimum integer is not used, because
		// that will likely interfere with other implementations that 
		// wants to be the default. By choosing a slightly larger value,
		// the probability for deterministic behavior in the presence of
		// multiple implementations increases.
		return Integer.MIN_VALUE+47382;
	}

	/**
	 * Sets a factory dependency.
	 * @param service the dependency
	 */
	@Reference
	public void setInputManagerFactory(TaskGroupFactoryMakerService service) {
		this.imf = service;
	}

	/**
	 * Removes a factory dependency.
	 * @param service the dependency to remove
	 */
	public void unsetInputManagerFactory(TaskGroupFactoryMakerService service) {
		this.imf = null;
	}

	@Override
	public void setCreatedWithSPI() {
		if (imf == null) {
			imf = TaskGroupFactoryMaker.newInstance();
		}
	}

}
