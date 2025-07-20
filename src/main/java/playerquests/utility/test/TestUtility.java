package playerquests.utility.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import playerquests.client.ClientDirector;
import playerquests.utility.annotation.PlayerQuestsTest;

public abstract class TestUtility {
    protected ClientDirector clientDirector;
    protected List<TestResult> testResults = new ArrayList<>();

    public static class TestResult {
        public final String className;
        public final String testName;
        public final String testLabel;
        public final boolean didTestPass;
        public final Throwable testError;

        public TestResult(String className, String testName, String testLabel, boolean didTestPass, Throwable testError) {
            this.className = className;
            this.testName = testName;
            this.testLabel = testLabel;
            this.didTestPass = didTestPass;
            this.testError = testError;
        }

        @Override
        public String toString() {
            return String.format("(%s) %s: %s - %s (%s)", 
                className, 
                testName,
                testLabel,
                didTestPass,
                testError
            );
        }
    }

    protected TestUtility(ClientDirector clientDirector) {
        this.clientDirector = clientDirector;
    }

    public void runTests(
        Consumer<TestResult> onEach, 
        Consumer<List<TestResult>> onSummary
    ) {
        // resolve PlayerQuestsTest annotated methods into an ordered list
        LinkedList<Method> remainingMethods = new LinkedList<>(
            Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(PlayerQuestsTest.class))
                .toList()
        );

        // use the ordered list to start a self-managed recursion
        manageTests(new ArrayList<>(), remainingMethods, onEach, onSummary);
    }

    private void manageTests(
        List<TestResult> completed,
        LinkedList<Method> remainingMethods,
        Consumer<TestResult> onEach,
        Consumer<List<TestResult>> onSummary
    ) {
        // if no more methods, then all tests done: do a summise and exit
        if (remainingMethods.isEmpty()) {
            onSummary.accept(completed);
            return;
        }

        // otherwise, pop next method for invoking
        Method testMethod = remainingMethods.pop();

        // collect details
        PlayerQuestsTest annotation = testMethod.getAnnotation(PlayerQuestsTest.class);
        String className = testMethod.getDeclaringClass().getSimpleName();
        String testName = testMethod.getName();
        String testLabel = annotation.label().isEmpty() ? testMethod.getName() : annotation.label();

        // try invoking
        try {
            // if the method is not a CompletableFuture, goto next and exit this
            if ( testMethod.getReturnType() != CompletableFuture.class ) {
                return;
            }

            // otherwise, invoke the test
            @SuppressWarnings("unchecked") // is already checked
            CompletableFuture<Boolean> futureResult = (CompletableFuture<Boolean>) testMethod.invoke(this);

            // attach test resolving for when test done
            futureResult.whenComplete((result, throwable) -> {
                Boolean didTestPass = throwable == null && result;
                TestResult testResult = new TestResult(
                    className, 
                    testName, 
                    testLabel, 
                    didTestPass, 
                    throwable != null ? throwable.getCause() : null
                );
                completed.add(testResult);
                onEach.accept(testResult);
                manageTests(completed, remainingMethods, onEach, onSummary);
            });

        // catch if invoking failed
        } catch (Exception e) {
            // resolve test as failed
            TestResult testResult = new TestResult(
                className, 
                testName, 
                testLabel, 
                false, 
                e.getCause()
            );
            completed.add(testResult);
            onEach.accept(testResult);
            manageTests(completed, remainingMethods, onEach, onSummary);
        }
    }
}
