package com.mgk.tau.web.auth;

import com.mgk.tau.ConfigProvider;
import com.mgk.tau.web.selenium.driver.managers.IDriverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProxyingAuthenticatorBase extends AuthenticatorBase {
    private static final Logger log = LoggerFactory.getLogger(ProxyingAuthenticatorBase.class);
    protected String proxyMapping;

    protected ProxyingAuthenticatorBase(String chainedProxy) {
        super(chainedProxy);
    }
    protected ProxyingAuthenticatorBase(ConfigProvider configProvider, String chainedProxy) {
        super(chainedProxy);
        proxyMapping = configProvider.getOptional(IDriverManager.Factory.PROPERTY_PROXY);
    }


    public boolean needsProxy() {
        return true;
    }


}
