package org.insa.algo.utils;

public class Label<E, F extends Comparable<F>> implements Comparable<Label<E, F>>{

    public E value;
    public F label;

    public Label(E value, F label) {
        this.value = value;
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
            return l.value.equals(value) && l.label.equals(label);
        }

        return false;
    }
}
