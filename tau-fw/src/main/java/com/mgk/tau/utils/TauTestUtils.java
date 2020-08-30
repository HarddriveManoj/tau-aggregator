package com.mgk.tau.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import java.util.HashSet;
import java.util.Set;

public class TauTestUtils {
    private static Logger log = LoggerFactory.getLogger(TauTestUtils.class);

    public static Set<Object> getTCInstances(ITestContext ctx) {
        log.debug("Retrieving all test classes in context: " + ctx.getName());

        Set<Object> testClasses = new HashSet<>();

        for(ITestNGMethod method : ctx.getAllTestMethods()) {
            Object testClass = method.getInstance();
            testClasses.add(testClass);

            log.debug("Found method: " + method.getMethodName() + " in class " + testClass.getClass().getName());
        }
        return testClasses;
    }
}
