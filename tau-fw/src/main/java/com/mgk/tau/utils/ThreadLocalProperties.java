package com.mgk.tau.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ThreadLocalProperties extends Properties  {
    private static final Logger log = LoggerFactory.getLogger(ThreadLocalProperties.class);
    private final ThreadLocal<Properties> localProperties = ThreadLocal.withInitial(Properties :: new);
    private static Properties sysProps;

    private ThreadLocalProperties(Properties properties) {
        super(properties);
    }

    @Override
    public String getProperty(String key) {
        String localValue = localProperties.get().getProperty(key);
        return localValue == null ? super.getProperty(key) : localValue;
    }

    @Override
    public Object setProperty(String key, String value) {
        return localProperties.get().setProperty(key, value);
    }

    public static synchronized void activate() {
        synchronized (ThreadLocalProperties.class) {
            if(sysProps != null) {
                log.debug("ThreadLocalProperties already active, not reinitializing them");
                return;
            }

            sysProps = System.getProperties();
            System.setProperties(new ThreadLocalProperties(sysProps));
        }
    }

    public static synchronized void deactivate() {
        synchronized (ThreadLocalProperties.class) {
            assert sysProps != null;
            System.setProperties(sysProps);
            sysProps = null;
        }
    }


}
