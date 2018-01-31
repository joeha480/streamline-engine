package org.daisy.streamline.engine.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.daisy.streamline.api.tasks.TaskGroupInformation;

class QueueInfo {
	private final TaskGroupSpecificationFilter candidates;
	private final List<TaskGroupInformation> specs;
	private final List<TaskGroupInformation> exclude;

	QueueInfo(List<TaskGroupInformation> inputs, List<TaskGroupInformation> specs) {
		this(inputs, specs, Collections.emptyList());
	}

	private QueueInfo(List<TaskGroupInformation> inputs, List<TaskGroupInformation> specs, List<TaskGroupInformation> exclude) {
		this.candidates = TaskGroupSpecificationFilter.filterLocaleGroupByType(inputs, exclude);
		this.specs = new ArrayList<>(specs);
		this.exclude = exclude;
	}

	QueueInfo with(List<TaskGroupInformation> inputs, TaskGroupInformation filter) {
		List<TaskGroupInformation> excl = new ArrayList<>(this.exclude);
		excl.add(filter);
		return new QueueInfo(inputs, specs, excl);
	}

	List<TaskGroupInformation> getConvert() {
		return candidates.getConvert();
	}

	List<TaskGroupInformation> getEnhance() {
		return candidates.getEnhance();
	}

	List<TaskGroupInformation> getSpecs() {
		return specs;
	}

}