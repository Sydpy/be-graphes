package org.insa.algo.shortestpath;

import org.insa.algo.AbstractSolution.Status;
import org.insa.algo.utils.BinaryHeap;
import org.insa.algo.utils.Label;
import org.insa.algo.utils.PriorityQueue;
import org.insa.graph.Arc;
import org.insa.graph.Graph;
import org.insa.graph.Node;
import org.insa.graph.Path;
import org.insa.graph.Point;

public class AStarAlgorithm extends DijkstraAlgorithm {

    public AStarAlgorithm(ShortestPathData data) {
        super(data);
    }

    @Override
	protected double computeCost(double minCost, Arc a) {
		Point current = a.getDestination().getPoint();
		Point destination = ((ShortestPathData) data).getDestination().getPoint();
		
		double distance = Point.distance(current, destination);
		
		return minCost + data.getCost(a) + distance;
	}
}
