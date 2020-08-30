package com.mgk.tau.cartprod;

import java.util.ArrayList;
import java.util.List;

public class CartProd<T extends NTuple, K> {
    private int setIndex;
    private T nTuple;
    private Class<T> tupleClass;
    private int[] pos;

    public CartProd(Class<T> tupleClass) throws IllegalAccessException, InstantiationException {
        nTuple = tupleClass.newInstance();
    }

    public List<K> cartProd(List<List<K>> sets) {
        List<K> product = new ArrayList<K>();
        if(sets == null || sets.isEmpty()) {
            return product;
        }

        clear(sets.size());

        while (isNotEos(sets) || isNotFirstSet()) {
            if(isEOS(sets) && isNotFirstSet()) {
                pos[setIndex] = 0;
                setIndex--;
                nTuple.removeLast();
                continue;
            }

            nTuple.add(sets.get(setIndex).get(pos[setIndex]));
            pos[setIndex]++;

            if(isLastSet(sets)) {
                product.add((K) nTuple.deepCopy().merge());
                nTuple.removeLast();
            } else {
                setIndex++;
            }
        }
        return product;
    }

    private void clear(int size) {
        setIndex = 0;
        nTuple.clear();
        pos = new int[size];
    }

    private boolean isEOS(List<List<K>> sets) {
        return sets.get(setIndex) == null || pos[setIndex] >= sets.get(setIndex).size();
    }

    private boolean isNotEos(List<List<K>> sets) {
        return sets.get(setIndex) != null && pos[setIndex] < sets.get(setIndex).size();
    }

    private boolean isLastSet(List<List<K>> sets) {
        return setIndex >= (sets.size() -1);
    }

    private boolean isNotFirstSet() {
        return setIndex != 0;
    }
}
