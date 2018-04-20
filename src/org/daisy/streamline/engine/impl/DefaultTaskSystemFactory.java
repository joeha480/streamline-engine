package org.daisy.streamline.engine.impl;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.daisy.streamline.api.media.FormatIdentifier;
import org.daisy.streamline.api.tasks.TaskGroupFactoryMaker;
import org.daisy.streamline.api.tasks.TaskGroupFactoryMakerService;
import org.daisy.streamline.api.tasks.TaskSystem;
import org.daisy.streamline.api.tasks.TaskSystemException;
import org.daisy.streamline.api.tasks.TaskSystemFactory;
import org.daisy.streamline.api.tasks.TaskSystemFactoryException;
import org.daisy.streamline.api.tasks.TaskSystemInformation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

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
		return supportsSpecification(new TaskSystemInformation.Builder(
				Objects.requireNonNull(FormatIdentifier.with(inputFormat)),
				Objects.requireNonNull(FormatIdentifier.with(outputFormat)))
				.locale(Objects.requireNonNull(locale))
				.build()
		);
	}
	
	private boolean supportsSpecification(TaskSystemInformation x) {	
		try {
			DefaultTaskSystem.getPath(imf, x, x.getLocale().get());
			return true;
		} catch (TaskSystemException e) {
			return false;
		}
	}

	@Override
	public Set<FormatIdentifier> listInputs() {
		return imf.listAll().stream().map(v->v.getInputType()).collect(Collectors.toSet());
	}

	@Override
	public Set<FormatIdentifier> listOutputs() {
		return imf.listAll().stream().map(v->v.getOutputType()).collect(Collectors.toSet());
	}

	@Override
	public Set<TaskSystemInformation> listForInput(FormatIdentifier input, String locale) {
		return imf.listAll().stream()
			.map(v->new TaskSystemInformation.Builder(input, v.getOutputType()).locale(locale).build())
			.filter(v->supportsSpecification(v))
			.collect(Collectors.toSet());
	}

	@Override
	public Set<TaskSystemInformation> listForOutput(FormatIdentifier output, String locale) {
		return imf.listAll().stream()
				.map(v->new TaskSystemInformation.Builder(v.getInputType(), output).locale(locale).build())
				.filter(v->supportsSpecification(v))
				.collect(Collectors.toSet());
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
	@Reference(cardinality=ReferenceCardinality.MANDATORY)
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
