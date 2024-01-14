package playerquests.gui.dynamic;

import java.io.File; // retrieve the template files
import java.io.IOException; // thrown when the quest templates dir cannot be found
import java.nio.file.Files; // manage multiple template files
import java.util.ArrayList; // stores the quests this player owns
import java.util.Arrays; // working with literal arrays
import java.util.List; // store temporary lists (like: string splitting)

import playerquests.Core; // fetching Singletons (like: Plugin)

/**
 * TODO: Shows the list of quests associated with this player.
 * <p>
 * Includes quests associated with null.
 */
public class Dynamicmyquests extends GUIDynamic {

    /**
     * Reports to the user that this point has been reached.
     */
    @Override
    public void execute() {
        this.player.sendMessage("Asked for dynamic 'myquest' screen");

        // exit early if the Plugin is not instantiated
        // -- this is because the process relies on plugin data (files in the server /plugins dir)
        if (Core.getPlugin() == null) {
            throw new RuntimeException("<Could not execute myquests dynamic GUI without final Plugin>");
        }

        // get the list of all quest templates with owner: null or owner: player UUID
        File questTemplatesDir = new File(Core.getPlugin().getDataFolder(), "/quest/templates");
        ArrayList<String> myquestTemplates = new ArrayList<>(); // the quest templates this player owns

        try {
            Files.walk(questTemplatesDir.toPath()).forEach(questTemplateFile -> { // get all the quest templates
                // skip if is a directory
                if (questTemplateFile.toFile().isDirectory()) { return; }

                // the quest template filename without .json
                String questTemplateName = questTemplateFile.toString()
                    .replace(".json", "")
                    .split("/templates/")[1];

                // divide up to uncover the concatenated data in the quest template filename
                List<String> questTemplateNameFragments = Arrays.asList(
                    questTemplateName.split("_")
                );

                // list of acceptable quest owners
                List<String> questOwnerList = Arrays.asList(
                    "null", 
                    this.player.getUniqueId().toString()
                );

                // set the quest owner to match best to the template filename (null or the user id) 
                String questOwner; // can be either null or the player UUID
                switch (questTemplateNameFragments.size()) {
                    case 2:
                        questOwner = questTemplateNameFragments.get(1);
                        break;
                    default:
                        questOwner = "null";
                        break;
                }

                // skip if current player is not the owner of the quest template
                if (!questOwnerList.contains(questOwner)) { return; }
                
                // add the quest to our list
                myquestTemplates.add(questTemplateName);
            });
        } catch (IOException e) {
            throw new RuntimeException("Could not access the " + questTemplatesDir.toString() + " path. ", e);
        }

        System.out.println(myquestTemplates);

        // TODO: create the new GUI to show the quests in

        // TODO: for every quest create a GUISlot on the GUI with the quest label and maybe a book for the Item
        // start with just one quest, it doesn't have to be real

        // TODO: create just an outline for re-accessing GUIFunction.UpdateScreenFile (and for later passing in the quest we see + an edit screen)
        // -- since the edit screen or quest id system doesn't really exist this might have to remain just an outline
    }
}
