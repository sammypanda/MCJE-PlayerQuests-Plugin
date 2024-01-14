package playerquests.gui.function;

/**
 * Fully closes the GUI window.
 */
public class CloseScreen extends GUIFunction {

    /**
     * Calls the close() method in the GUI class.
     */
    @Override
    public void execute() {
        this.parentGui.close();
    }
}