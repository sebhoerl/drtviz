package org.matsim.drtviz.requests;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdMap;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.optimizer.Request;

public class RequestDatabase {
	private final IdMap<Request, RequestInfo> requests = new IdMap<>(Request.class);

	public void addRequest(Id<Request> requestId, double submissionTime, Id<Link> originLinkId,
			Id<Link> destinationLinkId) {
		synchronized (requests) {
			requests.put(requestId, new RequestInfo(submissionTime, originLinkId, destinationLinkId));
		}
	}

	public void pickupRequest(Id<Request> requestId, double pickupTime) {
		synchronized (requests) {
			RequestInfo request = requests.get(requestId);

			if (request == null) {
				throw new IllegalStateException("Request does not exist");
			}

			request.pickupTime = pickupTime;
		}
	}

	public void dropoffRequest(Id<Request> requestId, double dropoffTime) {
		synchronized (requests) {
			RequestInfo request = requests.get(requestId);

			if (request == null) {
				throw new IllegalStateException("Request does not exist");
			}

			request.dropoffTime = dropoffTime;
		}
	}

	public void rejectRequest(Id<Request> requestId, double rejectionTime) {
		synchronized (requests) {
			RequestInfo request = requests.get(requestId);

			if (request == null) {
				throw new IllegalStateException("Request does not exist: " + requestId);
			}

			request.rejectionTime = rejectionTime;
		}
	}

	public Collection<RequestState> getActiveRequests(double time) {
		List<RequestState> result = new LinkedList<>();

		synchronized (requests) {
			for (Map.Entry<Id<Request>, RequestInfo> entry : requests.entrySet()) {
				RequestInfo request = entry.getValue();

				if (request.submissionTime <= time) {
					if (request.rejectionTime > time && request.dropoffTime > time) {
						RequestState state = new RequestState();
						state.requestId = entry.getKey();
						state.originLinkId = request.originLinkId;
						state.destinationLinkId = request.destinationLinkId;
						state.waitingTime = time - request.submissionTime;

						if (Double.isFinite(request.dropoffTime)) {
							state.relativeLocation = Math
									.max((time - request.pickupTime) / (request.dropoffTime - request.pickupTime), 0.0);
						}

						result.add(state);
					}
				}
			}
		}

		return result;
	}

	public class RequestState {
		public Id<Request> requestId;
		public Id<Link> originLinkId;
		public Id<Link> destinationLinkId;
		public double waitingTime;
		public double relativeLocation;
	}

	private class RequestInfo {
		final double submissionTime;
		final Id<Link> originLinkId;
		final Id<Link> destinationLinkId;

		double rejectionTime = Double.POSITIVE_INFINITY;
		double pickupTime = Double.POSITIVE_INFINITY;
		double dropoffTime = Double.POSITIVE_INFINITY;

		RequestInfo(double submissionTime, Id<Link> originLinkId, Id<Link> destinationLinkId) {
			this.submissionTime = submissionTime;
			this.originLinkId = originLinkId;
			this.destinationLinkId = destinationLinkId;
		}
	}
}
