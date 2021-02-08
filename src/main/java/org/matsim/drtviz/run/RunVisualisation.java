package org.matsim.drtviz.run;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.util.DrtEventsReaders;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.drtviz.assignments.AssignmentDatabase;
import org.matsim.drtviz.assignments.AssignmentDatabaseListener;
import org.matsim.drtviz.locations.LocationDatabase;
import org.matsim.drtviz.locations.LocationDatabaseListener;
import org.matsim.drtviz.network.NetworkItemProvider;
import org.matsim.drtviz.relocations.RelocationDatabase;
import org.matsim.drtviz.relocations.RelocationDatabaseListener;
import org.matsim.drtviz.requests.RequestDatabase;
import org.matsim.drtviz.requests.RequestDatabaseListener;
import org.matsim.drtviz.server.VisualisationRequest;
import org.matsim.drtviz.traversals.TraversalDatabase;
import org.matsim.drtviz.traversals.TraversalDatabaseListener;
import org.matsim.drtviz.visualisation.BoundsItem;
import org.matsim.drtviz.visualisation.DefaultVisualisationItemProvider;
import org.matsim.drtviz.visualisation.VisualisationItem;
import org.matsim.drtviz.visualisation.VisualisationItemProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class RunVisualisation {
	static public void main(String[] args) throws ConfigurationException {
		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("network-path", "events-path") //
				.build();

		Javalin app = Javalin.create(config -> {
			config.addStaticFiles("/public", Location.CLASSPATH);
			config.enableCorsForAllOrigins();
		}).start(9000);

		String networkPath = cmd.getOptionStrict("network-path");
		String eventsPath = cmd.getOptionStrict("events-path");

		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(networkPath);

		LocationDatabase locationDatabase = new LocationDatabase();
		TraversalDatabase traversalDatabase = new TraversalDatabase();
		RequestDatabase requestDatabase = new RequestDatabase();
		AssignmentDatabase assignmentDatabase = new AssignmentDatabase();
		RelocationDatabase relocationDatabase = new RelocationDatabase();

		VisualisationItemProvider visualisationProvider = new DefaultVisualisationItemProvider(traversalDatabase,
				requestDatabase, assignmentDatabase, relocationDatabase, network);

		NetworkItemProvider networkProvider = new NetworkItemProvider(network);

		app.post("/visualisation", ctx -> {
			VisualisationRequest request = new ObjectMapper().readValue(ctx.body(), VisualisationRequest.class);

			if (request.subject.equals("vehicles")) {
				VisualisationItem item = visualisationProvider.getVisualisation(request.time);
				ctx.json(item);
			} else if (request.subject.equals("bounds")) {
				double[] bounds = NetworkUtils.getBoundingBox(network.getNodes().values());

				BoundsItem item = new BoundsItem();
				item.minX = bounds[0];
				item.minY = bounds[1];
				item.maxX = bounds[2];
				item.maxY = bounds[3];

				ctx.json(item);
			} else if (request.subject.equals("network")) {
				ctx.json(networkProvider.getNetworkItem());
			} else {
				throw new IllegalStateException();
			}
		});

		EventsManager eventsManager = EventsUtils.createEventsManager();
		eventsManager.addHandler(new LocationDatabaseListener(locationDatabase));
		eventsManager.addHandler(new TraversalDatabaseListener(traversalDatabase));
		eventsManager.addHandler(new RequestDatabaseListener(requestDatabase));
		eventsManager.addHandler(new AssignmentDatabaseListener(assignmentDatabase));
		eventsManager.addHandler(new RelocationDatabaseListener(relocationDatabase));

		eventsManager.initProcessing();
		MatsimEventsReader reader = DrtEventsReaders.createEventsReader(eventsManager);
		reader.readFile(eventsPath);
		eventsManager.finishProcessing();
	}
}
