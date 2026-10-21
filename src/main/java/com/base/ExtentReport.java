
package com.base;

import java.awt.Desktop;
import java.io.File;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ExtentReport {

    public static ExtentReports extentReport;
    public static ExtentSparkReporter sparkReporter_AllTests;

    /*
     * Each test thread has its own ExtentTest reference.
     */
    private static final ThreadLocal<ExtentTest> currentExtentTest =
            new ThreadLocal<>();

    public static void setExtentTest(ExtentTest test) {
        currentExtentTest.set(test);
    }

    public static boolean hasExtentTest() {
        return currentExtentTest.get() != null;
    }

    public static ExtentTest getExtentTest() {
        ExtentTest test = currentExtentTest.get();

        if (test == null) {
            throw new IllegalStateException(
                    "ExtentTest is not initialized for thread: "
                            + Thread.currentThread().getName()
                            + ". Ensure ListenerClass.onTestStart() "
                            + "sets the current ExtentTest."
            );
        }

        return test;
    }

    public static void removeExtentTest() {
        currentExtentTest.remove();
    }

    public static void initializeExtentReports() {

        extentReport = new ExtentReports();

        File reportDir = new File("Report");

        if (!reportDir.exists() && !reportDir.mkdirs()) {
            throw new IllegalStateException(
                    "Unable to create report directory: "
                            + reportDir.getAbsolutePath()
            );
        }

        sparkReporter_AllTests =
                new ExtentSparkReporter(
                        new File(reportDir, "index.html")
                );

        sparkReporter_AllTests.config()
                .setTheme(Theme.DARK);

        extentReport.attachReporter(sparkReporter_AllTests);
    }

    public static void generateExtentReports() {

        if (extentReport != null) {
            extentReport.flush();
        }

        // Open the report locally, but not in CI.
        if (System.getenv("CI") == null) {
            try {
                File reportFile = new File("Report/index.html");

                if (reportFile.exists()) {
                    Desktop.getDesktop().browse(
                            reportFile.toURI()
                    );
                } else {
                    System.out.println(
                            "Report file not found: "
                                    + reportFile.getAbsolutePath()
                    );
                }

            } catch (Exception e) {
                System.out.println(
                        "Failed to open report automatically."
                );
                e.printStackTrace();
            }
        }
    }
}
