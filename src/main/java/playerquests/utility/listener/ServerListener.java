package playerquests.utility.listener;

import java.io.File;
import java.io.IOException; // thrown when a file operation fails, like reading
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.bukkit.Bukkit; // bukkit API
import org.bukkit.event.EventHandler; // indicate that a method is wanting to handle an event
import org.bukkit.event.Listener; // registering listening to Bukkit in-game events
import org.bukkit.event.server.ServerLoadEvent; // when a server is reloaded or started up

import com.fasterxml.jackson.core.JsonProcessingException; // thrown when json cannot be processed
import com.fasterxml.jackson.databind.JsonMappingException; // thrown when json is malformed

import playerquests.Core; // accessing plugin singeltons
import playerquests.client.quest.QuestClient; // represents a quest player/quest tracking
import playerquests.product.Quest; // quest product class
import playerquests.utility.ChatUtils; // for sending cute-ified messages
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.FileUtils; // helpers for working with files
import playerquests.utility.singleton.Database; // API for game persistent data
import playerquests.utility.singleton.QuestRegistry; // place where quests are stored

/**
 * Listens out for all server-related events to inform 
 * where needed.
 */
public class ServerListener implements Listener {

    public ServerListener() {
        Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
    }

    /**
     * Ran whenever the server is started or reloaded.
     * Use [return value].onFinish(() -> {}) for callback.
     * @param event the LoadType
     * @return the ServerListener itself
     */
    @EventHandler
    public ServerListener onLoad(ServerLoadEvent event) {
        // create plugin folder if it doesn't exist
        File f = new File(Core.getPlugin().getDataFolder() + "/");
        if (!f.exists()) {
            f.mkdir();
            ChatUtils.message("Welcome!")
                .target(MessageTarget.WORLD)
                .type(MessageType.NOTIF)
                .send();
        }

        // Save the demo quest to the server
        Core.getPlugin().saveResource("quest/templates/beans-tester-bonus.json", true);

        // initialise the database
        Database.getInstance().init();

        if (event.getEventName().equals("RELOAD")) {
            Bukkit.getServer().getScheduler().cancelTasks(Core.getPlugin());
            QuestRegistry.getInstance().clear();
        }

        // find all quests
        File fsQuestsDir = new File(Core.getPlugin().getDataFolder(), "/quest/templates");
        Set<String> allQuests = new HashSet<String>(); // CREATE MAIN LIST
        allQuests.addAll(Database.getInstance().getAllQuests()); // ADD DB QUESTS
        try (Stream<Path> paths = Files.walk(fsQuestsDir.toPath())) { // ADD FILESYTEM QUESTS
            paths.filter(Files::isRegularFile)  // no dirs, only files
                .filter(path -> path.toString().endsWith(".json"))  // only json files
                .forEach(path -> {
                    String questName = path.toString()
                        .replace(".json", "")
                        .split("/templates/")[1];
                    allQuests.add(questName);
                });
        } catch (IOException e) {
            ChatUtils.message("Could not process the quests template directory/path :(. " + e)
                .target(MessageTarget.CONSOLE)
                .style(MessageStyle.PLAIN)
                .type(MessageType.ERROR)
                .send();
        }

        // try to submit found quests to quest registry
        allQuests.forEach(id -> {
            Boolean err = true; // assume errored (avoids repeating it for each catch)
            
            try {
                Quest newQuest = Quest.fromTemplateString(FileUtils.get("quest/templates/" + id + ".json"));

                if (newQuest == null) {
                    return;
                }

                QuestRegistry.getInstance().submit(newQuest);
                err = false;

            } catch (JsonMappingException e) {
                System.err.println("Could not accurately map template: " + id + ", to the Quest object. " + e);
            } catch (JsonProcessingException e) {
                System.err.println("JSON in template: " + id + ", is malformed. " + e);
            } catch (IOException e) {
                System.err.println("Could not read file: " + id + ".json. " + e);
            }

            // remove the quest if unreadable
            if (err) {
                Database.removeQuest(id);
            }
        });

        System.out.println("[PlayerQuests] Finished loading database quests into registry: " + QuestRegistry.getInstance().getAllQuests().keySet());

        // for every user, create a quest client and add it to registry.
        // this enables a player to interact with and track quests.
        Bukkit.getServer().getOnlinePlayers().stream()
            .forEach(player -> {
                QuestClient quester = new QuestClient(player);
                QuestRegistry.getInstance().addQuester(quester);
            });

        // function chaining reasons:
        return this;
    }

    /**
     * Sets code to be executed when the function is finished.
     * @param runnable the code to run when the function completes
     */
    public void onFinish(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }
}
