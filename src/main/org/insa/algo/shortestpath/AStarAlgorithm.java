package org.insa.algo.shortestpath;

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
        Arrays.fill(labels, null);
        labels[originId] = new LabelStar(data.getOrigin(), 0,0);

        return labels;
    }

    @Override
    protected Label computeLabelForDest(Arc a, Label originLabel) {

        LabelStar originLabelStar = (LabelStar) originLabel;

        double cost = data.getCost(a) + originLabel.getCost() - originLabelStar.getHeuristic();

        return new LabelStar(a.getDestination(),
                cost, computeHeuristic(a.getDestination()));
    }

    private double computeHeuristic(Node origin) {

        Point originPoint = origin.getPoint();
        Point destPoint = getInputData().getDestination().getPoint();

        if (originPoint == null || destPoint == null)
            return 0.;

        return originPoint.distanceTo(destPoint);
    }
}
