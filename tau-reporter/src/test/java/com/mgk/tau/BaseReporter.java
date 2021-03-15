package com.mgk.tau;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Protocol;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.*;

import java.io.UnsupportedEncodingException;

public class BaseReporter {
    ExtentSparkReporter extentReporter;
    ExtentReports extentReports;
    ExtentTest test;

    @Parameters({"OS", "browser"})
    @BeforeTest
    public void startReport(String OS, String browser) throws UnsupportedEncodingException {
        extentReporter = new ExtentSparkReporter(System.getProperty("user.dir") + "/testReport.html") {
            @Override
            public int hashCode() {
                return super.hashCode();
            }
        };

        extentReports = new ExtentReports();
        extentReports.attachReporter(extentReporter);

        extentReports.setSystemInfo("OS", OS);
        extentReports.setSystemInfo("browser", browser);

        extentReporter.config().setOfflineMode(true);
        extentReporter.config().setDocumentTitle("Extent Report Demo");
        extentReporter.config().setReportName("Test Report");
        extentReporter.config().setProtocol(Protocol.HTTPS);

        extentReporter.config().setTheme(Theme.DARK);
        extentReporter.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a '('zzz')'");
    }

    @Test
    public void testCase1() {
        test = extentReports.createTest("Test Case 1", "PASSED test case");
        Assert.assertTrue(true);
    }

    @Test
    public void testCase2() {
        test = extentReports.createTest("Test Case 2", "PASSED test case");
        Assert.assertTrue(true);
    }

    @Test
    public void testCase3() {
        test = extentReports.createTest("Test Case 3", "PASSED test case");
        Assert.assertTrue(true);
    }

    @Test
    public void testCase4() {
        test = extentReports.createTest("Test Case 4", "PASSED test case");
        Assert.assertTrue(false);
    }

    @Test
    public void testCase5() {
        test = extentReports.createTest("Test Case 5", "SKIPPED test case");
        throw new SkipException("Skipping this test with exception");
    }

    @Test(enabled = false)
    public void testCase6() {
        test = extentReports.createTest("Test Case 6", "I'm Not Ready, please don't execute me");
    }

    @AfterMethod
    public void getResult(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            test.log(Status.FAIL, MarkupHelper.createLabel(result.getName() + " FAILED ", ExtentColor.RED));
            test.fail(result.getThrowable());
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            test.log(Status.PASS, MarkupHelper.createLabel(result.getName() + " PASSED ", ExtentColor.GREEN));
        } else {
            test.log(Status.SKIP, MarkupHelper.createLabel(result.getName() + " SKIPPED ", ExtentColor.ORANGE));
            test.skip(result.getThrowable());
        }
    }

    @AfterTest
    public void tearDown() {
        //to write or update test information to reporter
        extentReports.flush();
    }
}
