package org.insa.algo.shortestpath;

import org.insa.algo.AbstractInputData;
import org.insa.graph.Graph;
import org.insa.graph.Node;
import org.insa.graph.Point;

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
                return distance / (1000. * (double) getInputData().getMaximumSpeed() / 3600.);
        }

        return 0.;
    }
}
