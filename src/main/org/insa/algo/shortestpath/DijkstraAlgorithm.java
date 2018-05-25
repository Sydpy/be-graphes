package org.insa.algo.shortestpath;

import org.insa.algo.AbstractSolution.Status;
import org.insa.algo.utils.BinaryHeap;
import org.insa.graph.Arc;
import org.insa.graph.Graph;
import org.insa.graph.Node;
import org.insa.graph.Path;

import java.util.ArrayList;
import java.util.Collections;

public class DijkstraAlgorithm extends ShortestPathAlgorithm {

	private Arc[] predecessorArcs;
	private Label[] labels;
	private BinaryHeap<Label> queue;

	public DijkstraAlgorithm(ShortestPathData data) {
		super(data);

		Graph graph = data.getGraph();
		int graphSize = graph.size();

		// Initialize array of predecessors.
		this.predecessorArcs = new Arc[graphSize];

		// Initialize array of distances.
		this.labels = initLabels();

		//Initialize PriorityQueue
		int originId = data.getOrigin().getId();
		this.queue = new BinaryHeap<>();
		this.queue.insert(labels[originId]);
	}

	protected Label[] initLabels() {

		ShortestPathData data = getInputData();
		Graph graph = data.getGraph();
		int nbNodes = graph.size();
		int originId = data.getOrigin().getId();

		Label[] labels = new Label[nbNodes];

		for (int i = 0; i < labels.length; i++) {
			Node n = graph.get(i);
			labels[i] = new Label(n, Double.POSITIVE_INFINITY);
		}

		labels[originId].setCost(0);

		return labels;
	}

	@Override
	protected ShortestPathSolution doRun() {

		// Retrieve the graph.
		ShortestPathData data = getInputData();
		Graph graph = data.getGraph();

		// Notify observers about the first event (origin processed).
		notifyOriginProcessed(data.getOrigin());

		//Get min of our queue
		Node minNode;

		do {
			minNode = step();
		} while(minNode != null && !minNode.equals(data.getDestination()));

		ShortestPathSolution solution;

		Path path;
		// Destination has no predecessor, the solution is infeasible...
		if ((path = getPathTo(data.getDestination())) == null) {
			solution = new ShortestPathSolution(data, Status.INFEASIBLE);
		} else {

			notifyDestinationReached(data.getDestination());

			// Create the final solution.
			solution = new ShortestPathSolution(data, Status.OPTIMAL, path);
		}

		return solution;
	}

	public Node step() {

		//Do this while our queue is not empty or the destination is not the min
		if (!queue.isEmpty()) {

			//Extract min from priority queue
			Label min = queue.deleteMin();
			Node minNode = min.getNode();
			min.setMarked();

			notifyNodeMarked(minNode);

			//For each arc from the min
			for (Arc a : minNode) {

				if (!data.isAllowed(a)) continue;

				Node destNode = a.getDestination();
				Label oldLabel = labels[destNode.getId()];

				if (oldLabel.isMarked()) continue;

				//Compute new distance from origin via minNode
				double newCost = min.getCost() + data.getCost(a);

				if (Double.compare(oldLabel.getCost(), newCost) >= 0) {

					//If old label cost is infinite, it means we just
					//encountered this node, no need to remove it
					if (Double.isInfinite(oldLabel.getCost())) {
						notifyNodeReached(destNode);
					} else {
						queue.remove(oldLabel);
					}

					oldLabel.setCost(newCost);

					//Update distance and predecessor
					predecessorArcs[destNode.getId()] = a;

					queue.insert(oldLabel);
				}
			}

			return minNode;
		}

		return null;
	}

	public double getMinCost() {
		return queue.isEmpty() ? Double.POSITIVE_INFINITY : queue.findMin().getCost();
	}

	public Label[] getLabels() {
		return labels;
	}

	public Arc[] getPredecessorArcs() {
		return predecessorArcs;
	}

	public Path getPathTo(Node dest) {

		Arc arc = predecessorArcs[dest.getId()];

		//If the algorithm didn't reach the destination, we return null
		if (arc == null) return null;

		// Create the path from the array of predecessors...
		ArrayList<Arc> arcs = new ArrayList<>();

		while (arc != null) {
			arcs.add(arc);
			arc = predecessorArcs[arc.getOrigin().getId()];
		}

		// Reverse the path...
		Collections.reverse(arcs);

		return new Path(data.getGraph(), arcs);
	}
}
