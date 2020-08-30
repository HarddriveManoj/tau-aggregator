package com.mgk.tau.persist;

import org.apache.commons.lang3.StringUtils;

public class LongMapper implements Mapper<Long> {
    @Override
    public Long convertFromString(String input) throws Exception {
        return (StringUtils.isBlank(input) ? null : Long.valueOf(input)) ;
    }

    @Override
    public String convertToString(Long input) throws Exception {
        return input == null ? null : input.toString();
    }
}
