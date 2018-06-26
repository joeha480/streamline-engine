package org.daisy.streamline.engine.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.daisy.streamline.api.tasks.TaskGroupActivity;
import org.daisy.streamline.api.tasks.TaskGroupInformation;

class PathInfo {
	private final TaskGroupInformation convert;
	private final List<TaskGroupInformation> enhance;
	private final List<TaskGroupInformation> path;
	private final List<TaskGroupInformation> exclude;
	private int remainingDistance = 0;

	private PathInfo(TaskGroupInformation convert, List<TaskGroupInformation> enhance, List<TaskGroupInformation> path, List<TaskGroupInformation> exclude) {
		this.convert = convert;
		this.enhance = enhance;
		this.path = path;
		this.exclude = exclude;
		this.remainingDistance = convert.getEvaluationDistance();
	}

	static Stream<PathInfo> makePaths(List<TaskGroupInformation> inputs, List<TaskGroupInformation> specs, List<TaskGroupInformation> exclude) {
		if (inputs != null) {
			List<TaskGroupInformation> convert = getConverters(inputs, exclude);
			List<TaskGroupInformation> enhance = Collections.unmodifiableList(getEnhancers(inputs));
			List<TaskGroupInformation> path = Collections.unmodifiableList(new ArrayList<>(specs));
			List<TaskGroupInformation> excl = Collections.unmodifiableList(new ArrayList<>(exclude));
			return convert.stream().map(c->new PathInfo(c, enhance, path, excl));
		} else {
			return Stream.empty();
		}
	}

	TaskGroupInformation getConvert() {
		return convert;
	}

	List<TaskGroupInformation> getEnhance() {
		return enhance;
	}
	
	List<TaskGroupInformation> getExclude() {
		return exclude;
	}

	List<TaskGroupInformation> getPath() {
		return path;
	}
	
	int getRemainingDistance() {
		return remainingDistance;
	}

	void setRemainingDistance(int levels) {
		if (levels<0) {
			throw new IllegalArgumentException("Value must be >= 0: " + levels);
		}
		this.remainingDistance = levels;
	}


	
	private static List<TaskGroupInformation> getConverters(List<TaskGroupInformation> candidates, List<TaskGroupInformation> exclude) {
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