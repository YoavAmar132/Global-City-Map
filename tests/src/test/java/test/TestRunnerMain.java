package test;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.util.ArrayList;
import java.util.List;

public class TestRunnerMain {

    public static void main(String[] args) {
        System.out.println("=== GCM Test Runner ===");

        Launcher launcher = LauncherFactory.create();

        LauncherDiscoveryRequest request =
                LauncherDiscoveryRequestBuilder.request()
                        .selectors(
                                DiscoverySelectors.selectPackage("test")
                        )
                        .build();

        TestResultCollector listener = new TestResultCollector();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        printResults(listener.results);

        if (listener.hasFailures()) {
            System.out.println("\n❌ TESTS FAILED");
            System.exit(1);
        } else {
            System.out.println("\n✅ ALL TESTS PASSED");
            System.exit(0);
        }
    }

    private static void printResults(List<TestResult> results) {
        System.out.println();
        System.out.printf("%-50s | %-6s | %s%n", "TEST", "STATUS", "ERROR");
        System.out.println("-".repeat(100));

        for (TestResult r : results) {
            System.out.printf(
                    "%-50s | %-6s | %s%n",
                    r.displayName,
                    r.status,
                    r.errorMessage == null ? "" : r.errorMessage
            );
        }
    }

    /* ======================= SUPPORT CLASSES ======================= */

    static class TestResultCollector implements TestExecutionListener {

        final List<TestResult> results = new ArrayList<>();

        @Override
        public void executionFinished(TestIdentifier id,
                                      TestExecutionResult result) {
            if (!id.isTest()) return;

            String status = switch (result.getStatus()) {
                case SUCCESSFUL -> "PASS";
                case FAILED -> "FAIL";
                case ABORTED -> "SKIP";
            };

            String error = result.getThrowable()
                    .map(Throwable::getMessage)
                    .orElse(null);

            results.add(new TestResult(
                    id.getDisplayName(),
                    status,
                    error
            ));
        }

        boolean hasFailures() {
            return results.stream().anyMatch(r -> r.status.equals("FAIL"));
        }
    }

    static class TestResult {
        final String displayName;
        final String status;
        final String errorMessage;

        TestResult(String displayName, String status, String errorMessage) {
            this.displayName = displayName;
            this.status = status;
            this.errorMessage = errorMessage;
        }
    }
}
