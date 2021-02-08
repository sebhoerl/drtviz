package org.matsim.drtviz.visualisation;

import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdMap;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.drtviz.assignments.AssignmentDatabase;
import org.matsim.drtviz.assignments.AssignmentDatabase.AssignmentState;
import org.matsim.drtviz.relocations.RelocationDatabase;
import org.matsim.drtviz.relocations.RelocationDatabase.Relocation;
import org.matsim.drtviz.requests.RequestDatabase;
import org.matsim.drtviz.requests.RequestDatabase.RequestState;
import org.matsim.drtviz.traversals.TraversalDatabase;
import org.matsim.drtviz.traversals.TraversalDatabase.LinkLocation;
import org.matsim.vehicles.Vehicle;

public class DefaultVisualisationItemProvider implements VisualisationItemProvider {
	private final TraversalDatabase traversalDatabase;
	private final RequestDatabase requestDatabase;
	private final AssignmentDatabase assignmentDatabase;
	private final RelocationDatabase relocationDatabase;
	private final Network network;

	public DefaultVisualisationItemProvider(TraversalDatabase database, RequestDatabase requestDatabase,
			AssignmentDatabase assignmentDatabase, RelocationDatabase relocationDatabase, Network network) {
		this.assignmentDatabase = assignmentDatabase;
		this.traversalDatabase = database;
		this.requestDatabase = requestDatabase;
		this.relocationDatabase = relocationDatabase;
		this.network = network;
	}

	@Override
	public VisualisationItem getVisualisation(double time) {
		VisualisationItem visualisationItem = new VisualisationItem();

		IdMap<Vehicle, LinkLocation> vehicleLocations = traversalDatabase.getLocations(time);

		for (Map.Entry<Id<Vehicle>, LinkLocation> entry : vehicleLocations.entrySet()) {
			LinkLocation location = entry.getValue();
			Link link = network.getLinks().get(location.linkId);

			Coord startCoord = link.getFromNode().getCoord();
			Coord endCoord = link.getToNode().getCoord();
			Coord direction = CoordUtils.minus(endCoord, startCoord);

			VehicleItem vehicleItem = new VehicleItem();
			vehicleItem.id = entry.getKey().toString();
			vehicleItem.x = startCoord.getX() + location.relativeLocation * direction.getX();
			vehicleItem.y = startCoord.getY() + location.relativeLocation * direction.getY();

			visualisationItem.vehicles.add(vehicleItem);
		}

		for (RequestState state : requestDatabase.getActiveRequests(time)) {
			RequestItem requestItem = new RequestItem();

			Link originLink = network.getLinks().get(state.originLinkId);
			Link destinationLink = network.getLinks().get(state.destinationLinkId);

			requestItem.id = state.requestId.toString();
			requestItem.origin = new double[] { originLink.getCoord().getX(), originLink.getCoord().getY() };
			requestItem.destination = new double[] { destinationLink.getCoord().getX(),
					destinationLink.getCoord().getY() };
			requestItem.relativeLocation = state.relativeLocation;

			visualisationItem.requests.add(requestItem);
		}

		for (AssignmentState state : assignmentDatabase.getAssignments(time)) {
			AssignmentItem item = new AssignmentItem();
			item.requestId = state.requestId.toString();
			item.vehicleId = state.vehicleId.toString();
			visualisationItem.assignments.add(item);
		}

		for (Relocation relocation : relocationDatabase.getRelocations(time)) {
			Link link = network.getLinks().get(relocation.linkId);

			RelocationItem item = new RelocationItem();
			item.vehicleId = relocation.vehicleId.toString();
			item.destination = new double[] { link.getCoord().getX(), link.getCoord().getY() };
			visualisationItem.relocations.add(item);
		}

		return visualisationItem;
	}
}
