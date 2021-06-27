package org.matsim.drtviz.traversals;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdMap;
import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.Vehicle;

public class TraversalDatabase {
	private final IdMap<Vehicle, List<Traversal>> traversals = new IdMap<>(Vehicle.class);

	public void startTraversal(double startTime, Id<Vehicle> vehicleId, Id<Link> linkId) {
		synchronized (traversals) {			
			List<Traversal> vehicleTraversals = traversals.get(vehicleId);

			if (vehicleTraversals == null) {
				vehicleTraversals = new LinkedList<>();
				traversals.put(vehicleId, vehicleTraversals);
			}

			if (vehicleTraversals.size() > 0) {
				Traversal lastTraversal = vehicleTraversals.get(vehicleTraversals.size() - 1);

				if (!lastTraversal.isComplete()) {
					throw new IllegalStateException("Last traversal is not complete!");
				}

				if (lastTraversal.endTime > startTime) {
					throw new IllegalStateException("Cannot travel back in time!");
				}
			}

			vehicleTraversals.add(new Traversal(startTime, linkId));
		}
	}

	public void endTraversal(double endTime, Id<Vehicle> vehicleId, Id<Link> linkId) {
		synchronized (traversals) {			
			List<Traversal> vehicleTraversals = traversals.get(vehicleId);

			if (vehicleTraversals == null) {
				throw new IllegalStateException("Traversal as not been started");
			}

			Traversal lastTraversal = vehicleTraversals.get(vehicleTraversals.size() - 1);

			if (lastTraversal.isComplete()) {
				throw new IllegalStateException("Last traversal is already complete");
			}

			if (!lastTraversal.linkId.equals(linkId)) {
				throw new IllegalStateException("Wrong linkId for completing the traversal");
			}

			lastTraversal.endTime = endTime;
		}
	}

	public IdMap<Vehicle, LinkLocation> getLocations(double time) {
		IdMap<Vehicle, LinkLocation> result = new IdMap<>(Vehicle.class);

		synchronized (traversals) {
			for (Map.Entry<Id<Vehicle>, List<Traversal>> entry : traversals.entrySet()) {
				List<Traversal> vehicleTraversals = entry.getValue();

				if (vehicleTraversals.size() > 0) {
					for (Traversal traversal : vehicleTraversals) {
						if (traversal.startTime <= time && traversal.endTime > time) {
							double relativeLocation = (time - traversal.startTime)
									/ (traversal.endTime - traversal.startTime);
							result.put(entry.getKey(), new LinkLocation(traversal.linkId, relativeLocation));
							break;
						}
					}
				}
			}
		}

		return result;
	}

	public static class LinkLocation {
		final public Id<Link> linkId;
		final public double relativeLocation;

		LinkLocation(Id<Link> linkId, double relativeLocation) {
			this.linkId = linkId;
			this.relativeLocation = relativeLocation;
		}
	}

	private class Traversal {
		final double startTime;
		final Id<Link> linkId;

		double endTime = Double.POSITIVE_INFINITY;

		Traversal(double startTime, Id<Link> linkId) {
			this.startTime = startTime;
			this.linkId = linkId;
		}

		boolean isComplete() {
			return Double.isFinite(endTime);
		}
	}
}
