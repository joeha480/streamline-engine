package org.daisy.streamline.engine.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.daisy.streamline.api.tasks.TaskGroupInformation;

class QueueInfo {
	private TaskGroupSpecificationFilter candidates;
	private final List<TaskGroupInformation> inputs;
	private final List<TaskGroupInformation> specs;
	private final List<TaskGroupInformation> exclude;
	private int remainingDistance = 0;

	QueueInfo(List<TaskGroupInformation> inputs, List<TaskGroupInformation> specs) {
		this(inputs, specs, Collections.emptyList());
	}

	private QueueInfo(List<TaskGroupInformation> inputs, List<TaskGroupInformation> specs, List<TaskGroupInformation> exclude) {
		this.inputs = inputs;
		this.specs = new ArrayList<>(specs);
		this.exclude = exclude;
		this.candidates = null;
	}

	QueueInfo with(List<TaskGroupInformation> inputs, TaskGroupInformation filter) {
		List<TaskGroupInformation> excl = new ArrayList<>(this.exclude);
		excl.add(filter);
		QueueInfo ret = new QueueInfo(inputs, this.specs, excl);
		ret.getSpecs().addAll(getCandidate().getEnhance());
		ret.getSpecs().add(filter);
		return ret;
	}

	TaskGroupSpecificationFilter getCandidate() {
		if (candidates==null) {
			candidates = TaskGroupSpecificationFilter.filterLocaleGroupByType(inputs, exclude);
		}
		return candidates;
	}

	List<TaskGroupInformation> getSpecs() {
		return specs;
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

}