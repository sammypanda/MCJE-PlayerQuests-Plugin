package playerquests.quest.builder;

import java.nio.file.Path; // path to the quest template file

/**
 * Class for instantiating a quest from a JSON template.
 */
public class QuestLoader {
    
    /**
     * Loads a QuestBuilder from a JSON template.
     * @param templateAsJson quest template string.
     */
    public QuestLoader(String templateAsJson) {
        this.parseTemplate(templateAsJson);
    }

    /**
     * Loads a QuestBuilder from a template file (JSON).
     * @see #QuestLoader(String)
     * @param templateAsFile quest template json file path.
     */
    public QuestLoader(Path templateAsFile) {
        // templateAsJson = get the file, extract the json string
        // this(templateAsJson);
    }

    /**
     * Interprets the JSON object and creates an instance of the QuestBuilder.
     * @param jsonString quest template json object string.
     * @return an instance of the quest builder class with the template options already set.
     */
    public QuestBuilder parseTemplate(String jsonString) {
        // creates a questbuilder from a quest template
            // undecided if it's important that it sets the file location as the same as the original

        return new QuestBuilder("mock"); // just returns a mock QuestBuilder for now
    }
}
