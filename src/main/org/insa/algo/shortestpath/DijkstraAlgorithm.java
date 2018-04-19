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
	
	protected double computeCost(double minCost, Arc a) {
		return minCost+ data.getCost(a);
	}
	
	@Override
	protected ShortestPathSolution doRun() {

		// Retrieve the graph.
		ShortestPathData data = getInputData();
		Graph graph = data.getGraph();

		final int nbNodes = graph.size();

		// Notify observers about the first event (origin processed).
		notifyOriginProcessed(data.getOrigin());

		// Initialize array of distances.
		double[] distances = new double[nbNodes];
		Arrays.fill(distances, Double.POSITIVE_INFINITY);
		distances[data.getOrigin().getId()] = 0;

		// Initialize array of predecessors.
		Arc[] predecessorArcs = new Arc[nbNodes];

		//Initialize PriorityQueue
		BinaryHeap<Label<Node, Double>> queue = new BinaryHeap<>();
		queue.insert(new Label<>(data.getOrigin(), distances[data.getOrigin().getId()]));

		//Get min of our queue
		Label<Node, Double> min;
		double minDist;
		Node minNode = null;

		//Do this while our queue is not empty or the destination is not the min
		while (!queue.isEmpty() && !data.getDestination().equals(minNode)) {

			//Extract min from priority queue
			min = queue.deleteMin();
			minNode = min.data;
			minDist = min.label;

			//For each arc from the min
			for (Arc a : minNode) {

				if (!data.isAllowed(a)) continue;

				Node dest = a.getDestination();

				//Compute new distance from origin via minNode
				double oldDist = distances[dest.getId()];
				double newDist = computeCost(minDist, a);

				if (newDist < oldDist) {

					//If old distance is Infinite, it means we just
					//encountered this node, no need to remove it
					if (Double.isInfinite(oldDist)) {
						notifyNodeReached(dest);
					} else {
						queue.remove(new Label<>(dest, oldDist));
					}

					//Update distance and predecessor
					distances[dest.getId()] = newDist;
					predecessorArcs[dest.getId()] = a;

					//(Re)Insert the label concerning this arc with new distance
					queue.insert(new Label<>(dest, newDist));
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
