package org.matsim.drtviz.locations;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.vehicles.Vehicle;

public class LocationDatabaseListener implements LinkEnterEventHandler {
	private final LocationDatabase database;

	public LocationDatabaseListener(LocationDatabase database) {
		this.database = database;
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		if (isRelevant(event.getVehicleId())) {
			database.addLocation(event.getTime(), event.getVehicleId(), event.getLinkId());
		}
	}

	private boolean isRelevant(Id<Vehicle> vehicleId) {
		return vehicleId.toString().contains("drt");
	}
}
