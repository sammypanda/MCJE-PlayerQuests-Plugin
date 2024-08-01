package playerquests.utility.listener;

import java.io.File;
import java.io.IOException; // Thrown when a file operation fails, such as reading
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.bukkit.Bukkit; // Bukkit API for interacting with the server
import org.bukkit.event.EventHandler; // Annotation to mark methods as event handlers
import org.bukkit.event.Listener; // Interface for registering event listeners
import org.bukkit.event.server.ServerLoadEvent; // Event triggered when the server is loaded or reloaded

import com.fasterxml.jackson.core.JsonProcessingException; // Thrown when JSON cannot be processed
import com.fasterxml.jackson.databind.JsonMappingException; // Thrown when JSON is malformed

import playerquests.Core; // Access to plugin singleton
import playerquests.client.quest.QuestClient; // Represents a quest client for player quest tracking
import playerquests.product.Quest; // Represents a quest product class
import playerquests.utility.ChatUtils; // Utility for sending chat messages
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.FileUtils; // Utility for file operations
import playerquests.utility.singleton.Database; // API for managing persistent game data
import playerquests.utility.singleton.QuestRegistry; // Registry for storing quests

/**
 * This class listens for server-related events, such as server load or reload,
 * and performs necessary actions such as initializing data and processing quests.
 */
public class ServerListener implements Listener {

    /**
     * Constructor for the ServerListener class. Registers this listener
     * with the Bukkit event system.
     */
    public ServerListener() {
        Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
    }

    /**
     * Handles the ServerLoadEvent, which is triggered when the server is
     * started or reloaded. Initializes necessary directories, processes quest
     * templates, and sets up quest clients for online players.
     * 
     * @param event the ServerLoadEvent that contains information about the server load
     * @return the ServerListener instance
     */
    @EventHandler
    public ServerListener onLoad(ServerLoadEvent event) {
        createDirectories();
        initializeDatabase();

        if (isServerReload(event)) {
            handleServerReload();
        }

        processQuests();
        createQuestClients();

        return this;
    }

    /**
     * Creates necessary directories for the plugin if they do not already exist.
     */
    private void createDirectories() {
        File dataFolder = new File(Core.getPlugin().getDataFolder() + "/");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
            sendWelcomeMessage();
        }

        File templatesFolder = new File(Core.getPlugin().getDataFolder() + "/quest/templates");
        if (!templatesFolder.exists()) {
            templatesFolder.mkdirs();
        }

        Core.getPlugin().saveResource("quest/templates/beans-tester-bonus.json", true);
    }

    /**
     * Sends a welcome message to the world when the plugin folder is created.
     */
    private void sendWelcomeMessage() {
        ChatUtils.message("Welcome!")
            .target(MessageTarget.WORLD)
            .type(MessageType.NOTIF)
            .send();
    }

    /**
     * Initializes the database and handles any necessary setup.
     */
    private void initializeDatabase() {
        Database.getInstance().init();
    }

    /**
     * Determines if the event represents a server reload.
     * 
     * @param event the ServerLoadEvent
     * @return true if the event represents a server reload, false otherwise
     */
    private boolean isServerReload(ServerLoadEvent event) {
        return "RELOAD".equals(event.getEventName());
    }

    /**
     * Handles the server reload by canceling ongoing tasks and clearing the quest registry.
     */
    private void handleServerReload() {
        Bukkit.getServer().getScheduler().cancelTasks(Core.getPlugin());
        QuestRegistry.getInstance().clear();
    }

    /**
     * Processes quests from both the database and file system, and submits them to the quest registry.
     */
    private void processQuests() {
        File questsDir = new File(Core.getPlugin().getDataFolder(), "/quest/templates");
        Set<String> allQuests = new HashSet<>();
        allQuests.addAll(Database.getInstance().getAllQuests());

        try (Stream<Path> paths = Files.walk(questsDir.toPath())) {
            paths.filter(Files::isRegularFile)  // Filter to include only files
                .filter(path -> path.toString().endsWith(".json"))  // Include only JSON files
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

        submitQuestsToRegistry(allQuests);
    }

    /**
     * Submits quests to the quest registry and handles any errors.
     * 
     * @param quests the set of quest IDs to process
     */
    private void submitQuestsToRegistry(Set<String> quests) {
        quests.forEach(id -> {
            boolean errorOccurred = true; // Assume an error occurred initially
            
            try {
                Quest newQuest = Quest.fromTemplateString(FileUtils.get("quest/templates/" + id + ".json"));

                if (newQuest == null) {
                    return;
                }

                QuestRegistry.getInstance().submit(newQuest);
                errorOccurred = false;

            } catch (JsonMappingException e) {
                System.err.println("Could not map template: " + id + " to the Quest object. " + e);
            } catch (JsonProcessingException e) {
                System.err.println("JSON in template: " + id + " is malformed. " + e);
            } catch (IOException e) {
                System.err.println("Could not read file: " + id + ".json. " + e);
            }

            // Remove the quest from the database if an error occurred
            if (errorOccurred) {
                Database.removeQuest(id);
            }
        });

        System.out.println("[PlayerQuests] Finished loading database quests into registry: " + QuestRegistry.getInstance().getAllQuests().keySet());
    }

    /**
     * Creates a quest client for each online player and adds it to the quest registry.
     */
    private void createQuestClients() {
        Bukkit.getServer().getOnlinePlayers().forEach(player -> {
            QuestClient quester = new QuestClient(player);
            QuestRegistry.getInstance().addQuester(quester);
        });
    }

    /**
     * Executes the provided runnable code when the function completes.
     * 
     * @param runnable the code to execute upon completion
     */
    public void onFinish(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }
}
