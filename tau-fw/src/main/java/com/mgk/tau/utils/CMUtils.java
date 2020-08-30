package com.mgk.tau.utils;

import com.mgk.tau.ConfigProvider;
import com.mgk.tau.exceptions.TauException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class CMUtils {

    private static final String CONFIG_BASE_FOLDER = "config";

    private static Logger log = LoggerFactory.getLogger(CMUtils.class);

    public static String getRequiredConfig(ConfigProvider configProvider, String name) {
        String value = configProvider.get(name, null);
        if(value == null || value.equals("")) {
            throw new TauException("Missing required parameter : (" +name+ ")");
        }
        return value;
    }

    public static Properties getConfigProperties(String fileName) {
        String fqfn = CONFIG_BASE_FOLDER + "/" + fileName;

        URL location = ClassLoader.getSystemResource(fqfn);
        if(location == null) {
            throw new TauException(fqfn + " does not exist in " +System.getProperty("java.class.path"));
        }

        InputStream in = null;
        try {
            in = location.openStream();
        } catch (IOException ioe) {
            throw new TauException(location +" : is found, but not readable", ioe);
        }

        Properties ret = new Properties();
        try {
            ret.load(in);
            log.debug("Obtained properties from "+ location);
        } catch (IOException ioe) {
            throw new TauException("Digesting  " + location + "failed", ioe);
        } finally {
            try {
                if(in!=null) {
                    in.close();
                }
            } catch (IOException ioe) {
                log.warn("Failed to close stream on " + location, ioe);
            }
        }
        return  ret;
    }
}
