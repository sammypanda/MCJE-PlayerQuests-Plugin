package playerquests.gui.dynamic;

/**
 * TODO: Shows the list of quests associated with this player.
 * <p>
 * TODO: Includes quests associated with null.
 */
public class Dynamicmyquests extends GUIDynamic {

    /**
     * Reports to the user that this point has been reached.
     */
    @Override
    public void execute() {
        this.player.sendMessage("Asked for dynamic 'myquest' screen");
    }
}
