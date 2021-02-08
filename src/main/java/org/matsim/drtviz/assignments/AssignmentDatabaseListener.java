package org.matsim.drtviz.assignments;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.passenger.PassengerDroppedOffEvent;
import org.matsim.contrib.dvrp.passenger.PassengerDroppedOffEventHandler;
import org.matsim.contrib.dvrp.passenger.PassengerRequestScheduledEvent;
import org.matsim.contrib.dvrp.passenger.PassengerRequestScheduledEventHandler;
import org.matsim.vehicles.Vehicle;

public class AssignmentDatabaseListener
		implements PassengerRequestScheduledEventHandler, PassengerDroppedOffEventHandler {
	private final AssignmentDatabase database;

	public AssignmentDatabaseListener(AssignmentDatabase database) {
		this.database = database;
	}

	@Override
	public void handleEvent(PassengerRequestScheduledEvent event) {
		database.startAssignment(event.getRequestId(), Id.create(event.getVehicleId(), Vehicle.class), event.getTime());
	}

	@Override
	public void handleEvent(PassengerDroppedOffEvent event) {
		database.finishAssignment(event.getRequestId(), event.getTime());
	}
}
