package org.matsim.drtviz.assignments;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdMap;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.vehicles.Vehicle;

public class AssignmentDatabase {
	private final IdMap<Request, Assignment> assignments = new IdMap<>(Request.class);

	public void startAssignment(Id<Request> requestId, Id<Vehicle> vehicleId, double time) {
		assignments.put(requestId, new Assignment(requestId, vehicleId, time));
	}

	public void finishAssignment(Id<Request> requestId, double time) {
		assignments.get(requestId).endTime = time;
	}

	public Collection<AssignmentState> getAssignments(double time) {
		List<AssignmentState> result = new LinkedList<>();

		for (Map.Entry<Id<Request>, Assignment> entry : assignments.entrySet()) {
			Assignment assignment = entry.getValue();

			if (assignment.startTime <= time) {
				if (assignment.endTime > time) {
					AssignmentState state = new AssignmentState();
					state.requestId = assignment.requestId;
					state.vehicleId = assignment.vehicleId;
					state.relativeLocation = (time - assignment.startTime)
							/ (assignment.endTime - assignment.startTime);
					result.add(state);
				}
			}
		}

		return result;
	}

	public class AssignmentState {
		public Id<Request> requestId;
		public Id<Vehicle> vehicleId;
		public double relativeLocation = 0.0;
	}

	private class Assignment {
		public final Id<Request> requestId;
		public final Id<Vehicle> vehicleId;

		public final double startTime;
		public double endTime = Double.POSITIVE_INFINITY;

		Assignment(Id<Request> requestId, Id<Vehicle> vehicleId, double startTime) {
			this.requestId = requestId;
			this.vehicleId = vehicleId;
			this.startTime = startTime;
		}
	}
}
