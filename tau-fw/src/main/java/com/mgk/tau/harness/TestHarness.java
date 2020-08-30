package com.mgk.tau.harness;

import com.mgk.tau.ConfigProvider;
import com.mgk.tau.exceptions.TauException;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.StringTokenizer;

public abstract  class TestHarness {

    private static Logger logger = LoggerFactory.getLogger(TestHarness.class);

    protected volatile String environmentReportingName;
    protected volatile String environmentName;

    protected TestHarness testHarnessDecorator = null;

    public TestHarness() {

    }

    public TestHarness(TestHarness testHarness) {
        this.testHarnessDecorator = testHarness;
    }


    protected abstract TestHarness create(TestHarness decoratedHarness, ConfigProvider configProvider);

    public static String getDecoratorsBountyParameter() {
        return "tau.testharness-decorator.list";
    }

    public static TestHarness create(ConfigProvider configProvider, Class<?> decoratorClass) throws TauException {
        String decoratorsListProperty = "tau.testharness-decorator.list";

        if(TestHarness.class.isAssignableFrom(decoratorClass)) {
            try {
                Method method = decoratorClass.getMethod("getDecoratorsTauParameter");
                decoratorsListProperty = (String) method.invoke(decoratorClass);
            } catch (Exception e) {
                throw new TauException(e);
            }
        }

        String testHarnessDecoratorList = configProvider.getOptional(decoratorsListProperty);
        if(testHarnessDecoratorList == null || testHarnessDecoratorList.isEmpty()) {
            logger.info("### Decorator list is empty, running the test without decorators. You can configure with the same + " +decoratorsListProperty + " ###");
            return null;
        }

        StringTokenizer st = new StringTokenizer(testHarnessDecoratorList, ", ");

        TestHarness testHarness = null;

        while (st.hasMoreTokens()) {
            String decoratorClassName = st.nextToken();
            logger.info("Creating:" +decoratorClassName);
            testHarness = locateFactory(decoratorClassName, decoratorClass).create(testHarness, configProvider);

        }
        return testHarness;
    }

    private static TestHarness locateFactory(String discriminator, Class<?> decoratorClass) throws TauException {
        Class<?> fc;

        try {
            try {
                fc = Class.forName(discriminator);
            } catch (Exception e) {
                String ndiscriminator = TestHarness.class.getPackage().getName() + "." + discriminator;
                logger.info("Decorator class: " +discriminator + " not found in classpath, trying to load " +ndiscriminator + " instead.");
                fc = Class.forName(ndiscriminator);
            }

            if(decoratorClass.isAssignableFrom(fc) || TestHarness.class.isAssignableFrom(fc)) {
                return (TestHarness) fc.newInstance();
            } else {
                throw new TauException("Expecting instance of " + decoratorClass.getName() + ": invalid decorator class: " + discriminator + "");
            }
        } catch (ClassNotFoundException cnfe) {
            logClassPath();
            throw new TauException("Missing " + discriminator + "class, extension package is likely missing");
        } catch (InstantiationException ie) {
            logClassPath();
            throw new TauException("Couldn't instantiate" + discriminator + "class, ensure correct implementation (visibility) in " +discriminator, ie);
        } catch (Exception e) {
            logClassPath();
            throw new TauException("Couldn't instantiate" + discriminator + "class, ensure correct implementation (visibility) in " +discriminator, e);
        }

    }

    private static void logClassPath() {
        String classPath = System.getProperty("java.class.path");
        StringTokenizer stringTokenizer = new StringTokenizer(classPath, ":");
        while(stringTokenizer.hasMoreTokens()) {
            logger.trace(stringTokenizer.nextToken());
        }
    }

    public TestHarness getTestHarness() {
        return testHarnessDecorator;
    }

    public void setTestHarness(TestHarness testHarness) {
        this.testHarnessDecorator = testHarness;
    }

    public void setUp(Class<?> testClass) {
        if(testHarnessDecorator!=null) {
            testHarnessDecorator.setUp(testClass);
        }
    }

    public void suiteTearDown() {
        if(testHarnessDecorator!=null) {
            testHarnessDecorator.suiteTearDown();
        }
    }

    public void beforeMethod(Method method, Object[] args) {
        if(testHarnessDecorator!=null) {
            testHarnessDecorator.beforeMethod(method, args);
        }
    }

    public void afterMethod(ITestResult iTestResult) {
        if(testHarnessDecorator!=null) {
            testHarnessDecorator.afterMethod(iTestResult);
        }
    }

    public String getEnvironmentReportingName() {
        if(environmentReportingName != null) {
            return environmentReportingName;
        } else {
             return environmentName;
        }
    }

    public void setEnvironmentReportingName(String environmentReportingName) {
        this.environmentReportingName = environmentReportingName;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

}
