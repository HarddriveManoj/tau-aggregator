package com.mgk.tau.persist;

public interface Mapper<T> {

    public T convertFromString(String input) throws Exception;
    public String convertToString(T input) throws Exception;
}
