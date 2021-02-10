package org.matsim.drtviz.run;

import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;

public class ProgressHandler implements ActivityStartEventHandler {
	private double currentTime = 0.0;
	private boolean isFinished = false;

	@Override
	public void handleEvent(ActivityStartEvent event) {
		this.currentTime = event.getTime();
	}

	public void finish() {
		isFinished = true;
	}

	public double getCurrentTime() {
		return currentTime;
	}

	public boolean isFinished() {
		return isFinished;
	}
}
