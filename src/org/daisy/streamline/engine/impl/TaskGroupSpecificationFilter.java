package org.daisy.streamline.engine.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.daisy.streamline.api.tasks.TaskGroupActivity;
import org.daisy.streamline.api.tasks.TaskGroupInformation;

class TaskGroupSpecificationFilter {
	private final List<TaskGroupInformation> convert;
	private final List<TaskGroupInformation> enhance;
	
	private TaskGroupSpecificationFilter(List<TaskGroupInformation> convert, List<TaskGroupInformation> enhance) {
		this.convert = convert;
		this.enhance = enhance;
	}

	static TaskGroupSpecificationFilter filterLocaleGroupByType(List<TaskGroupInformation> candidates, List<TaskGroupInformation> exclude) {
		return new TaskGroupSpecificationFilter(getConverters(candidates, exclude), getEnhancers(candidates));
	}
	
	static List<TaskGroupInformation> getConverters(List<TaskGroupInformation> candidates, List<TaskGroupInformation> exclude) {
		if (candidates != null) {
			return candidates.stream()
					.filter(v-> v.getActivity()==TaskGroupActivity.CONVERT && !exclude.contains(v))
					.collect(Collectors.toList());
		} else {
			return new ArrayList<>();
		}
	}
	
	static List<TaskGroupInformation> getEnhancers(List<TaskGroupInformation> candidates) {
		if (candidates != null) {
			return candidates.stream()
					.filter(v->v.getActivity()==TaskGroupActivity.ENHANCE)
					.collect(Collectors.toList());
		} else {
			return new ArrayList<>();
		}
	}

	List<TaskGroupInformation> getConvert() {
		return convert;
	}

	List<TaskGroupInformation> getEnhance() {
		return enhance;
	}

}