package org.insa.algo.shortestpath;

import org.insa.graph.Node;

public class LabelStar extends Label {

    private final double heuristic;

    public LabelStar(Node node, double cost, double heuristic) {
        super(node, cost);
        this.heuristic = heuristic;
    }

    private double getHeuristic() {
        return heuristic;
    }

    private double getTotalCost() {
        return getCost() + getHeuristic();
    }

    @Override
    public int compareTo(Label label) {

        if (label instanceof LabelStar) {
            LabelStar labelStar = (LabelStar) label;

            int comp = Double.compare(getTotalCost(), labelStar.getTotalCost());

            if (comp == 0) {
                comp = Double.compare(getHeuristic(), labelStar.getHeuristic());
            }

            return comp;
        }

        return super.compareTo(label);
    }
}
