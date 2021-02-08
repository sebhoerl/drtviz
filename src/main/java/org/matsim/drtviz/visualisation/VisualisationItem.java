package org.matsim.drtviz.visualisation;

import java.util.LinkedList;
import java.util.List;

public class VisualisationItem {
	public List<VehicleItem> vehicles = new LinkedList<>();
	public List<RequestItem> requests = new LinkedList<>();
	public List<AssignmentItem> assignments = new LinkedList<>();
	public List<RelocationItem> relocations = new LinkedList<>();
}
