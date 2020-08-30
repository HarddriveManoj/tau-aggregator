package com.mgk.tau.persist;

import org.apache.commons.lang3.StringUtils;

public class IntegerMapper implements Mapper<Integer> {
    @Override
    public Integer convertFromString(String input) throws Exception {
        return (StringUtils.isBlank(input) ? null : Integer.valueOf(input)) ;
    }

    @Override
    public String convertToString(Integer input) throws Exception {
        return input == null ? null : input.toString();
    }
}
