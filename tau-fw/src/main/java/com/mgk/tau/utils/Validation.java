package com.mgk.tau.utils;

import com.mgk.tau.exceptions.TauTestDataException;
import com.mgk.tau.exceptions.TauVerificationException;
import org.slf4j.helpers.MessageFormatter;

public class Validation {

    public static void assertTrue(boolean condition, String msgLogbackTemplate, Object... params) {
        if(!condition) {
            String msg = MessageFormatter.arrayFormat(msgLogbackTemplate, params).getMessage();
            throw new TauVerificationException(msg);
        }
    }

    public static void assertTrueData(boolean condition, String msgLogbackTemplate, Object... params) {
        if(!condition) {
            throw new TauTestDataException(msgLogbackTemplate);
        }
    }
}
