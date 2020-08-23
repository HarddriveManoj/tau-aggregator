package com.mgk.tau.web.auth;

import com.mgk.tau.web.harness.ITauWebTest;

public class NoopAuthenticator extends ProxylessAuthenticatorBase implements ITauWebTest.Authenticable {

    public NoopAuthenticator() {
        super();
    }
}
