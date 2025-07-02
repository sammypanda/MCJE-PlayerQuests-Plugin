package playerquests.utility.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // Used at runtime with reflection
@Target(ElementType.METHOD) // Only applied to methods
public @interface PlayerQuestsTest {
    /**
     * A human-readable label for the test case.
     * If not specified, the method name will be used.
     * @return the test label
     */
    String label() default "";
    
    /**
     * Optional priority for test execution order (lower numbers execute first).
     * @return the test priority
     */
    int priority() default 100;
}
