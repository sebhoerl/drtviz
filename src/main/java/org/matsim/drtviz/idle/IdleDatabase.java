package org.matsim.drtviz.idle;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdMap;
import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.Vehicle;

public class IdleDatabase {
	private final IdMap<Vehicle, List<Idle>> idles = new IdMap<>(Vehicle.class);

	public void startIdle(double startTime, Id<Vehicle> vehicleId, Id<Link> linkId) {
		synchronized (idles) {
			List<Idle> vehicleIdles = idles.get(vehicleId);

			if (vehicleIdles == null) {
				vehicleIdles = new LinkedList<>();
				idles.put(vehicleId, vehicleIdles);
			}

			if (vehicleIdles.size() > 0) {
				Idle lastIdle = vehicleIdles.get(vehicleIdles.size() - 1);

				if (!lastIdle.isComplete()) {
					throw new IllegalStateException("Last idle is not complete!");
				}

				if (lastIdle.endTime > startTime) {
					throw new IllegalStateException("Cannot travel back in time!");
				}
			}

			vehicleIdles.add(new Idle(startTime, linkId));
		}
	}

	public void endIdle(double endTime, Id<Vehicle> vehicleId, Id<Link> linkId) {
		synchronized (idles) {
			List<Idle> vehicleIdles = idles.get(vehicleId);

			if (vehicleIdles == null) {
				throw new IllegalStateException("Idle as not been started");
			}

			Idle lastIdle = vehicleIdles.get(vehicleIdles.size() - 1);

			if (lastIdle.isComplete()) {
				throw new IllegalStateException("Last idle is already complete");
			}

			if (!lastIdle.linkId.equals(linkId)) {
				throw new IllegalStateException("Wrong linkId for completing the idle");
			}

			lastIdle.endTime = endTime;
		}
	}

	public IdMap<Vehicle, Id<Link>> getLocations(double time) {
		IdMap<Vehicle, Id<Link>> result = new IdMap<>(Vehicle.class);

		synchronized (idles) {
			for (Map.Entry<Id<Vehicle>, List<Idle>> entry : idles.entrySet()) {
				List<Idle> vehicleIdles = entry.getValue();

				if (vehicleIdles.size() > 0) {
					for (Idle idle : vehicleIdles) {
						if (idle.startTime <= time && idle.endTime > time) {
							result.put(entry.getKey(), idle.linkId);
							break;
						}
					}
				}
			}
		}

		return result;
	}

	public class Idle {
		final double startTime;
		final Id<Link> linkId;

		double endTime = Double.POSITIVE_INFINITY;

		Idle(double startTime, Id<Link> linkId) {
			this.startTime = startTime;
			this.linkId = linkId;
		}

		boolean isComplete() {
			return Double.isFinite(endTime);
		}
	}
}
