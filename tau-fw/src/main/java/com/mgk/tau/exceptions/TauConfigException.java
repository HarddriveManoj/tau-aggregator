package com.mgk.tau.exceptions;

import java.util.Arrays;

public class TauConfigException extends TauException {

    private String[] allowedValues;

    public TauConfigException(String[] allowedValues) {
        super();
        this.allowedValues = allowedValues;
    }

    public TauConfigException(String message) {
        super(message);
    }

    public TauConfigException(Throwable cause) {
        super(cause);
    }

    public TauConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public TauConfigException(String message, String[] allowedValues) {
        super(message);
        this.allowedValues = allowedValues;
    }

    public String getMessage() {
        if (allowedValues != null) {
            return super.getMessage() + ", allowed values = " + Arrays.asList(allowedValues);
        } else {
            return super.getMessage();
        }
    }

}
