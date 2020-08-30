package com.mgk.tau.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.net.URL;

public final class TauUrlUtils {
    private TauUrlUtils() {

    }

    public static File toTempFileIfRequired(String targetTempFilePath, URL url) {
        if(url == null || targetTempFilePath == null) {
            return  null;
        }

        try {
            File f = FileUtils.toFile(url);
            if(f != null) {
                return  f;
            } else {
                File tempFile = new File(targetTempFilePath);
                FileUtils.copyURLToFile(url, tempFile);
                tempFile.deleteOnExit();
                return tempFile;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static File toTempFileIfRequired(URL url) {
        if(url == null) {
            return  null;
        }

        try {
            File f = FileUtils.toFile(url);
            if(f != null) {
                return  f;
            } else {
                String name = FilenameUtils.getName(url.getPath());
                File tempFile = File.createTempFile(name, "");
                FileUtils.copyURLToFile(url, tempFile);
                tempFile.deleteOnExit();
                return tempFile;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

