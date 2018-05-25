package org.insa.algo.shortestpath;

import org.insa.graph.Node;

public class Label implements Comparable<Label> {

    private final Node node;
    private double cost;
    private boolean marked;

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

    public boolean isMarked() {
        return marked;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setMarked() {
        marked = true;
    }

}
