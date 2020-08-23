package com.mgk.tau.web.selenium.driver.managers;

import com.mgk.tau.exceptions.TauConfigException;
import com.mgk.tau.web.auth.ClientCertificateBasedAuthenticator;
import com.mgk.tau.web.harness.ITauWebTest;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mgk.tau.ConfigProvider;

import java.util.List;

public interface IDriverManager {
    public DesiredCapabilities getDesiredCapabilities();

    static class Factory {
        private static final Logger log = LoggerFactory.getLogger(Factory.class);

        public static final String PROPERTY_NAME = "tau.browser";
        public static final String PROPERTY_PROXY = "tau.browser.proxy";

        private static final String PROPERTY_PROXY_AUTO_CONFIG_URL = "tau.browser.proxy.auto-config-url";
        private static final String PROPERTY_PROXY_MAPPING = "tau.browser.proxy.mapping";

        public static final String TAUCLOUD_ROUTER_URL = "taucloud.router.url";
        public static final String TAUCLOUD_BROWSER_VERSION = "taucloud.router.browser.version";
        public static final String TAUCLOUD_NODE_PLATFORM = "taucloud.router.platform";

        public static IDriverManager create(ConfigProvider configProvider, ITauWebTest.Authenticable auth) {
            return create(configProvider, auth);
        }

        public static IDriverManager create(ConfigProvider configProvider, DesiredCapabilities desiredCapabilities, ITauWebTest.Authenticable auth) {
            DesiredCapabilities dc = null;
            String browserType = configProvider.getRequired(PROPERTY_NAME);
            if(browserType.equalsIgnoreCase("chrome")) {
                dc = DesiredCapabilities.chrome();
            } else if(browserType.equalsIgnoreCase("ie") || browserType.equalsIgnoreCase("internetexplorer")) {
                dc = DesiredCapabilities.internetExplorer();
            } else if(browserType.equals("edge")) {
                dc = DesiredCapabilities.edge();
            } else {
                throw new TauConfigException("Incorrect value for " + PROPERTY_NAME + ": " + browserType);
            }
            return createRemoteWebDriverManager(configProvider, auth, dc);
        }

        private static IDriverManager createRemoteWebDriverManager(ConfigProvider configProvider, ITauWebTest.Authenticable authenticable, DesiredCapabilities desiredCapabilities) {
            String hubUrls = configProvider.get(TAUCLOUD_ROUTER_URL);
            String hubBrowser = configProvider.get(TAUCLOUD_BROWSER_VERSION);
            String hubPlatform = configProvider.get(TAUCLOUD_NODE_PLATFORM);

            return null;
        }

    }

    public WebDriver start() throws Exception;

    public void stop() throws Exception;

    public abstract void forceStop() throws Exception;

    public List<WebDriver> getWebDrivers();

    public WebDriver getWebDriver();
}
