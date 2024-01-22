package playerquests.product;

import playerquests.builder.quest.QuestBuilder; // the parent builder

/**
 * The Quest product containing all the information 
 * about a quest, ready to be played.
 */
public class Quest {

    /**
     * The builder used to manage and control the output Quest
     */
    private QuestBuilder builder;
    
    /**
     * Creates a quest instance for playing and viewing!
     * @param builder the quest builder which made this quest
     */
    public Quest(QuestBuilder builder) {
        this.builder = builder;
    }
}