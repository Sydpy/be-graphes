package org.insa.algo.utils;

import org.insa.graph.Node;

public class Label implements Comparable<Label>{

    private final Node node;
    private final double cost;

    public Label(Node node, double cost) {
        this.node = node;
        this.cost = cost;
    }

    @Override
    public int compareTo(Label label) {
        return Double.compare(cost, label.cost);
    }

    public Node getNode() {
        return node;
    }

    public double getCost() {
        return cost;
    }
}
