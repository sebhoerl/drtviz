package org.matsim.drtviz.network;

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
			networkItem.links.add(linkItem);
		}

		return networkItem;
	}
}
