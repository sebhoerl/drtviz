package org.matsim.drtviz.relocations;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.Vehicle;

public class RelocationDatabase {
	private final List<Relocation> relocations = new LinkedList<>();

	public void addRelocation(double startTime, double endTime, Id<Vehicle> vehicleId, Id<Link> linkId) {
		synchronized (relocations) {
			relocations.add(new Relocation(startTime, endTime, vehicleId, linkId));
		}
	}

	public Collection<Relocation> getRelocations(double time) {
		List<Relocation> result = new LinkedList<>();

		synchronized (relocations) {
			for (Relocation relocation : relocations) {
				if (relocation.startTime <= time) {
					if (Double.isFinite(relocation.endTime) && relocation.endTime > time) {
						result.add(relocation);
					}
				}
			}
		}

		return result;
	}

	public class Relocation {
		final public double startTime;
		final public double endTime;

		final public Id<Vehicle> vehicleId;
		final public Id<Link> linkId;

		Relocation(double startTime, double endTime, Id<Vehicle> vehicleId, Id<Link> linkId) {
			this.startTime = startTime;
			this.endTime = endTime;
			this.linkId = linkId;
			this.vehicleId = vehicleId;
		}
	}
}
