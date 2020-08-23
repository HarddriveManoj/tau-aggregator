package com.mgk.tau.web.auth;

import com.mgk.tau.web.harness.ITauWebTest;
import org.openqa.selenium.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AuthenticatorBase implements ITauWebTest.Authenticable {
    private static final Logger log = LoggerFactory.getLogger(AuthenticatorBase.class);
    protected final String chainedProxy;
    protected List<Proxy> authProxies = new ArrayList<Proxy>();

    protected AuthenticatorBase(String chainedProxy) {
        log.info("Using " + this.getClass().getName());
        this.chainedProxy = chainedProxy;
    }


    public void startAuth() {
        if(needsProxy()) {
            configureProxy();
        }
    }

    public void stopAuth() {

    }

    public Proxy getAuthProxy() {
        return authProxies.get(0);
    }
 }
