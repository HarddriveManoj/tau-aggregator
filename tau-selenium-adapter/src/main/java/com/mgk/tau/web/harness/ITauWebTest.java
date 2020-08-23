package com.mgk.tau.web.harness;

import com.mgk.tau.ConfigProvider;
import com.mgk.tau.TauTest;
import com.mgk.tau.exceptions.TauConfigException;
import com.mgk.tau.web.auth.NoopAuthenticator;
import com.mgk.tau.web.selenium.driver.managers.IDriverManager;
import org.openqa.selenium.Proxy;


public interface ITauWebTest extends TauTest {

    IDriverManager getDriverManager();

    Authenticable getAuthenticable();

    interface Authenticable {
        boolean needsProxy();

        void startAuth();

        void stopAuth();

        void configureProxy();

        Proxy getAuthProxy();

        class Factory {
            public static final String TYPE = "tau.auth";

            public static Authenticable create(ConfigProvider configProvider) {
                String chainedProxy = configProvider.getOptional(IDriverManager.Factory.PROPERTY_PROXY);

                String authType = configProvider.getRequired(TYPE);

                if (authType.equalsIgnoreCase(TYPES.noauth.name()) || authType.equalsIgnoreCase("none") || authType.equalsIgnoreCase("noop")) {
                    return new NoopAuthenticator();
                } else {
                    throw new TauConfigException("Incorrect value for " + TYPE + ":" + authType, new String[]{"noauth|none|noop", "ie|internetexplorer", "pkcs", "pem", "basic", "krb"});
                }
            }

            public enum TYPES {
                noauth, none, ie, pkcs, pem, basic, krb
            }
        }
    }

}
