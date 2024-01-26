package playerquests.builder.quest.component.action.type;

public class Speak extends ActionType {
    
    /**
     * Not intended to be created directly.
     * <p>
     * Produces dialogue from an NPC.
     */
    public Speak() {
        super();
    }

    @Override
    public String toString() {
        return "Speak";
    }
}
