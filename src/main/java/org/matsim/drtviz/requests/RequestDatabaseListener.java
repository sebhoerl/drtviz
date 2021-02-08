package org.matsim.drtviz.requests;

import org.matsim.contrib.dvrp.passenger.PassengerDroppedOffEvent;
import org.matsim.contrib.dvrp.passenger.PassengerDroppedOffEventHandler;
import org.matsim.contrib.dvrp.passenger.PassengerPickedUpEvent;
import org.matsim.contrib.dvrp.passenger.PassengerPickedUpEventHandler;
import org.matsim.contrib.dvrp.passenger.PassengerRequestRejectedEvent;
import org.matsim.contrib.dvrp.passenger.PassengerRequestRejectedEventHandler;
import org.matsim.contrib.dvrp.passenger.PassengerRequestSubmittedEvent;
import org.matsim.contrib.dvrp.passenger.PassengerRequestSubmittedEventHandler;

public class RequestDatabaseListener implements PassengerRequestSubmittedEventHandler,
		PassengerRequestRejectedEventHandler, PassengerPickedUpEventHandler, PassengerDroppedOffEventHandler {
	private final RequestDatabase database;

	public RequestDatabaseListener(RequestDatabase database) {
		this.database = database;
	}

	@Override
	public void handleEvent(PassengerRequestSubmittedEvent event) {
		database.addRequest(event.getRequestId(), event.getTime(), event.getFromLinkId(), event.getToLinkId());
	}

	@Override
	public void handleEvent(PassengerRequestRejectedEvent event) {
		database.rejectRequest(event.getRequestId(), event.getTime());
	}

	@Override
	public void handleEvent(PassengerDroppedOffEvent event) {
		database.dropoffRequest(event.getRequestId(), event.getTime());
	}

	@Override
	public void handleEvent(PassengerPickedUpEvent event) {
		database.pickupRequest(event.getRequestId(), event.getTime());
	}
}
