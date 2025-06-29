package playerquests.utility.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
    }

    public TestUtility(ClientDirector clientDirector) {
        this.clientDirector = clientDirector;
    }

    public List<TestResult> runTests() {
        Method[] methods = this.getClass().getDeclaredMethods();
        String className = this.getClass().getSimpleName();

        for (Method method : methods) {
            if ( ! method.isAnnotationPresent(PlayerQuestsTest.class)) {
                break;
            }

            PlayerQuestsTest annotation = method.getAnnotation(PlayerQuestsTest.class);
            String label = annotation.label().isEmpty() ? method.getName() : annotation.label();

            try {
                boolean result = (boolean) method.invoke(this);
                testResults.add(new TestResult(className, method.getName(), label, result, null));
            } catch (Exception e) {
                testResults.add(new TestResult(className, className, label, false, e.getCause()));
            }
        }

        return testResults;
    }
}
