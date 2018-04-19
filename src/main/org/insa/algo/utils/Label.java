package org.insa.algo.utils;

public class Label<E, F extends Comparable<F>> implements Comparable<Label<E, F>>{

    public E data;
    public F label;

    public Label(E data, F label) {
        this.data = data;
        this.label = label;
    }

    @Override
    public int compareTo(Label<E, F> efLabel) {
        return label.compareTo(efLabel.label);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;

        if (o instanceof Label) {
            Label l = (Label) o;
            return l.data.equals(data) && l.label.equals(label);
        }

        return false;
    }
}
