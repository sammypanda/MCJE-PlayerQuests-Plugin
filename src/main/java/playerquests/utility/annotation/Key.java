package playerquests.utility.annotation;

import java.lang.annotation.Retention; // controls annotation retention rules
import java.lang.annotation.RetentionPolicy; // used to set when the annotation is accessible

/**
 * Interface for annotating a key (identifier) on methods.
 * <p>
 * This key annotation is used as a proxy for the 
 * setter. Facilitates use of a key-value pair pattern.
 */
@Retention(RetentionPolicy.RUNTIME) // allows accessing this annotation during runtime
public @interface Key {

    /**
     * The actual key name to identify the subject by.
     * @return key identifier as string
     */
    String value();

}
