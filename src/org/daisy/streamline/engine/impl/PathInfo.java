package org.daisy.streamline.engine.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.daisy.streamline.api.tasks.TaskGroupActivity;
import org.daisy.streamline.api.tasks.TaskGroupInformation;

class PathInfo {
	private final List<TaskGroupInformation> convert;
	private final List<TaskGroupInformation> enhance;
	private final List<TaskGroupInformation> path;
	private final List<TaskGroupInformation> exclude;

	PathInfo(List<TaskGroupInformation> inputs, List<TaskGroupInformation> specs) {
		this(inputs, specs, Collections.emptyList());
	}

	private PathInfo(List<TaskGroupInformation> inputs, List<TaskGroupInformation> specs, List<TaskGroupInformation> exclude) {
		if (inputs != null) {
			this.convert = getConverters(inputs, exclude);
			this.enhance = getEnhancers(inputs);
		} else {
			this.convert = new ArrayList<>();
			this.enhance = new ArrayList<>();
		}

		this.path = new ArrayList<>(specs);
		this.exclude = exclude;
	}

	PathInfo with(List<TaskGroupInformation> inputs, TaskGroupInformation filter) {
		List<TaskGroupInformation> excl = new ArrayList<>(this.exclude);
		excl.add(filter);
		return new PathInfo(inputs, path, excl);
	}

	List<TaskGroupInformation> getConvert() {
		return convert;
	}

	List<TaskGroupInformation> getEnhance() {
		return enhance;
	}

	List<TaskGroupInformation> getPath() {
		return path;
	}
	
	static List<TaskGroupInformation> getConverters(List<TaskGroupInformation> candidates, List<TaskGroupInformation> exclude) {
		Objects.requireNonNull(exclude);
		return Objects.requireNonNull(candidates).stream()
				.filter(v-> v.getActivity()==TaskGroupActivity.CONVERT && !exclude.contains(v))
				.collect(Collectors.toList());
	}

	static List<TaskGroupInformation> getEnhancers(List<TaskGroupInformation> candidates) {
		return Objects.requireNonNull(candidates).stream()
				.filter(v->v.getActivity()==TaskGroupActivity.ENHANCE)
				.collect(Collectors.toList());
	}

}