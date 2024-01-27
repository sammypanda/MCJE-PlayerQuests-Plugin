package playerquests.builder.quest.component.action.type;

import java.util.ArrayList;

public class Speak extends ActionType {
    
    /**
     * Produces dialogue from an NPC.
     * @param params the list of parameters for a function 
     */
    public Speak(ArrayList<Object> params) {
        super(params);
    }

    /**
     * Produces dialogue from an NPC.
     * <p>
     * Assumes an empty param list.
     */ 
    public Speak() {
        super(new ArrayList<>());
    }

    @Override
    public String toString() {
        return "Speak";
    }
}
