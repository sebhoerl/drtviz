package org.matsim.drtviz.network;

import java.util.Optional;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

public class NetworkItemProvider {
	private final Network network;

	public NetworkItemProvider(Network network) {
		this.network = network;
	}

	public NetworkItem getNetworkItem() {
		NetworkItem networkItem = new NetworkItem();

		for (Link link : network.getLinks().values()) {
			LinkItem linkItem = new LinkItem();
			linkItem.from = new double[] { link.getFromNode().getCoord().getX(), link.getFromNode().getCoord().getY() };
			linkItem.to = new double[] { link.getToNode().getCoord().getX(), link.getToNode().getCoord().getY() };
			linkItem.isOneway = Optional.ofNullable((Boolean) link.getAttributes().getAttribute("oneway"))
					.orElse(false);
			linkItem.isTerminus = Optional.ofNullable((Boolean) link.getAttributes().getAttribute("is_terminus"))
					.orElse(false);
			linkItem.isSms = link.getId().toString().startsWith("sms_");
			linkItem.isTracks = link.getId().toString().contains("tracks");
			linkItem.isSms |= linkItem.isTracks;
			networkItem.links.add(linkItem);
		}

		return networkItem;
	}
}
