package org.matsim.drtviz.locations;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdMap;
import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.Vehicle;

public class LocationDatabase {
	private final IdMap<Vehicle, List<Location>> locations = new IdMap<>(Vehicle.class);

	public void addLocation(double time, Id<Vehicle> vehicleId, Id<Link> linkId) {
		List<Location> vehicleLocations = locations.get(vehicleId);

		if (vehicleLocations == null) {
			vehicleLocations = new LinkedList<>();
			locations.put(vehicleId, vehicleLocations);
		}

		if (vehicleLocations.size() > 0) {
			Location lastLocation = vehicleLocations.get(vehicleLocations.size() - 1);

			if (lastLocation.linkId.equals(linkId)) {
				return; // Nothing to do as location has not changed ...
			}

			if (time < lastLocation.time) {
				throw new IllegalStateException("Cannot travel back in time.");
			}
		}

		vehicleLocations.add(new Location(time, linkId));
	}

	public IdMap<Vehicle, Id<Link>> getLocations(double time) {
		IdMap<Vehicle, Id<Link>> result = new IdMap<>(Vehicle.class);

		for (Map.Entry<Id<Vehicle>, List<Location>> entry : locations.entrySet()) {
			List<Location> vehicleLocations = entry.getValue();

			if (vehicleLocations.size() > 0) {
				Iterator<Location> iterator = vehicleLocations.iterator();
				Location closestLocation = null;

				while (iterator.hasNext()) {
					Location location = iterator.next();

					if (location.time > time) {
						break;
					}

					closestLocation = location;
				}

				if (closestLocation != null) {
					result.put(entry.getKey(), closestLocation.linkId);
				}
			}
		}

		return result;
	}

	private class Location {
		double time;
		Id<Link> linkId;

		Location(double time, Id<Link> linkId) {
			this.time = time;
			this.linkId = linkId;
		}
	}
}
