package org.insa.algo.utils;

import org.insa.graph.Node;

public class Label<F extends Comparable<F>> implements Comparable<Label<F>>{

    public Node node;
    public F cost;

    public Label(Node node, F cost) {
        this.node = node;
        this.cost = cost;
    }

    @Override
    public int compareTo(Label<F> efLabel) {
        return cost.compareTo(efLabel.cost);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        if (o instanceof Label) {
            Label l = (Label) o;
            return l.node.equals(node) && l.cost.equals(cost);
        }

        return false;
    }
}
