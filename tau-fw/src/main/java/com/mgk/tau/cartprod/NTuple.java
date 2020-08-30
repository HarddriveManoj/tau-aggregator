package com.mgk.tau.cartprod;

import java.util.ArrayList;
import java.util.List;

public abstract class NTuple<T> {
    protected List<T> elements = new ArrayList<>();

    abstract T merge();

    abstract NTuple<T> deepCopy();

    public void removeLast() {
        elements.remove(elements.size() - 1);
    }

    public void add(T element) {
        elements.add(element);
    }

    public void clear() {
        elements.clear();
    }
}
