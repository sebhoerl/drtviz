package org.matsim.drtviz.run;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.util.DrtEventsReaders;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.drtviz.TransitTraversalAndIdleListener;
import org.matsim.drtviz.assignments.AssignmentDatabase;
import org.matsim.drtviz.assignments.AssignmentDatabaseListener;
import org.matsim.drtviz.idle.IdleDatabase;
import org.matsim.drtviz.idle.IdleDatabaseListener;
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
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class RunVisualisation {
	static public void main(String[] args) throws ConfigurationException {
		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("network-path", "events-path") //
				.allowOptions("schedule-path") //
				.allowOptions("port") //
				.build();

		int port = cmd.getOption("port").map(Integer::parseInt).orElse(9000);

		Javalin app = Javalin.create(config -> {
			config.addStaticFiles("/public", Location.CLASSPATH);
			config.enableCorsForAllOrigins();
		}).start(port);

		String networkPath = cmd.getOptionStrict("network-path");
		String eventsPath = cmd.getOptionStrict("events-path");

		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(networkPath);

		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

		if (cmd.hasOption("schedule-path")) {
			new TransitScheduleReader(scenario).readFile(cmd.getOptionStrict("schedule-path"));
		}

		LocationDatabase locationDatabase = new LocationDatabase();
		TraversalDatabase traversalDatabase = new TraversalDatabase();
		RequestDatabase requestDatabase = new RequestDatabase();
		AssignmentDatabase assignmentDatabase = new AssignmentDatabase();
		RelocationDatabase relocationDatabase = new RelocationDatabase();
		IdleDatabase idleDatabase = new IdleDatabase();

		ProgressHandler progressHandler = new ProgressHandler();

		VisualisationItemProvider visualisationProvider = new DefaultVisualisationItemProvider(traversalDatabase,
				requestDatabase, assignmentDatabase, relocationDatabase, idleDatabase, network);

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

		app.get("/endpoint.json", ctx -> {
			// Means we access the current URL including the port.
			// If frontend is started by yarn, endpoint.json is delivered directly by the JS
			// server pointing to localhost:9000
			Map<String, String> result = new HashMap<>();
			result.put("endpoint", "");
			ctx.json(result);
		});

		app.get("/progress", ctx -> {
			ProgressItem item = new ProgressItem();
			item.time = progressHandler.getCurrentTime();
			item.finished = progressHandler.isFinished();
			ctx.json(item);
		});

		EventsManager eventsManager = EventsUtils.createEventsManager();
		eventsManager.addHandler(new LocationDatabaseListener(locationDatabase));
		eventsManager.addHandler(new TraversalDatabaseListener(traversalDatabase));
		eventsManager.addHandler(new RequestDatabaseListener(requestDatabase));
		eventsManager.addHandler(new AssignmentDatabaseListener(assignmentDatabase));
		eventsManager.addHandler(new RelocationDatabaseListener(relocationDatabase));
		eventsManager.addHandler(new IdleDatabaseListener(idleDatabase));
		eventsManager.addHandler(progressHandler);

		if (cmd.hasOption("schedule-path")) {
			eventsManager.addHandler(new TransitTraversalAndIdleListener(traversalDatabase, idleDatabase,
					scenario.getTransitSchedule()));

		}

		eventsManager.initProcessing();
		MatsimEventsReader reader = DrtEventsReaders.createEventsReader(eventsManager);
		reader.readFile(eventsPath);
		eventsManager.finishProcessing();
		progressHandler.finish();
	}
}
