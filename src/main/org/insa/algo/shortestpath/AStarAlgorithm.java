package org.insa.algo.shortestpath;

import org.insa.algo.AbstractInputData;
import org.insa.algo.ArcInspector;
import org.insa.algo.utils.Label;
import org.insa.algo.utils.LabelStar;
import org.insa.graph.Arc;
import org.insa.graph.Graph;
import org.insa.graph.Node;
import org.insa.graph.Point;

import java.util.Arrays;

public class AStarAlgorithm extends DijkstraAlgorithm {

    public AStarAlgorithm(ShortestPathData data) {
        super(data);
    }

    @Override
    protected Label[] initLabels() {

        ShortestPathData data = getInputData();
        Graph graph = data.getGraph();
        int nbNodes = graph.size();
        int originId = data.getOrigin().getId();

        Label[] labels = new LabelStar[nbNodes];

        for (int i = 0; i < labels.length; i++) {
            Node n = graph.get(i);
            labels[i] = new LabelStar(n, Double.POSITIVE_INFINITY, computeHeuristic(n));
        }

        labels[originId].setCost(0);

        return labels;
    }

    private double computeHeuristic(Node node) {

        AbstractInputData.Mode mode = getInputData().getArcInspector().getMode();

        Point originPoint = node.getPoint();
        Point destPoint = getInputData().getDestination().getPoint();

        if (originPoint == null || destPoint == null)
            return 0.;

        double distance = originPoint.distanceTo(destPoint) * 9d / 10d;

        switch (mode) {
            case LENGTH:
                return distance;
            case TIME:
                return distance / (double) (1000 * getInputData().getMaximumSpeed());
        }

        return 0.;
    }
}
