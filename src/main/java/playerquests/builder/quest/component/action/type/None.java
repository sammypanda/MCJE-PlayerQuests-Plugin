package playerquests.builder.quest.component.action.type;

import java.util.ArrayList;

/**
 * Skips the quest action.
 */
public class None extends ActionType {
    
    /**
     * Skips the currently tasked quest action.
     * <p>
     * Takes an empty param list.
     * @param params the list of parameters for a function 
     */
    public None(ArrayList<Object> params) {
        super(params);
    }

    /**
     * Skips the currently tasked quest action.
     * <p>
     * Assumes an empty param list.
     */ 
    public None() {
        super(new ArrayList<>());
    }

    @Override
    public String toString() {
        return "None";
    }
}
