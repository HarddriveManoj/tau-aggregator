package com.mgk.tau.web.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxylessAuthenticatorBase extends AuthenticatorBase {

    private static final Logger log = LoggerFactory.getLogger(ProxyingAuthenticatorBase.class);

    public ProxylessAuthenticatorBase() {
        super(null);
    }


    public boolean needsProxy() {
        return false;
    }


    public void configureProxy() {

    }

    public String modifyURL(String url) {
        log.info("Not modifying URL" +url);
        return url;
    }
}
