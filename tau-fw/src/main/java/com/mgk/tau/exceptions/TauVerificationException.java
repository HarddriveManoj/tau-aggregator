package com.mgk.tau.exceptions;

import com.mgk.tau.utils.MsgBuilder;

public class TauVerificationException extends TauException {

    private String expected;
    private String actual;

    public TauVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TauVerificationException(String message) {
        super(message);
    }

    public TauVerificationException(Throwable cause) {
        super(cause);
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public TauVerificationException expected(String expected) {
        this.expected = expected;
        return this;
    }

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }


    public TauVerificationException actual(String actual) {
        this.actual = actual;
        return this;
    }

    @Override
    public String getMessage() {
        if (actual != null && expected != null) {
            String error = MsgBuilder.format("Found {} while expecting to find {} ", actual, expected);
            return error + " , " + super.getMessage();
        } else {
            return super.getMessage();
        }
    }
}
