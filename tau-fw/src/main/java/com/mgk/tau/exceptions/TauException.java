package com.mgk.tau.exceptions;

public class TauException extends RuntimeException {

    public TauException() {

    }

    public TauException(String message) {
        super(message);
    }

    public TauException(Throwable cause) {
        super(cause);
    }

    public TauException(String message, Throwable cause) {
        super(message, cause);
    }
}
