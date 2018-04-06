package org.insa.algo.utils;

public class Label<E, F extends Comparable<F>> implements Comparable<Label<E, F>>{

    public E value;
    public F label;

    @Override
    public int compareTo(Label<E, F> efLabel) {
        return label.compareTo(efLabel.label);
    }
}
