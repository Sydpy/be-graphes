package org.insa.algo.utils;

import org.insa.graph.Node;

public class LabelStar extends Label {

    private final double heuristic;

    public LabelStar(Node node, double cost, double heuristic) {
        super(node, cost);
        this.heuristic = heuristic;
    }

    public double getHeuristic() {
        return heuristic;
    }

    @Override
    public double getCost() {
        return super.getCost() + heuristic;
    }

    @Override
    public int compareTo(Label label) {
        int comp = super.compareTo(label);

        if (comp == 0 && label instanceof LabelStar) {
            return Double.compare(heuristic, ((LabelStar) label).heuristic);
        }

        return comp;
    }

}
