package com.mgk.tau;


import com.google.inject.Inject;
import com.mgk.tau.annotations.test.GetSharedData;
import com.mgk.tau.annotations.test.SetAsSharedData;
import com.mgk.tau.annotations.test.SharedDataPersistent;
import com.mgk.tau.exceptions.TauException;
import com.mgk.tau.harness.TestHarness;
import com.mgk.tau.input.ExcelInputProvider;
import com.mgk.tau.input.data.InputFixedParams;
import com.mgk.tau.persist.MapperFactory;
import com.mgk.tau.utils.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class AbstractBasicTest<T extends TestHarness> extends TestNG implements TauTest, ITest {

    private static final ThreadLocal<String> currentTestName = new ThreadLocal<>();
    private static final Logger logger = LoggerFactory.getLogger(AbstractBasicTest.class);
    private final Class<T> decoratorClassType;
    private final String propertiesFileName;
    @Inject
    protected ConfigProvider configProvider;
    protected TestHarness testHarness = null;
    protected Class<T> testHarnessClassType;
    protected int webDriverCount = 1;


    protected AbstractBasicTest(Class<T> decoratorClassType, String propertiesFileName) {
        this.decoratorClassType = decoratorClassType;
        this.propertiesFileName = propertiesFileName;
    }

    private AbstractBasicTest(Class<T> decoratorClassType) {
        this(decoratorClassType, null);
    }

    @Override
    public String getTestName() {
        if (currentTestName.get() == null) {
            return "TAU has not set any name for this test";
        }
        return currentTestName.get();
    }

    public void setTestName(String name) {
        currentTestName.set(name);
    }

    public void prepareTestName(Method method, Object[] args) {
        if (method.getAnnotation(Test.class) != null) {
            logger.info("Setting the test name to be used in testng and tau report");
            String methodName = method.getName();
            try {
                Test testAnnoation = method.getAnnotation(Test.class);
                if (testAnnoation != null) {
                    String overriddenTestName = testAnnoation.testName();
                    if (StringUtils.isNotBlank(overriddenTestName)) {
                        methodName = overriddenTestName;
                    }
                    if (config().getEnvironment() != null && Boolean.valueOf(config().getOptional(TauConstants.TAU_REPORTER_SHOW_ENV_IN_REPORT))) {
                        methodName = config().getEnvironment().getName() + " | " + methodName;
                    }
                }
            } catch (SecurityException se) {
                throw new TauException("Method: " + methodName + " can't be accessed,", se);
            }
            methodName = appendKey(methodName, args);
            setTestName(methodName);
        }
    }

    private String appendKey(String methodName, Object[] args) {
        Map<String, Object> input = null;
        for (Object o : args) {
            if (o instanceof Map) {
                input = (Map<String, Object>) o;
            }
            break;
        }
        if (input != null) {
            Object o = input.get(InputFixedParams.ITERATION_NUMBER_TEST);
        } else {
            Object keywordTestCase = null;
            for (Object o : args) {
                if (o != null && (o.getClass().getName().equals("com.mgk.tau.keyword.TestCase"))) {
                    keywordTestCase = o;
                    break;
                }
            }
            if (keywordTestCase != null) {
                methodName = keywordTestCase.toString();
            }
        }
        return methodName;
    }

    private String appendDiscriminator(String methodName, Object o) {
        String discriminator = (String) o;
        try {
            return methodName + "_" + URLEncoder.encode(discriminator, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new TauException("UTF-8 is not supported by platform", e);
        }
    }

    @AfterMethod
    public void prepareNextTestName(Method method, Object[] o) {
        if (method.getAnnotation(Test.class) != null) {
            setTestName(null);
        }
    }

    @BeforeClass(alwaysRun = true)
    protected void initClassAfterConstructor() {
        if (propertiesFileName != null) {
            config().loadCustomClassProperties(CMUtils.getConfigProperties(propertiesFileName));
        }

        testHarness = TestHarness.create(configProvider, decoratorClassType);

        System.setProperty(TauConstants.TAU_WORDIFY_OTP, configProvider.get(TauConstants.TAU_WORDIFY_OTP));
        System.setProperty(TauConstants.REPORTER_ZIP, configProvider.get(TauConstants.REPORTER_ZIP));
        System.setProperty(TauConstants.REPORTER_ZIP_LOCATION, configProvider.get(TauConstants.REPORTER_ZIP_LOCATION));
        System.setProperty(TauConstants.REPORTER_ZIP_PREFIX, configProvider.get(TauConstants.REPORTER_ZIP_PREFIX));

        ThreadLocalProperties.activate();

        if (config().getOptional(TauConstants.JAVAX_NET_SSL_TRUSTSTORE) != null) {
            URL trustStore = this.getClass().getClassLoader().getResource(config().get(TauConstants.JAVAX_NET_SSL_TRUSTSTORE));
            if (trustStore == null) {
                logger.warn("Cannot be loaded as resource: [" + config().get(TauConstants.JAVAX_NET_SSL_TRUSTSTORE) + "]");
                System.setProperty("javax.net.ssl.truststore", config().get(TauConstants.JAVAX_NET_SSL_TRUSTSTORE));
            } else {
                System.setProperty("javax.net.ssl.truststore", TauUrlUtils.toTempFileIfRequired(trustStore).getAbsolutePath());
            }
        }

        if (config().getOptional(TauConstants.JAVAX_NET_SSL_KEYSTORE) != null) {
            URL keyStore = this.getClass().getClassLoader().getResource(config().get(TauConstants.JAVAX_NET_SSL_KEYSTORE));
            if (keyStore == null) {
                logger.warn("Cannot be loaded as resource: [" + config().get(TauConstants.JAVAX_NET_SSL_KEYSTORE) + "]");
                System.setProperty("javax.net.ssl.keystore", config().get(TauConstants.JAVAX_NET_SSL_KEYSTORE));
            } else {
                System.setProperty("javax.net.ssl.keystore", TauUrlUtils.toTempFileIfRequired(keyStore).getAbsolutePath());
            }
        }

        if (config().getOptional(TauConstants.JAVAX_NET_SSL_KEYSTORE_FILE) != null) {
            try {
                URL keyStoreFile = this.getClass().getClassLoader().getResource(config().get(TauConstants.JAVAX_NET_SSL_KEYSTORE_FILE));
                if (keyStoreFile == null) {
                    logger.warn("Cannot be loaded as resource: [" + config().get(TauConstants.JAVAX_NET_SSL_KEYSTORE_FILE) + "]");
                    System.setProperty("javax.net.ssl.keystore", config().get(TauConstants.JAVAX_NET_SSL_KEYSTORE_FILE));
                } else {
                    System.setProperty("javax.net.ssl.keystore", IOUtils.toString(TauUrlUtils.toTempFileIfRequired(keyStoreFile).toURI(), StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                throw new TauException("Error while reading password file: " + config().get(TauConstants.JAVAX_NET_SSL_KEYSTORE_FILE));
            }
        }
    }


    public ConfigProvider config() {
        return configProvider;
    }

    @BeforeMethod(alwaysRun = true)
    protected void beforeMethod(Method method, Object[] args) {
        if (testHarness != null) {
            testHarness.beforeMethod(method, args);
        }
    }

    @AfterMethod(alwaysRun = true)
    protected void afterMethod(ITestResult result) {
        if (testHarness != null) {
            testHarness.afterMethod(result);
        }
    }

    @BeforeClass(alwaysRun = true)
    @Parameters({"environment.name"})
    public void setupEnvironmentReportingNameOnClassLevel(
            @org.testng.annotations.Optional String environmentReportingName) {
        if (testHarness != null) {
            testHarness.setEnvironmentReportingName(environmentReportingName);
        }
    }


    @AfterClass(alwaysRun = true)
    public void setSharedData(ITestContext ctx) throws Exception {
        for (Object tcInstance : TauTestUtils.getTCInstances(ctx)) {
            Map<String, Object> map = getAllSetAsSharedData(tcInstance);
            if (!map.isEmpty()) {
                String persistentFileName = getFlenameIfSharedDataPersistentAnnotationPresent(tcInstance.getClass());
                if (persistentFileName != null) {
                    saveIntoTestContext(map, ctx);
                } else {
                    saveToFile(map, persistentFileName);
                }
            }
        }
    }

    @BeforeClass(alwaysRun = true)
    public void getSharedData(ITestContext ctx) throws Exception {
        for (Object tcInstance : TauTestUtils.getTCInstances(ctx)) {
            Map<String, Object> getAsFields = getAllGetAsSharedData(tcInstance);

            if (!getAsFields.isEmpty()) {
                String persistentFileName = getFlenameIfSharedDataPersistentAnnotationPresent(tcInstance.getClass());
                Map<String, Object> values = (persistentFileName != null) ? readFromFile(persistentFileName, getAsFields) : readFromContext(ctx, getAsFields);
                applyToTestInstance(tcInstance, getAsFields, values);
            }
        }
    }

    private void saveIntoTestContext(Map<String, Object> map, ITestContext ctx) {
        logger.debug("Saved shared fields into test context");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            ctx.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    private Properties readFromFile(String fileName) throws Exception {
        Properties properties = new Properties();
        File file = resolveFileName(fileName);
        if (file.exists()) {
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(fileName);
                properties.load(fileReader);
            } finally {
                IOUtils.closeQuietly(fileReader);
            }
        }
        return properties;
    }


    private File resolveFileName(String fileName) throws Exception {
        if (new File(fileName).exists()) {
            return new File(fileName);
        }

        ClassPathResource cRes = new ClassPathResource(fileName);
        if (cRes.exists() && cRes.getFile() != null && cRes.getFile().exists()) {
            return cRes.getFile();
        }
        return new File(fileName);
    }

    private void saveToFile(Map<String, Object> map, String fileName) throws Exception {
        logger.debug("Persisting shared data into file: {} ", fileName);
        Map<String, String> converted = convertSupportedDataTypes(map);
        Properties properties = new Properties();

        for (Map.Entry<String, String> entry : converted.entrySet()) {
            String value = (String) entry.getValue();
            if (value != null) {
                properties.setProperty(entry.getKey(), value);
            }
        }

        File file = resolveFileName(fileName);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            properties.store(fileWriter, null);
            fileWriter.flush();
        } finally {
            IOUtils.closeQuietly(fileWriter);
        }
    }

    private Map<String, String> convertSupportedDataTypes(Map<String, Object> toConvert) throws Exception {
        Map<String, String > converted = new LinkedHashMap<>();
        for(Map.Entry<String, Object> entry : toConvert.entrySet()) {
            Object value = entry.getValue();
            converted.put(entry.getKey(), value == null ? null : MapperFactory.getMapper(value.getClass().getName()).convertToString(value));
        }

        return converted;
    }

    private Map<String, Object> getAllSetAsSharedData(Object testClassInstance) {
        Map<String, Object> map = new LinkedHashMap<>();
        Class testClass = testClassInstance.getClass();

        logger.debug("Checking for {} annotated fields in class {}", SetAsSharedData.class.getName(), testClass.getName());

        for (Field field : testClass.getDeclaredFields()) {
            logger.debug("Checking field {}", field.getName());
            SetAsSharedData setAsSharedData = field.getAnnotation(SetAsSharedData.class);

            if (setAsSharedData != null) {
                logger.debug("Field is annoated with {}, will be retained, ", SetAsSharedData.class.getName());

                String attributeName = setAsSharedData.name();
                if (attributeName.isEmpty()) {
                    attributeName = field.getName();
                }
                try {
                    field.setAccessible(true);
                    logger.debug("Retaining the field {} using the name {}, with the value {}", field.getName(), attributeName, field.get(testClassInstance));
                } catch (IllegalAccessException e) {
                    logger.error("Error in obtaining value of field: " + field.getName() + " from class: " + testClass.getName() + ": " + e, e);
                }
            }
        }
        return map;
    }

    private Map<String, Field> getAllGetAsSharedData(Object tcInstance) {
        Map<String, Field> map = new LinkedHashMap<>();

        Class testClass = tcInstance.getClass();
        logger.debug("Checking for {} annotated fields in class {}", GetSharedData.class.getName(), testClass.getName());

        for (Field field : testClass.getDeclaredFields()) {
            logger.debug("Checking field {}", field.getName());
            GetSharedData getSharedData = field.getAnnotation(GetSharedData.class);

            if (getSharedData != null) {
                logger.debug("Field is annoated with {}, will be retained, ", GetSharedData.class.getName());

                String attributeName = getSharedData.name();
                if (attributeName.isEmpty()) {
                    attributeName = field.getName();
                }
                map.put(attributeName, field);
            }
        }
        return map;

    }

    private void applyToTestInstance(Object tcInstance, Map<String, Field> getAsFields, Map<String, Object> values) throws Exception {
        for(Map.Entry<String, Field> entry : getAsFields.entrySet()) {
            String key = entry.getKey();
            Object attrValue = values.get(key);
            Field field = getAsFields.get(key);
            entry.getValue().setAccessible(true);

            if(attrValue != null) {
                try {
                    field.setAccessible(true);
                    if(attrValue instanceof String && StringUtils.isNotBlank((String) attrValue)) {
                        field.set(tcInstance, MapperFactory.getMapper(field).convertFromString((String) attrValue));
                        logger.debug("Successfully set the value of the field: {} from the attribute {}", field.getName(), key);
                    } else if(attrValue != null) {
                        field.set(tcInstance, attrValue);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error in setting the value of the field" +  field.getName() + " to: " + attrValue);
                }
            }
        }
    }

    private Map<String, Object> readFromFile(String persistentFileName, Map<String, Field> getAsFields) throws  Exception {
        Properties properties = readFromFile(persistentFileName);
        Map<String, Object> map = new LinkedHashMap<>();

        for(Map.Entry<String, Field> entry : getAsFields.entrySet()) {
            String key = entry.getKey();
            String value = properties.getProperty(key);
            if(StringUtils.isNotBlank(value)) {
                logger.debug("Found the value of {} for the attribute {} in persistent file", value, key);
                map.put(key, value);
            }
        }
        return map;
    }

    private Map<String, Object> readFromContext(ITestContext ctx, Map<String, Field> getAsFields) throws  Exception {

        Map<String, Object> map = new LinkedHashMap<>();

        for(Map.Entry<String, Field> entry : getAsFields.entrySet()) {
            String key = entry.getKey();
            Object value = ctx.getAttribute(key);
            if(value != null) {
                logger.debug("Found the value of {} for the attribute {} in test context", value, key);
                map.put(key, value);
            }
        }
        return map;
    }

    private String getFileNameIfSharedDataPersistenAnnotationPresent(Class<?> testClass) {
        if(testClass.getAnnotation(SharedDataPersistent.class) != null) {
            return testClass.getAnnotation(SharedDataPersistent.class).filename();
        }

        Class<?> C = testClass.getSuperclass();
        while(C != null) {
            if(C.getAnnotation(SharedDataPersistent.class) != null) {
                return C.getAnnotation(SharedDataPersistent.class).filename();
            }
            C = C.getSuperclass();
        }
        return null;
    }


    public TestHarness getTestHarness() {
        return testHarness;
    }

    public void setTestHarness(T testHarness) {
        this.testHarness = testHarness;
    }

    public Class<T> getTestHarnessClassType() {
        return testHarnessClassType;
    }

    public void setTestHarnessClassType(Class<T> testHarnessClassType) {
        this.testHarnessClassType = testHarnessClassType;
    }

    public int getWebDriverCount() {
        return webDriverCount;
    }

    public void setWebDriverCount(int webDriverCount) {
        this.webDriverCount = webDriverCount;
    }


    @DataProvider(name = "TauExcelInputProvider", parallel = false)
    public Object[][] tauExcelInputProvider(Method testMethod) throws IOException, InstantiationException, IllegalAccessException {
        reutrn tauExcelInputProviderRun(testMethod);
    }

    @DataProvider(name = "TauExcelInputProviderParallel", parallel = true)
    public Object[][] tauExcelInputProviderParallel(Method testMethod, ITestContext iTestContext) throws IOException, InstantiationException, IllegalAccessException {
        reutrn tauExcelInputProviderRun(testMethod);
    }

    @DataProvider(name = "TauExcelGroupedInputProvider", parallel = false)
    public Object[][] tauExcelGroupedInputProvider(Method testMethod, ITestContext iTestContext) throws IOException, InstantiationException, IllegalAccessException {
        reutrn tauExcelGroupedInputProviderRun(testMethod);
    }

    @DataProvider(name = "TauExcelGroupedInputProviderParallel", parallel = true)
    public Object[][] tauExcelGroupedInputProviderParallel(Method testMethod, ITestContext iTestContext) throws IOException, InstantiationException, IllegalAccessException {
        reutrn tauExcelGroupedInputProviderRun(testMethod);
    }

    @DataProvider(name = "TauExcelTreeGroupedInputProvider", parallel = false)
    public Object[][] tauExcelTreeGroupedInputProvider(Method testMethod, ITestContext iTestContext) throws IOException, InstantiationException, IllegalAccessException {
        reutrn tauExcelTreeGroupedInputProviderRun(testMethod);
    }

    @DataProvider(name = "TauExcelTreeGroupedInputProviderParallel", parallel = true)
    public Object[][] tauExcelTreeGroupedInputProviderParallel(Method testMethod, ITestContext iTestContext) throws IOException, InstantiationException, IllegalAccessException {
        reutrn tauExcelTreeGroupedInputProviderRun(testMethod);
    }

    private Object[][] tauExcelInputProviderRun(Method testMethod) throws IllegalAccessException, InstantiationException, FileNotFoundException {
        TauExcelInput tei = testMethod.isAnnotationPresent(TauExcelInput.class) ? testMethod.getAnnotation(TauExcelInput.class) : null;

        String className = tei != null && StringUtils.isNotBlank(tei.filename()) ? tei.filename() : this.getClass().getSimpleName();

        String tabName = tei != null && ArrayUtils.isNotEmpty(tei.sheetName()) ? tei.sheetName()[0] : testMethod.getName();

        String testCaseNameKey = tei != null && StringUtils.isNotBlank(tei.testCaseNameKey()) ? tei.testCaseNameKey() : null;

        String tabNameForEach = tei != null & StringUtils.isNotBlank(tei.forEach()) ? tei.forEach() : null;
        Boolean forEachLookup = tei != null && tei.forEachLookup();

        String dataFile = findDataFile(className, new String[]{"xls", "xlsx"});

        if(forEachLookup) {
            List<Object[]> results = new ArrayList<>();
            List<Map<String, String>> masterData = ExcelInputProvider.getTestDataListWithMaps(dataFile, tabName, testCaseNameKey);
            for(Map<String, String> masterRow : masterData) {
                List<String[]> detailFileSheets = new ArrayList<>();
                Map<String, String> plainFields = new HashMap<>();

                for(Map.Entry<String, String> nameValue : masterRow.entrySet()) {
                    String[] fileSheet = ExcelInputProvider.extractIfFileSheet(nameValue.getValue());
                    if(fileSheet!=null) {
                        detailFileSheets.add(fileSheet);
                    } else {
                        if(!nameValue.getValue().isEmpty()) {
                            plainFields.put(nameValue.getKey(), nameValue.getValue());
                            logger.trace("Add field plain {}", nameValue.getKey());
                        }
                    }
                }


                List<List<Map<String, String>>> detailSheets = new ArrayList<>();
                for(String[] fileSheet : detailFileSheets) {
                    logger.debug("Read detail sheet: {}!{}", fileSheet[0], fileSheet[1]);
                    String detailFile = findDataFile(fileSheet[0], new String[]{"xls", "xlsx"});
                    List<Map<String, String>> detailData = ExcelInputProvider.getTestDataListWithMaps(detailFile, fileSheet[1]);
                    if(!detailData.isEmpty()) {
                        detailSheets.add(detailData);
                        logger.trace("Add elements: ({}) {}", detailData.size(), detailData.get(0).keySet().toString());
                    }
                }

                if(!plainFields.isEmpty()) {
                    List<Map<String, String>> plainData = new ArrayList<>();
                    plainData.add(plainFields);
                    detailSheets.add(plainData);
                    logger.trace("Add elements plain: ({}) {}", plainData.size() + plainData.get(0).keySet().toString());
                }
                List<Map<String, String>> sheetProduct = ExcelInputProvider.combineDetailSheets(detailSheets);

                for(Map<String, String> m : sheetProduct) {
                    results.add(new Object[]{m});
                }
            }
            return results.toArray(new Object[0][]);
        } else if(tabNameForEach != null) {
            List<Map<String, String>> factoryData = ExcelInputProvider.getTestDataListWithMaps(dataFile, tabNameForEach, null);
            List<Object[]> results = new ArrayList<>();
            List<Map<String, String>> tcData = ExcelInputProvider.getTestDataListWithMaps(dataFile, tabName, testCaseNameKey);
            int iterationCounter = 0;
            for(Map<String, String> fRow : factoryData) {
                for(Map<String, String> tcRow : tcData) {
                    Map<String, String> tmpTc = new HashMap<>();
                    tmpTc.putAll(tcRow);
                    iterationCounter++;
                    tmpTc.put(InputFixedParams.ITERATION_NUMBER_TEST, String.valueOf(iterationCounter));
                    results.add(new Object[] {tmpTc});
                }
            }
            return  results.toArray(new Object[0][]);
        } else {
            return ExcelInputProvider.getTestData(dataFile, tabName, testCaseNameKey);
        }
    }

    private Object[][] tauExcelGroupedInputProviderRun(Method testMethod) throws Exception {
        Validation.assertTrueData(testMethod.isAnnotationPresent(TauExcelInput.class), "@TauExcelInput annotation must be present");
        TauExcelInput tei = testMethod.getAnnotation(TauExcelInput.class);
        String className = StringUtils.isNotBlank(tei.filename()) ? tei.filename() : this.getClass();

        String[] tabNames = ArrayUtils.isNotEmpty(tei.sheetName()) ? tei.sheetName() : new String[] { testMethod.getName()};

        Validation.assertTrueData(StringUtils.isNotBlank(tei.testCaseGroupName()), "testCaseGroupName atttribute of @TauExcelInput annotation must not be blank");
        Validation.assertTrueData(StringUtils.isNotBlank(tei.testCaseNameKey()), "testCaseNameKey attribute of @TauExcelInput annotation must not be blank");

        Validation.assertTrueData(tei.startCol()>=0, "startCol attribute of @TauExcelInput annotation must be greater than 0");
        Validation.assertTrueData(tei.startRow()>=0, "startRow attribute of @TauExcelInput annotation must be greater than 0");
        String detailFile = findDataFile(className, new String[]{"xls", "xlsx"});

        List<LinkedHashMap<String, TestCaseGroupData>> tcGroupsInSheets = new ArrayList<>();
        for(String tabName : tabNames) {
            tcGroupsInSheets.add(ExcelGroup)
        }




    }



}