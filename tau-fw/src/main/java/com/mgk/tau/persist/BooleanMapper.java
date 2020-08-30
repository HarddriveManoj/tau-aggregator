package com.mgk.tau.persist;

import org.apache.commons.lang3.StringUtils;

public class BooleanMapper implements Mapper<Boolean> {
    @Override
    public Boolean convertFromString(String input) throws Exception {
        return (StringUtils.isBlank(input) ? null : Boolean.valueOf(input)) ;
    }

    @Override
    public String convertToString(Boolean input) throws Exception {
        return input == null ? null : input.toString();
    }
}
