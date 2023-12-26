package playerquests;

import static org.mockito.Mockito.when; // used to mock return values

import java.util.logging.Level; // the type of severity used with the Logger dependency
import java.util.logging.Logger; // required for the Bukkit Server

import org.bukkit.Bukkit; // used to set the Server which is a type required in many places of a bukkit plugin
import org.bukkit.Server; // type required in many places for testing a bukkit plugin
import org.junit.jupiter.api.BeforeAll; // annotation that runs the code "before all" other tests
import org.junit.jupiter.api.TestInstance; // control of the test lifecycle, see {@link #BukkitTestUtil()}
import org.mockito.Mock; // annotation for a mock java object
import org.mockito.MockitoAnnotations; // bridge between local mocks and class-level mocks

/**
 * Utility class for setting up a mocked Bukkit server instance for testing purposes.
 * <p>
 * This class is annotated with {@code @TestInstance(TestInstance.Lifecycle.PER_CLASS)}, meaning
 * that there is one instance with shared values. This is efficient but can cause issues for 
 * tests in cases where we want test independence.
 * </p>
 * <p>
 * The {@link #setupServer()} method is annotated with {@code @BeforeAll} and initializes the server setup
 * before running any test methods. The mocked Server is set with {@code Bukkit.setServer(s)} for
 * other tests to use.
 * </p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BukkitTestUtil {

    @Mock
    private Server s;

    /**
     * Initializes the server setup before running all tests.
     * <p>
     * This method is annotated with {@code @BeforeAll}, meaning it'll be executed before
     * any other test methods in the test class.
     * The mocked Server instance is then set using {@code Bukkit.setServer(s)}.
     * </p>
     * <p>
     * The mocked behaviors include getLogger(), isPrimaryThread(), getName(),
     * getVersion(), getBukkitVersion().
     * </p>
     */
    @BeforeAll
    public void setupServer() {
        // Gives access from this function to class-level mock values 
        // for example: variable s
        MockitoAnnotations.openMocks(this);
        
        // Building the logger required for the Bukkit Server
        Logger logger = Logger.getLogger(getClass().getCanonicalName());
        logger.setLevel(Level.WARNING);

        // Mocking behavior for the Server with variable s
        when(s.getLogger()).thenReturn(logger);

        // Sets the server singleton for other tests
        Bukkit.setServer(s);
    }
}
