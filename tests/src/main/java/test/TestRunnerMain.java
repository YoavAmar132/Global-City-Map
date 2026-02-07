package test;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TestRunnerMain {
public static String SuccessMsg;
    private static final Map<String, String> successMessages =
            new ConcurrentHashMap<>();
    public static  String DB_PASS ;
    public static void setSuccessMsg(String testDisplayName, String message) {
        successMessages.put(testDisplayName, message);
    }
    private static void initialize() {


    }



    public static void main(String[] args) {

        System.out.println("=== GCM Test Runner ===");

        Launcher launcher = LauncherFactory.create();

        LauncherDiscoveryRequest request =
                LauncherDiscoveryRequestBuilder.request()
                        .selectors(
                                DiscoverySelectors.selectPackage("test.UnitTests")
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
                    r.errorMessage == null ? successMessages.getOrDefault(
                            r.displayName,
                            "OK"
                    ) : r.errorMessage

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

            String success = successMessages.get(id.getDisplayName());

            results.add(new TestResult(
                    id.getDisplayName(),
                    status,
                    error,
                    success
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
        final String successMessage;

        TestResult(String displayName, String status, String errorMessage,String successMessage) {
            this.displayName = displayName;
            this.status = status;
            this.errorMessage = errorMessage;
            this.successMessage = successMessage;
        }
    }


}
