package org.matsim.drtviz.idle;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdSet;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.vehicles.Vehicle;

public class IdleDatabaseListener implements ActivityStartEventHandler, ActivityEndEventHandler {
	private final IdleDatabase database;
	private final IdSet<Vehicle> isIdleing = new IdSet<>(Vehicle.class);

	public IdleDatabaseListener(IdleDatabase database) {
		this.database = database;
	}

	private boolean isRelevant(Id<Vehicle> vehicleId) {
		return vehicleId.toString().contains("drt");
	}

	@Override
	public void handleEvent(ActivityStartEvent event) {
		Id<Vehicle> vehicleId = Id.createVehicleId(event.getPersonId());

		if (isRelevant(vehicleId)) {
			database.startIdle(event.getTime(), vehicleId, event.getLinkId());
			isIdleing.add(vehicleId);
		}
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		Id<Vehicle> vehicleId = Id.createVehicleId(event.getPersonId());

		if (isRelevant(vehicleId)) {
			if (isIdleing.remove(vehicleId)) {
				database.endIdle(event.getTime(), vehicleId, event.getLinkId());
			}
		}
	}
}
