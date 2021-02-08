package org.matsim.drtviz.relocations;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdMap;
import org.matsim.contrib.drt.schedule.DrtTaskBaseType;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerDroppedOffEvent;
import org.matsim.contrib.dvrp.passenger.PassengerDroppedOffEventHandler;
import org.matsim.contrib.dvrp.passenger.PassengerPickedUpEvent;
import org.matsim.contrib.dvrp.passenger.PassengerPickedUpEventHandler;
import org.matsim.contrib.dvrp.vrpagent.TaskStartedEvent;
import org.matsim.contrib.dvrp.vrpagent.TaskStartedEventHandler;
import org.matsim.vehicles.Vehicle;

public class RelocationDatabaseListener
		implements TaskStartedEventHandler, PassengerPickedUpEventHandler, PassengerDroppedOffEventHandler {
	private final RelocationDatabase database;

	private final IdMap<DvrpVehicle, Integer> passengers = new IdMap<>(DvrpVehicle.class);
	private final IdMap<DvrpVehicle, Double> startTimes = new IdMap<>(DvrpVehicle.class);

	public RelocationDatabaseListener(RelocationDatabase database) {
		this.database = database;
	}

	@Override
	public void handleEvent(PassengerPickedUpEvent event) {
		passengers.put(event.getVehicleId(), passengers.getOrDefault(event.getVehicleId(), 0) + 1);
	}

	@Override
	public void handleEvent(PassengerDroppedOffEvent event) {
		passengers.put(event.getVehicleId(), passengers.get(event.getVehicleId()) - 1);
	}

	@Override
	public void handleEvent(TaskStartedEvent event) {
		if (DrtTaskBaseType.DRIVE.isBaseTypeOf(event.getTaskType())) {
			if (passengers.getOrDefault(event.getDvrpVehicleId(), 0) == 0) {
				startTimes.put(event.getDvrpVehicleId(), event.getTime());
			}
		} else if (DrtTaskBaseType.STAY.isBaseTypeOf(event.getTaskType())) {
			Double startTime = startTimes.remove(event.getDvrpVehicleId());

			if (startTime != null) {
				database.addRelocation(startTime, event.getTime(), Id.create(event.getDvrpVehicleId(), Vehicle.class),
						event.getLinkId());
			}
		} else {
			startTimes.remove(event.getDvrpVehicleId());
		}
	}
}
