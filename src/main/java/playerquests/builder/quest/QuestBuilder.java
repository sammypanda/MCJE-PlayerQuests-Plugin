package playerquests.builder.quest;

import java.io.IOException; // thrown if a file cannot be created
import java.util.HashMap; // hash table map type
import java.util.Map; // generic map type

import com.fasterxml.jackson.annotation.JsonProperty; // for declaring a field as a json property
import com.fasterxml.jackson.core.JsonProcessingException; // thrown when json cannot serialise
import com.fasterxml.jackson.databind.ObjectMapper; // turns objects into json
import com.fasterxml.jackson.databind.SerializationFeature; // configures json serialisation 

import playerquests.Core; // gets the KeyHandler singleton
import playerquests.builder.gui.GUIBuilder; // for modifying the GUI
import playerquests.builder.gui.component.GUIFrame; // modifying the outer frame of the GUI
import playerquests.builder.quest.component.QuestStage; // object for quest stages
import playerquests.client.ClientDirector; // abstractions for plugin functionality
import playerquests.product.Quest; // quest product class
import playerquests.utility.ChatUtils; // sends message in-game
import playerquests.utility.FileUtils; // creates files
import playerquests.utility.annotation.Key; // to associate a key name with a method

/**
 * For creating and managing a Quest.
 */
public class QuestBuilder {

    /**
     * Used to access plugin functionality.
     */
    private ClientDirector director;

    /**
     * The title of the quest.
     */
    @JsonProperty("title")
    private String title = ""; // default quest title

    /**
     * Entry point for the quest.
     */
    private QuestStage entryPoint = new QuestStage(0); // default entry point as first stage (stage_0)

    /**
     * List of the quest stages and actions.
     */
    @JsonProperty("stages")
    private Map<String, QuestStage> questPlan = new HashMap<String, QuestStage>();

    /**
     * Where quests are saved.
     */
    private String savePath = "quest/templates/";

    /**
     * The quest product.
     */
    private Quest quest = new Quest(this);

    /**
     * Operations to run whenever the class is instantiated.
     */
    {
        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this);

        // add default entry point stage to questPlan map
        this.questPlan.put(this.entryPoint.getID(), this.entryPoint);
    }

    /**
     * Returns a new default Quest.
     * @param director used to control the plugin
     */
    public QuestBuilder(ClientDirector director) {
        this.director = director;
    }

    /**
     * Title for the quest.
     * <p>
     * Also used as the ID: [Title]_[Owner Player ID]
     * @param title the name for the quest
     */
    @Key("quest.title")
    public void setTitle(String title) {
        String oldTitle = " (" + this.title + ")"; // store the old title to identify later
        String newTitle = " (" + title + ")";

        this.title = title; // set the new title

        GUIBuilder guiBuilder = (GUIBuilder) this.director.getCurrentInstance(GUIBuilder.class); // get the GUI builder
        GUIFrame frame = guiBuilder.getFrame(); // get the GUI frame

        if (guiBuilder.getResult() != null) {
            if (frame.getTitle().contains(oldTitle)) {
                String fullTitle = frame.getTitle().replace(oldTitle, newTitle); // replace the part we appended only
                frame.setTitle(fullTitle); // re-submit
            } else {
                frame.setTitle(frame.getTitle() + newTitle); // submit for the first time
            }
            
            guiBuilder.getResult().draw(); // ask the changes to update in the GUI
        }
    }

    /**
     * Creates a quest template based on the current state of the builder.
     * @return this quest as a json object
     * @throws JsonProcessingException when the json cannot seralise
     */
    private String getTemplateString() throws JsonProcessingException {
        // serialises an object into json
        ObjectMapper jsonObjectMapper = new ObjectMapper();

        // configure the mapper
        jsonObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // allow json object to be empty

        // present this quest builder as a template json string (prettied)
        return jsonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }

    /**
     * Gets the entry point ID.
     * <p>
     * Either an action or a stage.
     * @return the string representation for the entry point
     */
    @JsonProperty("entry")
    public String getEntryPointString() {
        return this.entryPoint.getID();
    }

    /**
     * Saves a quest into the QuestBuilder.savePath.
     * @return the response message
     * @throws IllegalArgumentException when saving is not safe/possible
     */
    @Key("quest")
    public String save() throws IllegalArgumentException {
        if (this.title.contains("_")) {
            throw new IllegalArgumentException("Quest name '" + this.title + "' not allowed underscores.");
        }

        try {
            FileUtils.create( // create the template json file
                this.savePath + this.title + "_" + this.director.getPlayer().getUniqueId().toString() + ".json", // name pattern
                getTemplateString().getBytes() // put the content in the file
            );
        } catch (IOException e) {
            ChatUtils.sendError(this.director.getPlayer(), e.getMessage(), e);
            return "Quest Builder: '" + this.title + "' could not save.";
        }

        return "Quest Builder: '" + this.title + "' was saved";
    }
}