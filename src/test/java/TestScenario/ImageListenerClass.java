
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.base.Slack.sendSlackNotificationImageCSV;

public class ImageListenerClass extends ExtentReport
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

    private static final Set<String> passedTests =
            ConcurrentHashMap.newKeySet();

    private static final Set<String> failedTests =
            ConcurrentHashMap.newKeySet();

    private static final Set<String> skippedTests =
            ConcurrentHashMap.newKeySet();

    private static volatile String browserName;
    private static volatile String suiteName;

    public static String reportLink = "Image report upload failed";

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

        passedTests.clear();
        failedTests.clear();
        skippedTests.clear();

        Fpassed = 0;
        Ffailed = 0;
        Fskipped = 0;

        reportLink = "Image report upload failed";

        initializeExtentReports();

        System.out.println("=================================");
        System.out.println("Image test execution started");
        System.out.println("=================================");
    }

    // ================= SUITE START =================

    @Override
    public void onStart(ITestContext context) {
        suiteName = context.getSuite().getName();
        browserName = context.getCurrentXmlTest()
                .getParameter("browser");

        System.out.println("Suite: " + suiteName);
        System.out.println("Browser: " + browserName);

        if (sparkReporter_AllTests != null) {
            if ("Image Test Suite".equals(suiteName)) {
                sparkReporter_AllTests.config()
                        .setReportName("Images Missing Alt Text");
            } else {
                sparkReporter_AllTests.config()
                        .setReportName("Regression Test Report");
            }
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

        test.info("Executing test attempt");

        System.out.println(
                "Executing: " + result.getName()
                        + " | Thread: "
                        + Thread.currentThread().getName()
        );
    }

    // ================= SUCCESS =================

    @Override
    public void onTestSuccess(ITestResult result) {
        try {
            ExtentTest test = getOrCreateExtentTest(result);

            // Final status is assigned after TestNG finishes the context.
            test.info("Attempt completed successfully");

            System.out.println(
                    "Image test attempt passed: " + result.getName()
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
                    "Image test attempt failed: " + result.getName()
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
                    "Image test attempt skipped: " + result.getName()
            );
        } finally {
            ExtentReport.removeExtentTest();
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(
            ITestResult result) {
        // Final outcomes are aggregated when the context finishes.
    }

    // ================= RESULT PRIORITY =================

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
        System.out.println("Finished Image context: " + context.getName());
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
                    contextResults,
                    result,
                    ITestResult.SUCCESS
            );
        }

        for (ITestResult result :
                context.getFailedTests().getAllResults()) {
            collectResult(
                    contextResults,
                    result,
                    ITestResult.FAILURE
            );
        }

        for (ITestResult result :
                context.getSkippedTests().getAllResults()) {
            collectResult(
                    contextResults,
                    result,
                    ITestResult.SKIP
            );
        }

        // Merge final outcomes across TestNG contexts.
        contextResults.forEach((key, status) ->
                finalResults.merge(
                        key,
                        status,
                        (oldStatus, newStatus) ->
                                resultPriority(newStatus) > resultPriority(oldStatus)
                                        ? newStatus
                                        : oldStatus
                )
        );

        // Apply the final Extent status once per logical test.
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

        passedTests.clear();
        failedTests.clear();
        skippedTests.clear();

        // Build Slack test-name sets from final outcomes, not attempts.
        for (Map.Entry<String, Integer> entry :
                finalResults.entrySet()) {

            String key = entry.getKey();
            int status = entry.getValue();
            String name = testNames.getOrDefault(key, key);

            if (status == ITestResult.SUCCESS) {
                Fpassed++;
                passedTests.add(name);

            } else if (status == ITestResult.FAILURE) {
                Ffailed++;
                failedTests.add(name);

            } else if (status == ITestResult.SKIP) {
                Fskipped++;
                skippedTests.add(name);
            }
        }

        System.out.println("=================================");
        System.out.println("FINAL IMAGE TEST RESULTS");
        System.out.println("Passed: " + Fpassed);
        System.out.println("Failed: " + Ffailed);
        System.out.println("Skipped: " + Fskipped);
        System.out.println(
                "Unique final test count: " + finalResults.size()
        );
        System.out.println("=================================");

        // Flush Extent before uploading the report.
        try {
            generateExtentReports();
        } catch (Exception e) {
            System.err.println("Image Extent report generation failed.");
            e.printStackTrace();
        }

        // Upload report and CSV files.
        Map<String, String> links = new HashMap<>();

        try {
            links = GDrivePOST.UploadImageReportAndCSV();

            if (links == null) {
                links = new HashMap<>();
            }

            reportLink = links.getOrDefault(
                    "reportLink",
                    "Image report upload failed"
            );

            System.out.println("Image report upload result: " + reportLink);

        } catch (Exception e) {
            reportLink = "Image report upload failed";

            System.err.println("Image report and CSV upload failed.");
            e.printStackTrace();
        }

        // Send the final counts and image CSV links to Slack.
        try {
            sendSlackNotificationImageCSV(
                    String.valueOf(Fpassed),
                    String.valueOf(Ffailed),
                    String.valueOf(Fskipped),
                    reportLink,
                    links.get("homepageCsvLink"),
                    links.get("plpCsvLink"),
                    links.get("pdpCsvLink"),
                    new HashSet<>(passedTests),
                    new HashSet<>(failedTests),
                    new HashSet<>(skippedTests)
            );

            System.out.println("Image Slack notification completed.");

        } catch (Exception e) {
            System.err.println("Image Slack notification failed.");
            e.printStackTrace();
        }
    }
}
