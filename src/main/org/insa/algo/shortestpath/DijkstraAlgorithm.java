package org.insa.algo.shortestpath;

import org.insa.algo.AbstractSolution.Status;
import org.insa.algo.utils.BinaryHeap;
import org.insa.algo.utils.Label;
import org.insa.graph.Arc;
import org.insa.graph.Graph;
import org.insa.graph.Node;
import org.insa.graph.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DijkstraAlgorithm extends ShortestPathAlgorithm {

	public DijkstraAlgorithm(ShortestPathData data) {
		super(data);
	}

	protected Label[] initLabels() {

		ShortestPathData data = getInputData();
		Graph graph = data.getGraph();
		int nbNodes = graph.size();
		int originId = data.getOrigin().getId();

		Label[] labels = new Label[nbNodes];
		Arrays.fill(labels, null);
		labels[originId] = new Label<Double>(data.getOrigin(), 0.);

		return labels;
	}

	protected BinaryHeap initBinaryHeap(Label[] labels) {
		ShortestPathData data = getInputData();
		Graph graph = data.getGraph();

		assert labels.length == graph.size();

		int originId = data.getOrigin().getId();

		BinaryHeap queue = new BinaryHeap<Label<Double>>();
		queue.insert(labels[originId]);

		return queue;
	}

	protected Label computeLabelForDest(Arc a, Label originLabel) {
		return new Label<>(a.getDestination(), data.getCost(a) + (double) originLabel.cost);
	}

	@Override
	protected ShortestPathSolution doRun() {

		// Retrieve the graph.
		ShortestPathData data = getInputData();
		Graph graph = data.getGraph();

		final int nbNodes = graph.size();

		// Initialize array of predecessors.
		Arc[] predecessorArcs = new Arc[nbNodes];

		// Initialize array of distances.
		Label[] labels = initLabels();

		//Initialize PriorityQueue
		BinaryHeap queue = initBinaryHeap(labels);

		// Notify observers about the first event (origin processed).
		notifyOriginProcessed(data.getOrigin());

		//Get min of our queue
		Label min;
		Node minNode = null;

		//Do this while our queue is not empty or the destination is not the min
		while (!queue.isEmpty() && !data.getDestination().equals(minNode)) {

			//Extract min from priority queue
			min = (Label) queue.deleteMin();
			minNode = min.node;

			//For each arc from the min
			for (Arc a : minNode) {

				if (!data.isAllowed(a)) continue;

				Node destNode = a.getDestination();

				//Compute new distance from origin via minNode
				Label oldLabel = labels[destNode.getId()];
				Label newLabel = computeLabelForDest(a, labels[minNode.getId()]);

				if (oldLabel == null || newLabel.compareTo(oldLabel) < 0) {

					//If old label is null, it means we just
					//encountered this node, no need to remove it
					if (oldLabel == null) {
						notifyNodeReached(destNode);
					} else {
						queue.remove(oldLabel);
					}

					//Update distance and predecessor
					labels[destNode.getId()] = newLabel;
					predecessorArcs[destNode.getId()] = a;

					//(Re)Insert the cost concerning this arc with new distance
					queue.insert(newLabel);

				}
			}
		}

		ShortestPathSolution solution;

		// Destination has no predecessor, the solution is infeasible...
		if (predecessorArcs[data.getDestination().getId()] == null) {
			solution = new ShortestPathSolution(data, Status.INFEASIBLE);
		} else {

			// The destination has been found, notify the observers.
			notifyDestinationReached(data.getDestination());

			// Create the path from the array of predecessors...
			ArrayList<Arc> arcs = new ArrayList<>();
			Arc arc = predecessorArcs[data.getDestination().getId()];
			while (arc != null) {
				arcs.add(arc);
				arc = predecessorArcs[arc.getOrigin().getId()];
			}

			// Reverse the path...
			Collections.reverse(arcs);

			// Create the final solution.
			solution = new ShortestPathSolution(data, Status.OPTIMAL, new Path(graph, arcs));
		}

		return solution;
	}

}
