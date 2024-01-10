package playerquests.annotations;

import static org.junit.jupiter.api.Assertions.assertTrue; // used to succeed test on the condition a value is true

import java.lang.reflect.Method; // programatically accessing methods on a class

import org.junit.jupiter.api.Test; // signals a method is to be run as a test

import playerquests.gui.GUI; // a class that uses the @Key annotation

public class AnnotationTest {

    /**
     * Checks to see if the @Key annotation is on a GUI class method.
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    @Test
    void isKeyAnnotationPresent() throws NoSuchMethodException, SecurityException {
        Method guiClassField = GUI.class.getMethod("setTitle", String.class); // get GUI.setTitle() method
        assertTrue(guiClassField.isAnnotationPresent(Key.class)); // check if the method has a @Key annotation
    }
}
