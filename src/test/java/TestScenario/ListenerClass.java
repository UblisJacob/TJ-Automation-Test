
package TestScenario;

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

import static com.base.Slack.sendSlackNotificationReg;

public class ListenerClass extends ExtentReport
		implements ITestListener, IExecutionListener {

	/*
	 * One Extent test per logical test invocation.
	 */
	private static final Map<String, ExtentTestHolder> extentTests =
			new ConcurrentHashMap<>();

	/*
	 * Stores the final outcome of each logical test.
	 */
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

	public static String reportLink = "Report upload failed";

	public int Fpassed = 0;
	public int Ffailed = 0;
	public int Fskipped = 0;

	private static class ExtentTestHolder {
		final com.aventstack.extentreports.ExtentTest test;

		ExtentTestHolder(
				com.aventstack.extentreports.ExtentTest test) {
			this.test = test;
		}
	}

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
		reportLink = "Report upload failed";

		initializeExtentReports();

		System.out.println("=================================");
		System.out.println("Test execution started");
		System.out.println("=================================");
	}

	@Override
	public void onStart(ITestContext context) {

		suiteName = context.getSuite().getName();
		browserName =
				context.getCurrentXmlTest().getParameter("browser");

		System.out.println("=================================");
		System.out.println("Suite Name: " + suiteName);
		System.out.println("Test Context: " + context.getName());
		System.out.println("Browser: " + browserName);
		System.out.println("=================================");

		if (sparkReporter_AllTests != null) {
			if ("ADACheck Test Suite".equals(suiteName)) {
				sparkReporter_AllTests.config()
						.setReportName("ADACheck Test Report");
			} else {
				sparkReporter_AllTests.config()
						.setReportName("Regression Test Report");
			}
		}
	}

	/*
	 * Identifies a test using its class, method, parameters,
	 * and test instance.
	 *
	 * Retry attempts should reuse this key.
	 */
	private String getTestKey(ITestResult result) {

		return result.getTestClass().getName()
				+ "."
				+ result.getMethod().getMethodName()
				+ "|params="
				+ Arrays.deepToString(result.getParameters())
				+ "|instance="
				+ System.identityHashCode(result.getInstance());
	}

	private com.aventstack.extentreports.ExtentTest getExtentTest(
			ITestResult result) {

		String key = getTestKey(result);

		ExtentTestHolder holder = extentTests.computeIfAbsent(
				key,
				k -> {
					synchronized (extentReport) {
						return new ExtentTestHolder(
								extentReport.createTest(
										result.getName()
								)
						);
					}
				}
		);

		testNames.put(key, result.getName());

		return holder.test;
	}


	@Override
	public void onTestStart(ITestResult result) {

		com.aventstack.extentreports.ExtentTest test =
				getExtentTest(result);

		// Set the ExtentTest for the current execution thread.
		ExtentReport.setExtentTest(test);

		test.info("Executing test attempt");

		System.out.println(
				"Executing: " + result.getName()
						+ " | Thread: "
						+ Thread.currentThread().getName()
		);
	}


	@Override
	public void onTestSuccess(ITestResult result) {

		com.aventstack.extentreports.ExtentTest test =
				getExtentTest(result);

		/*
		 * Do not finalize status here.
		 * Final status is determined after retries finish.
		 */
		test.info("Attempt completed successfully");

		System.out.println(
				"Test attempt passed: " + result.getName()
		);

		ExtentReport.removeExtentTest();
	}

	@Override
	public void onTestFailure(ITestResult result) {

		com.aventstack.extentreports.ExtentTest test =
				getExtentTest(result);

		/*
		 * An intermediate retry failure is informational.
		 */
		test.log(
				Status.INFO,
				"Attempt failed; awaiting final retry outcome."
		);

		String key = getTestKey(result);

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
					"Screenshot capture failed: "
							+ e.getMessage()
			);
		}

		System.out.println(
				"Test attempt failed: " + result.getName()
						+ " | Awaiting final retry outcome"
		);

		ExtentReport.removeExtentTest();
	}

	@Override
	public void onTestSkipped(ITestResult result) {

		com.aventstack.extentreports.ExtentTest test =
				getExtentTest(result);

		test.info(
				"Attempt skipped; final outcome will be determined "
						+ "after retries."
		);

		if (result.getThrowable() != null) {
			test.log(Status.INFO, result.getThrowable());
		}

		System.out.println(
				"Test attempt skipped: " + result.getName()
		);

		ExtentReport.removeExtentTest();
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(
			ITestResult result) {
		// Final results are handled during context aggregation.
	}

	/*
	 * Final result priority:
	 * SUCCESS > FAILURE > SKIP
	 *
	 * A successful retry overrides an earlier failure.
	 */
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
			Map<String, Integer> suiteResults,
			ITestResult result,
			int status) {

		String key = getTestKey(result);

		suiteResults.merge(
				key,
				status,
				(oldStatus, newStatus) ->
						resultPriority(newStatus)
								> resultPriority(oldStatus)
								? newStatus
								: oldStatus
		);

		testNames.put(key, result.getName());

		/*
		 * Retain the throwable for a final failure.
		 */
		if (status == ITestResult.FAILURE
				&& result.getThrowable() != null) {

			finalErrors.put(key, result.getThrowable());
		}
	}

	@Override
	public void onFinish(ITestContext context) {

		Map<String, Integer> suiteResults =
				new ConcurrentHashMap<>();

		/*
		 * Print the raw TestNG result collection sizes.
		 * These are diagnostic values, not the final counts.
		 */
		System.out.println("=================================");
		System.out.println("Finished Test Context: "
				+ context.getName());
		System.out.println("Raw passed results: "
				+ context.getPassedTests().size());
		System.out.println("Raw failed results: "
				+ context.getFailedTests().size());
		System.out.println("Raw skipped results: "
				+ context.getSkippedTests().size());
		System.out.println("=================================");

		for (ITestResult result :
				context.getPassedTests().getAllResults()) {

			collectResult(
					suiteResults,
					result,
					ITestResult.SUCCESS
			);
		}

		for (ITestResult result :
				context.getFailedTests().getAllResults()) {

			collectResult(
					suiteResults,
					result,
					ITestResult.FAILURE
			);
		}

		for (ITestResult result :
				context.getSkippedTests().getAllResults()) {

			collectResult(
					suiteResults,
					result,
					ITestResult.SKIP
			);
		}

		/*
		 * Merge the logical test outcomes into the execution-wide map.
		 */
		suiteResults.forEach((key, status) ->
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

		/*
		 * Apply final statuses to Extent exactly once per logical test.
		 */
		for (Map.Entry<String, Integer> entry :
				suiteResults.entrySet()) {

			String key = entry.getKey();
			int status = entry.getValue();

			ExtentTestHolder holder = extentTests.get(key);

			if (holder == null) {
				continue;
			}

			String name = testNames.getOrDefault(key, key);

			if (status == ITestResult.SUCCESS) {

				holder.test.pass(
						name + " passed after retries, if any."
				);

			} else if (status == ITestResult.FAILURE) {

				Throwable error = finalErrors.get(key);

				if (error != null) {
					holder.test.fail(error);
				} else {
					holder.test.fail(
							name + " failed after retries."
					);
				}

			} else if (status == ITestResult.SKIP) {

				holder.test.skip(name + " was skipped.");
			}
		}

		System.out.println(
				"Unique logical outcomes in context: "
						+ suiteResults.size()
		);

		System.out.println(
				"Execution-wide logical outcomes: "
						+ finalResults.size()
		);

		System.out.println(
				"Finished TestNG context: " + context.getName()
		);
	}

	@Override
	public void onExecutionFinish() {

		/*
		 * Count final logical outcomes once.
		 */
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
		System.out.println("FINAL REGRESSION RESULTS");
		System.out.println("Passed: " + Fpassed);
		System.out.println("Failed: " + Ffailed);
		System.out.println("Skipped: " + Fskipped);
		System.out.println("Unique final test count: "
				+ finalResults.size());
		System.out.println("=================================");

		/*
		 * Flush and generate the Extent report before uploading.
		 */
		generateExtentReports();

		/*
		 * Upload the final report.
		 */
		try {

			reportLink = GDrivePOST.UploadReport();

			System.out.println(
					"Report uploaded to Drive: " + reportLink
			);

		} catch (Exception e) {

			reportLink = "Report upload failed";

			System.err.println("Drive upload failed.");
			e.printStackTrace();
		}

		/*
		 * Send the same final counts to Slack.
		 */
		try {

			sendSlackNotificationReg(
					String.valueOf(Fpassed),
					String.valueOf(Ffailed),
					String.valueOf(Fskipped),
					reportLink
			);

			System.out.println(
					"Slack notification completed."
			);

		} catch (Exception e) {

			System.err.println(
					"Slack notification failed."
			);

			e.printStackTrace();
		}
	}
}
