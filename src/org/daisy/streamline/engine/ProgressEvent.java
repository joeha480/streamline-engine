package org.daisy.streamline.engine;

import java.util.Date;

/**
 * Provides a progress event.
 * @author Joel Håkansson
 */
public final class ProgressEvent {
	private final double progress;
	private final Date etc;

	/**
	 * Creates a new progress event.
	 * @param progress the progress, in percent
	 * @param etc the estimated time of completion
	 */
	public ProgressEvent(double progress, Date etc) {
		this.progress = progress;
		this.etc = etc;
	}

	/**
	 * Gets the progress, in percent.
	 * @return the progress
	 */
	public double getProgress() {
		return progress;
	}

	/**
	 * Gets the estimated time of completion.
	 * @return the estimated time of completion
	 */
	public Date getETC() {
		return etc;
	}
}