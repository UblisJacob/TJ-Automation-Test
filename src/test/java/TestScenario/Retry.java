
package TestScenario;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class Retry implements IRetryAnalyzer {

	private int retryCount = 0;

	private static final int maxRetryCount = 1;

	@Override
	public boolean retry(ITestResult result) {

		if (retryCount < maxRetryCount) {

			retryCount++;

			System.out.println(
					"Retrying test: " + result.getName()
							+ " | Previous status: "
							+ getResultStatusName(result.getStatus())
							+ " | Retry attempt: "
							+ retryCount
							+ " of "
							+ maxRetryCount
			);

			return true;
		}

		System.out.println(
				"Retry limit reached for test: "
						+ result.getName()
						+ " | Total retries: "
						+ retryCount
		);

		return false;
	}

	private String getResultStatusName(int status) {

		switch (status) {

			case ITestResult.SUCCESS:
				return "SUCCESS";

			case ITestResult.FAILURE:
				return "FAILURE";

			case ITestResult.SKIP:
				return "SKIP";

			default:
				return "UNKNOWN";
		}
	}
}
