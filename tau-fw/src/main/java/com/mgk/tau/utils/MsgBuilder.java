package com.mgk.tau.utils;

import org.slf4j.helpers.MessageFormatter;

public final class MsgBuilder {

    private MsgBuilder() {

    }

    public static String format(String msgTemplate, Object... params) {
        return MessageFormatter.arrayFormat(msgTemplate, params).getMessage();
    }
}
