package playerquests.gui.function;

import org.bukkit.Bukkit; // used to broadcast a message if this meta action is used

/**
 * Just a simple tester class to validate if your GUI template/meta action is working.
 */
public class Test extends GUIFunction {

    /**
     * Sends a message in the console and chat to notify that this test executed.
     */
    @Override
    public void execute() {
        System.out.println("(the testing Meta Action was reached.)");
        Bukkit.broadcastMessage("test function from GUI");
    }
    
}
