package com.mgk.tau.cartprod;

import java.util.HashMap;
import java.util.Map;

public class NTupleMapStrStr extends NTuple<Map<String, String>> {
    @Override
    Map<String, String> merge() {
        Map<String, String> ret = new HashMap<>();
        for(Map<String, String> m : elements) {
            ret.putAll(m);
        }
        return ret;
    }

    @Override
    NTuple<Map<String, String>> deepCopy() {
        NTuple<Map<String, String>> ret = new NTupleMapStrStr();
        for(Map<String, String> m : elements) {
            ret.add(new HashMap<String, String>(m));
        }
        return ret;
    }
}
