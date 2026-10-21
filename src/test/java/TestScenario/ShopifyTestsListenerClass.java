
package TestScenario;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.base.ExtentReport;
import com.base.GDrivePOST;
import com.tommyjohn.automation.utils.CustomUtilities;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.IExecutionListener;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.base.Slack.sendSlackNotificationShopify;

public class ShopifyTestsListenerClass extends ExtentReport
        implements ITestListener, IExecutionListener {

    private static final Map<String, ExtentTest> extentTests =
            new ConcurrentHashMap<>();

    private static final Map<String, Integer> finalResults =
            new ConcurrentHashMap<>();

    private static final Map<String, String> testNames =
            new ConcurrentHashMap<>();

    private static final Map<String, Throwable> finalErrors =
            new ConcurrentHashMap<>();

    private static final Map<String, String> finalScreenshots =
            new ConcurrentHashMap<>();

    private static volatile String browserName;
    private static volatile String suiteName;

    public static String reportLink = "Shopify report upload failed";

    public int Fpassed = 0;
    public int Ffailed = 0;
    public int Fskipped = 0;

    // ================= EXECUTION START =================

    @Override
    public void onExecutionStart() {
        extentTests.clear();
        finalResults.clear();
        testNames.clear();
        finalErrors.clear();
        finalScreenshots.clear();

        Fpassed = 0;
        Ffailed = 0;
        Fskipped = 0;

        reportLink = "Shopify report upload failed";

        initializeExtentReports();

        System.out.println("=================================");
        System.out.println("Shopify test execution started");
        System.out.println("=================================");
    }

    // ================= SUITE START =================

    @Override
    public void onStart(ITestContext context) {
        suiteName = context.getSuite().getName();

        browserName = context.getCurrentXmlTest()
                .getParameter("browser");

        System.out.println("=================================");
        System.out.println("Suite: " + suiteName);
        System.out.println("Context: " + context.getName());
        System.out.println("Browser: " + browserName);
        System.out.println("=================================");

        if (sparkReporter_AllTests != null) {
            sparkReporter_AllTests.config()
                    .setReportName("Shopify Test Report");
        }
    }

    // ================= TEST IDENTIFICATION =================

    private String getTestKey(ITestResult result) {
        return result.getTestClass().getName()
                + "."
                + result.getMethod().getMethodName()
                + "|params="
                + Arrays.deepToString(result.getParameters())
                + "|instance="
                + System.identityHashCode(result.getInstance());
    }

    private ExtentTest getOrCreateExtentTest(ITestResult result) {
        String key = getTestKey(result);

        ExtentTest test = extentTests.computeIfAbsent(key, k -> {
            synchronized (extentReport) {
                return extentReport.createTest(result.getName());
            }
        });

        testNames.put(key, result.getName());

        return test;
    }

    // ================= TEST START =================

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = getOrCreateExtentTest(result);

        ExtentReport.setExtentTest(test);

        test.info("Executing Shopify test attempt");

        System.out.println(
                "Executing Shopify test: " + result.getName()
                        + " | Thread: "
                        + Thread.currentThread().getName()
        );
    }

    // ================= SUCCESS =================

    @Override
    public void onTestSuccess(ITestResult result) {
        try {
            ExtentTest test = getOrCreateExtentTest(result);

            test.info("Attempt completed successfully");

            System.out.println(
                    "Shopify test attempt passed: " + result.getName()
            );
        } finally {
            ExtentReport.removeExtentTest();
        }
    }

    // ================= FAILURE =================

    @Override
    public void onTestFailure(ITestResult result) {
        String key = getTestKey(result);

        try {
            ExtentTest test = getOrCreateExtentTest(result);

            test.log(
                    Status.INFO,
                    "Attempt failed; awaiting final retry outcome."
            );

            if (result.getThrowable() != null) {
                finalErrors.put(key, result.getThrowable());
                test.log(Status.INFO, result.getThrowable());
            }

            try {
                String screenshot = CustomUtilities.screenshot();

                if (screenshot != null && !screenshot.isEmpty()) {
                    test.info(
                            "Failure attempt screenshot",
                            MediaEntityBuilder
                                    .createScreenCaptureFromBase64String(
                                            screenshot
                                    )
                                    .build()
                    );

                    finalScreenshots.put(key, screenshot);
                }
            } catch (Exception e) {
                test.info(
                        "Screenshot capture failed: " + e.getMessage()
                );
            }

            System.out.println(
                    "Shopify test attempt failed: " + result.getName()
                            + " | Awaiting final retry outcome"
            );
        } finally {
            ExtentReport.removeExtentTest();
        }
    }

    // ================= SKIPPED =================

    @Override
    public void onTestSkipped(ITestResult result) {
        try {
            ExtentTest test = getOrCreateExtentTest(result);

            test.info(
                    "Attempt skipped; final outcome will be determined "
                            + "after retries."
            );

            if (result.getThrowable() != null) {
                test.log(Status.INFO, result.getThrowable());
            }

            System.out.println(
                    "Shopify test attempt skipped: " + result.getName()
            );
        } finally {
            ExtentReport.removeExtentTest();
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(
            ITestResult result) {
        // Final results are aggregated after the context finishes.
    }

    // ================= FINAL RESULT PRIORITY =================

    private int resultPriority(int status) {
        if (status == ITestResult.SUCCESS) {
            return 3;
        }

        if (status == ITestResult.FAILURE) {
            return 2;
        }

        if (status == ITestResult.SKIP) {
            return 1;
        }

        return 0;
    }

    private void collectResult(
            Map<String, Integer> contextResults,
            ITestResult result,
            int status) {

        String key = getTestKey(result);

        contextResults.merge(
                key,
                status,
                (oldStatus, newStatus) ->
                        resultPriority(newStatus) > resultPriority(oldStatus)
                                ? newStatus
                                : oldStatus
        );

        testNames.put(key, result.getName());

        if (status == ITestResult.FAILURE
                && result.getThrowable() != null) {
            finalErrors.put(key, result.getThrowable());
        }
    }

    // ================= CONTEXT FINISH =================

    @Override
    public void onFinish(ITestContext context) {
        Map<String, Integer> contextResults =
                new ConcurrentHashMap<>();

        System.out.println("=================================");
        System.out.println(
                "Finished Shopify context: " + context.getName()
        );
        System.out.println(
                "Raw passed results: "
                        + context.getPassedTests().size()
        );
        System.out.println(
                "Raw failed results: "
                        + context.getFailedTests().size()
        );
        System.out.println(
                "Raw skipped results: "
                        + context.getSkippedTests().size()
        );
        System.out.println("=================================");

        for (ITestResult result :
                context.getPassedTests().getAllResults()) {
            collectResult(
                    contextResults, result, ITestResult.SUCCESS
            );
        }

        for (ITestResult result :
                context.getFailedTests().getAllResults()) {
            collectResult(
                    contextResults, result, ITestResult.FAILURE
            );
        }

        for (ITestResult result :
                context.getSkippedTests().getAllResults()) {
            collectResult(
                    contextResults, result, ITestResult.SKIP
            );
        }

        // Merge logical test outcomes across contexts.
        contextResults.forEach((key, status) ->
                finalResults.merge(
                        key,
                        status,
                        (oldStatus, newStatus) ->
                                resultPriority(newStatus)
                                        > resultPriority(oldStatus)
                                        ? newStatus
                                        : oldStatus
                )
        );

        // Apply one final status per logical test.
        for (Map.Entry<String, Integer> entry :
                contextResults.entrySet()) {

            String key = entry.getKey();
            int status = entry.getValue();

            ExtentTest test = extentTests.get(key);

            if (test == null) {
                continue;
            }

            String name = testNames.getOrDefault(key, key);

            if (status == ITestResult.SUCCESS) {
                test.pass(name + " passed after retries, if any.");

            } else if (status == ITestResult.FAILURE) {
                Throwable error = finalErrors.get(key);

                if (error != null) {
                    test.fail(error);
                } else {
                    test.fail(name + " failed after retries.");
                }

            } else if (status == ITestResult.SKIP) {
                test.skip(name + " was skipped.");
            }
        }

        System.out.println(
                "Unique logical outcomes in context: "
                        + contextResults.size()
        );
    }

    // ================= EXECUTION FINISH =================

    @Override
    public void onExecutionFinish() {
        Fpassed = 0;
        Ffailed = 0;
        Fskipped = 0;

        for (int status : finalResults.values()) {
            if (status == ITestResult.SUCCESS) {
                Fpassed++;
            } else if (status == ITestResult.FAILURE) {
                Ffailed++;
            } else if (status == ITestResult.SKIP) {
                Fskipped++;
            }
        }

        System.out.println("=================================");
        System.out.println("FINAL SHOPIFY RESULTS");
        System.out.println("Passed: " + Fpassed);
        System.out.println("Failed: " + Ffailed);
        System.out.println("Skipped: " + Fskipped);
        System.out.println(
                "Unique final test count: " + finalResults.size()
        );
        System.out.println("=================================");

        // Generate the final Extent report.
        try {
            generateExtentReports();
        } catch (Exception e) {
            System.err.println(
                    "Shopify Extent report generation failed."
            );
            e.printStackTrace();
        }

        // Upload the Shopify report.
        try {
            reportLink = GDrivePOST.UploadShopifyReport();

            if (reportLink == null || reportLink.trim().isEmpty()) {
                reportLink = "Shopify report upload failed";
            }

            System.out.println(
                    "Shopify report upload result: " + reportLink
            );

        } catch (Exception e) {
            reportLink = "Shopify report upload failed";

            System.err.println("Shopify report upload failed.");
            e.printStackTrace();
        }

        // Notify Slack only after a successful report upload.
        try {
            if (!reportLink.toLowerCase().contains("failed")) {
                sendSlackNotificationShopify(
                        String.valueOf(Fpassed),
                        String.valueOf(Ffailed),
                        String.valueOf(Fskipped),
                        reportLink
                );

                System.out.println(
                        "Shopify Slack notification completed."
                );
            } else {
                System.err.println(
                        "Shopify Slack notification skipped because "
                                + "the report upload failed."
                );
            }
        } catch (Exception e) {
            System.err.println(
                    "Shopify Slack notification failed."
            );
            e.printStackTrace();
        }
    }
}
