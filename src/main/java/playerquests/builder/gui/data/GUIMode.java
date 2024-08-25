package playerquests.builder.gui.data;

/**
 * Enum representing the different modes available in the GUI.
 * <p>
 * The {@code GUIMode} enum defines the modes used within the graphical user interface (GUI)
 * of the PlayerQuests plugin. Each mode represents a specific functionality or behavior
 * within the GUI.
 * </p>
 */
public enum GUIMode {
    /**
     * Mode for handling clicks within the GUI.
     * <p>
     * This mode is used when the GUI is in a state where user interactions are based
     * on clicking elements. It is typically used for selecting or interacting with GUI
     * components directly.
     * </p>
     */
    CLICK,

    /**
     * Mode for arranging elements within the GUI.
     * <p>
     * This mode is used when the GUI is in a state where elements need to be arranged
     * or organized. It is typically used for layout management or configuring the
     * arrangement of GUI components.
     * </p>
     */
    ARRANGE
}
