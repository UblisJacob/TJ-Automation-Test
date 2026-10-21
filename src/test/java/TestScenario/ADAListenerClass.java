
package TestScenario;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.base.ExtentReport;
import com.tommyjohn.automation.utils.CustomUtilities;
import org.testng.IExecutionListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.base.Slack.sendSlackNotificationADA;

public class ADAListenerClass extends ExtentReport
        implements ITestListener, IExecutionListener {

    private static final String UPLOAD_FAILED = "ADA report upload failed";

    private static String browserName;
    public static String reportLink = UPLOAD_FAILED;

    private String suiteName;

    private int Fpassed = 0;
    private int Ffailed = 0;
    private int Fskipped = 0;

    // One ExtentTest per logical test, reused across retry attempts.
    private final Map<String, ExtentTest> extentTests =
            new ConcurrentHashMap<>();

    // Stores the final outcome for each logical test.
    private final Map<String, ITestResult> finalResults =
            new ConcurrentHashMap<>();

    // Tracks the attempt number for logging.
    private final Map<String, AtomicInteger> attemptCounts =
            new ConcurrentHashMap<>();

    // ================= EXECUTION START =================

    @Override
    public void onExecutionStart() {
        extentTests.clear();
        finalResults.clear();
        attemptCounts.clear();

        Fpassed = 0;
        Ffailed = 0;
        Fskipped = 0;
        reportLink = UPLOAD_FAILED;

        initializeExtentReports();

        System.out.println("ADA/Regression test execution started.");
    }

    // ================= SUITE START =================

    @Override
    public void onStart(ITestContext context) {

        String adaSuiteName = "ADACheck Test Suite";

        suiteName = context.getSuite().getName();
        browserName = context.getCurrentXmlTest()
                .getParameter("browser");

        System.out.println("SuiteName: " + suiteName);
        System.out.println("Browser: " + browserName);

        if (sparkReporter_AllTests != null) {
            if (adaSuiteName.equals(suiteName)) {
                sparkReporter_AllTests.config()
                        .setReportName("ADACheck Test Report");
            } else {
                sparkReporter_AllTests.config()
                        .setReportName("Regression Test Report");
            }
        }
    }

    // ================= TEST START =================

    @Override
    public void onTestStart(ITestResult result) {

        String key = getLogicalTestKey(result);
        String testName = result.getMethod().getMethodName();

        int attempt = attemptCounts
                .computeIfAbsent(key, k -> new AtomicInteger(0))
                .incrementAndGet();

        ExtentTest test = extentTests.computeIfAbsent(
                key,
                k -> extentReport.createTest(testName)
        );

        // Set the current test for this execution thread.
        ExtentReport.setExtentTest(test);

        String message = attempt == 1
                ? "Executing " + testName + " | Attempt " + attempt
                : "Re-executing " + testName + " | Attempt " + attempt;

        System.out.println(message);
        ExtentReport.getExtentTest().info(message);
    }

    // ================= TEST SUCCESS =================

    @Override
    public void onTestSuccess(ITestResult result) {

        String key = getLogicalTestKey(result);
        finalResults.put(key, result);

        ExtentReport.getExtentTest().pass(
                result.getMethod().getMethodName()
                        + " executed successfully."
        );

        ExtentReport.getExtentTest().log(
                Status.PASS,
                "Test attempt passed."
        );

        System.out.println(
                result.getMethod().getMethodName()
                        + " passed."
        );

        ExtentReport.removeExtentTest();
    }

    // ================= TEST FAILURE =================

    @Override
    public void onTestFailure(ITestResult result) {

        String key = getLogicalTestKey(result);
        finalResults.put(key, result);

        String testName = result.getMethod().getMethodName();

        ExtentTest test = ExtentReport.getExtentTest();

        test.fail(testName + " failed on this attempt.");

        if (result.getThrowable() != null) {
            test.info(result.getThrowable());
        }

        try {
            String screenshotData = CustomUtilities.screenshot();

            if (screenshotData != null && !screenshotData.isBlank()) {
                test.info(
                        "Screenshot",
                        MediaEntityBuilder
                                .createScreenCaptureFromBase64String(
                                        screenshotData
                                )
                                .build()
                );
            }
        } catch (Exception e) {
            System.err.println(
                    "Unable to attach ADA failure screenshot: "
                            + e.getMessage()
            );
            test.warning("Failure screenshot could not be attached.");
        }

        System.out.println(testName + " failed on this attempt.");

        ExtentReport.removeExtentTest();
    }

    // ================= TEST SKIPPED =================

    @Override
    public void onTestSkipped(ITestResult result) {

        String key = getLogicalTestKey(result);
        finalResults.put(key, result);

        ExtentTest test = ExtentReport.getExtentTest();

        test.log(
                Status.SKIP,
                result.getMethod().getMethodName()
                        + " was skipped."
        );

        if (result.getThrowable() != null) {
            test.info(result.getThrowable());
        }

        System.out.println(
                result.getMethod().getMethodName() + " was skipped."
        );

        ExtentReport.removeExtentTest();
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(
            ITestResult result) {
        // Not used.
    }

    // ================= SUITE FINISH =================

    @Override
    public void onFinish(ITestContext context) {

        // Merge all TestNG outcomes, including retry attempts.
        collectResults(context.getPassedTests().getAllResults());
        collectResults(context.getFailedTests().getAllResults());
        collectResults(context.getSkippedTests().getAllResults());

        // Recalculate final counts from unique logical tests.
        Fpassed = 0;
        Ffailed = 0;
        Fskipped = 0;

        for (Map.Entry<String, ITestResult> entry
                : finalResults.entrySet()) {

            ITestResult result = entry.getValue();

            ExtentTest test = extentTests.get(entry.getKey());

            if (result.getStatus() == ITestResult.SUCCESS) {
                Fpassed++;

                if (test != null) {
                    test.getModel().setStatus(Status.PASS);
                }

            } else if (result.getStatus() == ITestResult.FAILURE) {
                Ffailed++;

                if (test != null) {
                    test.getModel().setStatus(Status.FAIL);
                }

            } else if (result.getStatus() == ITestResult.SKIP) {
                Fskipped++;

                if (test != null) {
                    test.getModel().setStatus(Status.SKIP);
                }
            }
        }

        System.out.println("========== ADA FINAL RESULTS ==========");
        System.out.println("Passed: " + Fpassed);
        System.out.println("Failed: " + Ffailed);
        System.out.println("Skipped: " + Fskipped);
        System.out.println("Total: " + (Fpassed + Ffailed + Fskipped));
        System.out.println("========================================");
    }

    // ================= EXECUTION FINISH =================

    @Override
    public void onExecutionFinish() {

        try {
            generateExtentReports();
            System.out.println("Extent report generated.");

        } catch (Exception e) {
            System.err.println("Failed to generate Extent report.");
            e.printStackTrace();
        }

        try {
            reportLink = com.base.GDrivePOST.UploadADAReport();

            if (reportLink == null || reportLink.isBlank()
                    || reportLink.toLowerCase().contains("failed")) {
                reportLink = UPLOAD_FAILED;
                System.err.println("ADA report upload failed.");
            } else {
                System.out.println("ADA Report uploaded: " + reportLink);
            }

        } catch (Exception e) {
            reportLink = UPLOAD_FAILED;
            System.err.println("ADA report upload failed.");
            e.printStackTrace();
        }

        // Preserve the existing behavior: send Slack only after upload.
        if (!UPLOAD_FAILED.equals(reportLink)) {
            try {
                sendSlackNotificationADA(
                        String.valueOf(Fpassed),
                        String.valueOf(Ffailed),
                        String.valueOf(Fskipped),
                        reportLink
                );
            } catch (Exception e) {
                System.err.println(
                        "Failed to send ADA Slack notification."
                );
                e.printStackTrace();
            }
        } else {
            System.err.println(
                    "ADA Slack notification skipped because report upload failed."
            );
        }

        // Prevent stale test references on later executions.
        ExtentReport.removeExtentTest();
    }

    // ================= HELPER METHODS =================

    private void collectResults(Set<ITestResult> results) {

        for (ITestResult result : results) {

            String key = getLogicalTestKey(result);

            finalResults.merge(
                    key,
                    result,
                    (existing, incoming) ->
                            getPriority(incoming.getStatus())
                                    > getPriority(existing.getStatus())
                                    ? incoming
                                    : existing
            );
        }
    }

    /*
     * When retries exist, PASS takes priority over FAILURE,
     * and FAILURE takes priority over SKIP.
     */
    private int getPriority(int status) {

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

    private String getLogicalTestKey(ITestResult result) {

        return result.getTestClass().getName()
                + "."
                + result.getMethod().getMethodName()
                + "|params="
                + Arrays.deepToString(result.getParameters())
                + "|instance="
                + System.identityHashCode(result.getInstance());
    }
}
