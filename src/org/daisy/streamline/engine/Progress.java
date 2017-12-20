package org.daisy.streamline.engine;

import java.util.Date;

/**
 * Provides simple progress measuring and reporting. The internal stop watch is started
 * as soon as an instance is created.
 * @author Joel Håkansson
 *
 */
public class Progress {
	private ProgressEvent progress;
	private long tstamp;
	private final long start;
	double step;
	
	/**
	 * Creates a new instance. The internal stop watch is started immediately.
	 */
	public Progress() {
		this(System.currentTimeMillis());
	}

	//allows setting the current time, for testing
	Progress(long now) {
		progress = new ProgressEvent(0, new Date());
		start = now;
		reset(start);
		
	}
	
	private void reset(long now) {
		step = -1;
		tstamp = now;
		progress = new ProgressEvent(0, progress.getETC());
	}
	
	/**
	 * Gets the estimated time of completion.
	 * @return returns the estimated time of completion 
	 */
	public Date getETC() {
		return progress.getETC();
	}
	
	/**
	 * Gets the start date, in other words the time of instantiation.
	 * @return returns the start date
	 */
	public Date getStart() {
		return new Date(start);
	}
	
	/**
	 * Sets the current progress of the operation, in percent.
	 * @param val the current progress, in percent.
	 * @return the current progress
	 * @throws IllegalArgumentException if the value is out of range
	 * @throws IllegalStateException if the progress is less than or equal to previous reported value.
	 */
	public ProgressEvent updateProgress(double val) {
		return updateProgress(val, System.currentTimeMillis());
	}

	//allows setting the current time, for testing
	ProgressEvent updateProgress(double val, long now) {
		if (val<0 || val>1) {
			throw new IllegalArgumentException("Value out of range [0, 1]: " + val);
		}
		if (val<=progress.getProgress()) {
			reset(start);
		}
		double pD = val - progress.getProgress();
		long tD = now - tstamp;
		if (step > 0) {
			step = step * 0.3 + (tD / pD) * 0.7; 
		} else {
			step = (tD / pD);
		}
		double etcMs = step * (1 - val);
		progress = new ProgressEvent(val, new Date(now + (long)Math.round(etcMs)));
		tstamp = now;
		return progress;
	}
	
	/**
	 * Gets the current progress in percent.
	 * @return returns the current progress
	 */
	public double getProgress() {
		return progress.getProgress();
	}
	
	/**
	 * Gets the time since instantiation, in milliseconds.
	 * @return returns the number of milliseconds since instantiation.
	 */
	public long timeSinceStart() {
		return timeSinceStart(System.currentTimeMillis());
	}
	
	//allows setting the current time, for testing
	long timeSinceStart(long now) {
		return now-start;
	}

}
