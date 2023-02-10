package org.matsim.drtviz.requests;

import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdMap;
import org.matsim.api.core.v01.IdSet;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerDroppedOffEvent;
import org.matsim.contrib.dvrp.passenger.PassengerDroppedOffEventHandler;
import org.matsim.contrib.dvrp.passenger.PassengerPickedUpEvent;
import org.matsim.contrib.dvrp.passenger.PassengerPickedUpEventHandler;
import org.matsim.contrib.dvrp.passenger.PassengerRequestRejectedEvent;
import org.matsim.contrib.dvrp.passenger.PassengerRequestRejectedEventHandler;
import org.matsim.contrib.dvrp.passenger.PassengerRequestSubmittedEvent;
import org.matsim.contrib.dvrp.passenger.PassengerRequestSubmittedEventHandler;

public class RequestDatabaseListener implements PassengerRequestSubmittedEventHandler, PersonDepartureEventHandler,
		PassengerRequestRejectedEventHandler, PassengerPickedUpEventHandler, PassengerDroppedOffEventHandler {
	private final RequestDatabase database;

	private final IdMap<Person, List<Id<Request>>> personRequests = new IdMap<>(Person.class);
	private final IdSet<Person> departedPersons = new IdSet<>(Person.class);

	public RequestDatabaseListener(RequestDatabase database) {
		this.database = database;
	}

	@Override
	public void handleEvent(PassengerRequestSubmittedEvent event) {
		database.addRequest(event.getRequestId(), event.getTime(), event.getFromLinkId(), event.getToLinkId());

		if (departedPersons.remove(event.getPersonId())) {
			database.departRequest(event.getRequestId(), event.getTime());
		} else {
			personRequests.computeIfAbsent(event.getPersonId(), id -> new LinkedList<>()).add(event.getRequestId());
		}
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if (event.getLegMode().equals("drt")) {
			List<Id<Request>> requests = personRequests.get(event.getPersonId());

			if (requests != null && requests.size() > 0) {
				database.departRequest(requests.remove(0), event.getTime());
			} else {
				departedPersons.add(event.getPersonId());
			}
		}
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
