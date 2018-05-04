package org.insa.algo.utils;

public class Label<E, F extends Comparable<F>> implements Comparable<Label<E, F>>{

    public E data;
    public F cost;

    public Label(E data, F label) {
        this.data = data;
        this.cost = label;
    }

    @Override
    public int compareTo(Label<E, F> efLabel) {
        return cost.compareTo(efLabel.cost);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        if (o instanceof Label) {
            Label l = (Label) o;
            return l.data.equals(data) && l.cost.equals(cost);
        }

        return false;
    }
}
