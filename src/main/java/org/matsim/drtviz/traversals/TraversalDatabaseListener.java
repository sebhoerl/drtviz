package org.matsim.drtviz.traversals;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
import org.matsim.vehicles.Vehicle;

public class TraversalDatabaseListener implements LinkEnterEventHandler, LinkLeaveEventHandler,
		VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler {
	private final TraversalDatabase database;

	public TraversalDatabaseListener(TraversalDatabase database) {
		this.database = database;
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		if (isRelevant(event.getVehicleId())) {
			database.startTraversal(event.getTime(), event.getVehicleId(), event.getLinkId());
		}
	}

	@Override
	public void handleEvent(VehicleEntersTrafficEvent event) {
		if (isRelevant(event.getVehicleId())) {
			database.startTraversal(event.getTime(), event.getVehicleId(), event.getLinkId());
		}
	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		if (isRelevant(event.getVehicleId())) {
			database.endTraversal(event.getTime(), event.getVehicleId(), event.getLinkId());
		}
	}

	@Override
	public void handleEvent(VehicleLeavesTrafficEvent event) {
		if (isRelevant(event.getVehicleId())) {
			database.endTraversal(event.getTime(), event.getVehicleId(), event.getLinkId());
		}
	}

	private boolean isRelevant(Id<Vehicle> vehicleId) {
		return vehicleId.toString().contains("drt");
	}
}
