package org.daisy.streamline.engine.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.daisy.streamline.api.tasks.TaskGroupInformation;

class PathInfo {
	private final TaskGroupSpecificationFilter candidates;
	private final List<TaskGroupInformation> path;
	private final List<TaskGroupInformation> exclude;

	PathInfo(List<TaskGroupInformation> inputs, List<TaskGroupInformation> specs) {
		this(inputs, specs, Collections.emptyList());
	}

	private PathInfo(List<TaskGroupInformation> inputs, List<TaskGroupInformation> specs, List<TaskGroupInformation> exclude) {
		this.candidates = TaskGroupSpecificationFilter.filterLocaleGroupByType(inputs, exclude);
		this.path = new ArrayList<>(specs);
		this.exclude = exclude;
	}

	PathInfo with(List<TaskGroupInformation> inputs, TaskGroupInformation filter) {
		List<TaskGroupInformation> excl = new ArrayList<>(this.exclude);
		excl.add(filter);
		return new PathInfo(inputs, path, excl);
	}

	List<TaskGroupInformation> getConvert() {
		return candidates.getConvert();
	}

	List<TaskGroupInformation> getEnhance() {
		return candidates.getEnhance();
	}

	List<TaskGroupInformation> getPath() {
		return path;
	}

}