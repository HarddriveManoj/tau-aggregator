package com.mgk.tau.exceptions;

import org.testng.SkipException;

public class TauTestDataException extends TauException {

    public TauTestDataException(String message) {
        super(message, new SkipException(message));
    }
}
