package org.matsim.drtviz;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdSet;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.TransitDriverStartsEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.TransitDriverStartsEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.drtviz.idle.IdleDatabase;
import org.matsim.drtviz.traversals.TraversalDatabase;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.vehicles.Vehicle;

public class TransitTraversalAndIdleListener implements LinkEnterEventHandler, LinkLeaveEventHandler,
		VehicleArrivesAtFacilityEventHandler, VehicleDepartsAtFacilityEventHandler, TransitDriverStartsEventHandler {
	private final TraversalDatabase traversalDatabase;
	private final IdleDatabase idleDatabase;
	private final TransitSchedule schedule;

	private final IdSet<Vehicle> relevantIds = new IdSet<>(Vehicle.class);
	private final IdSet<Vehicle> isFirstArrivalEvent = new IdSet<>(Vehicle.class);
	private final IdSet<Vehicle> isDeparting = new IdSet<>(Vehicle.class);

	public TransitTraversalAndIdleListener(TraversalDatabase traversalDatabase, IdleDatabase idleDatabase,
			TransitSchedule schedule) {
		this.traversalDatabase = traversalDatabase;
		this.idleDatabase = idleDatabase;
		this.schedule = schedule;
	}

	@Override
	public void handleEvent(TransitDriverStartsEvent event) {
		if (event.getVehicleId().toString().contains("rail")) {
			relevantIds.add(event.getVehicleId());
			isFirstArrivalEvent.add(event.getVehicleId());
		}
	}

	@Override
	public void handleEvent(VehicleArrivesAtFacilityEvent event) {
		if (relevantIds.contains(event.getVehicleId())) {
			Id<Link> linkId = schedule.getFacilities().get(event.getFacilityId()).getLinkId();

			if (!isFirstArrivalEvent.remove(event.getVehicleId())) {
				traversalDatabase.endTraversal(event.getTime(), event.getVehicleId(), linkId);
			}

			idleDatabase.startIdle(event.getTime(), event.getVehicleId(), linkId);
		}
	}

	@Override
	public void handleEvent(VehicleDepartsAtFacilityEvent event) {
		if (relevantIds.contains(event.getVehicleId())) {
			Id<Link> linkId = schedule.getFacilities().get(event.getFacilityId()).getLinkId();
			idleDatabase.endIdle(event.getTime() + 1, event.getVehicleId(), linkId);
			isDeparting.add(event.getVehicleId());
		}
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		if (relevantIds.contains(event.getVehicleId())) {
			traversalDatabase.startTraversal(event.getTime(), event.getVehicleId(), event.getLinkId());
		}
	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		if (relevantIds.contains(event.getVehicleId())) {
			if (!isDeparting.remove(event.getVehicleId())) {
				traversalDatabase.endTraversal(event.getTime(), event.getVehicleId(), event.getLinkId());
			}
		}
	}
}
