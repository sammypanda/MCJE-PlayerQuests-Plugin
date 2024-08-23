package playerquests.utility.listener;

import java.io.File; // Represents a file in the filesystem
import java.io.IOException; // Thrown when a file operation fails, such as reading
import java.nio.file.*; // Provides classes and methods for file I/O operations
import java.util.HashSet; // Implements a set that does not allow duplicate elements
import java.util.Set; // Interface for collections that do not allow duplicate elements
import java.util.stream.Stream; // Provides a sequence of elements supporting sequential and parallel aggregate operations

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
import playerquests.utility.ChatUtils.MessageStyle; // Enum for different message styles
import playerquests.utility.ChatUtils.MessageTarget; // Enum for different message targets
import playerquests.utility.ChatUtils.MessageType; // Enum for different message types
import playerquests.utility.FileUtils; // Utility for file operations
import playerquests.utility.singleton.Database; // API for managing persistent game data
import playerquests.utility.singleton.PlayerQuests;
import playerquests.utility.singleton.QuestRegistry; // Registry for storing quests

/**
 * This class listens for server-related events and filesystem changes,
 * such as server load or reload, and performs necessary actions such as 
 * initializing data, processing quests, and setting up quest clients.
 */
public class ServerListener implements Listener {

    private WatchService watchService;
    private Thread watchThread;

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
     * templates, and sets up quest clients for online players. Also starts
     * the file watcher service if it is not already running.
     * 
     * @param event the ServerLoadEvent that contains information about the server load
     */
    @EventHandler
    public void onLoad(ServerLoadEvent event) {
        createDirectories(); // ensure dirs are created
        initializeDatabase(); // init db

        // Ensure quest processing runs on the main thread
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            processQuests();
            createQuestClients();
        });

        startWatchService(); // start fs watching
    }
    
    /**
     * Called when the server is being disabled (including when reloaded).
     * Designed to handle onDisable from the Core (JavaPlugin).
     * 
     * This method is used to clean up resources, stop ongoing tasks, and 
     * perform other necessary shutdown procedures.
     */
    public void onDisable() {
        // Cancel all tasks scheduled by this plugin to prevent overlaps
        Bukkit.getServer().getScheduler().cancelTasks(Core.getPlugin());

        // Close/clear the plugin
        PlayerQuests.getInstance().clear();

        // Stop the WatchService used for monitoring file changes.
        // This ensures that no further file watching occurs after 
        // the plugin is disabled, and resources related to file 
        // monitoring are properly released.
        stopWatchService();        
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
     * Processes quests from both the database and file system, and submits them to the quest registry.
     */
    private void processQuests() {
        File questsDir = new File(Core.getPlugin().getDataFolder(), "/quest/templates");
        Set<String> allQuests = new HashSet<>();
        
        // add from db
        allQuests.addAll(Database.getInstance().getAllQuests());

        // add from fs
        try (Stream<Path> paths = Files.walk(questsDir.toPath())) {
            paths.filter(Files::isRegularFile) // Filter to include only files
                .filter(path -> path.toString().endsWith(".json")) // Include only JSON files
                .forEach(path -> {
                    String questName = getQuestName(path);
                    allQuests.add(questName);
                });
        } catch (IOException e) {
            ChatUtils.message("Could not process the quests template directory/path :(. " + e)
                .target(MessageTarget.CONSOLE)
                .style(MessageStyle.PLAIN)
                .type(MessageType.ERROR)
                .send();
        }

        // submit/process collected quests
        submitQuestsToRegistry(allQuests);

        // notify the server about the newly processed quests
        ChatUtils.message("Finished submitting quests into server: " + allQuests)
            .target(MessageTarget.CONSOLE)
            .style(MessageStyle.PLAIN)
            .type(MessageType.NOTIF)
            .send();
    }

    private String getQuestName(Path path) {
        String[] questNameParts = path.toString()
            .replace(".json", "")
            .split("/templates/");

        if (questNameParts.length < 1) {
            return null;
        }

        return questNameParts[1];
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
                Database.getInstance().removeQuest(id);
            }
        });
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
     * Starts the WatchService to monitor the quest/templates directory for changes.
     * This service watches for file creation, deletion, and modification events.
     */
    private void startWatchService() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path questTemplatesPath = Paths.get(Core.getPlugin().getDataFolder() + "/quest/templates");
            questTemplatesPath.register(watchService, 
                StandardWatchEventKinds.ENTRY_CREATE, 
                StandardWatchEventKinds.ENTRY_DELETE, 
                StandardWatchEventKinds.ENTRY_MODIFY
            );

            watchThread = new Thread(() -> {
                // let the server know
                ChatUtils.message("Started watching for changes to plugin files.")
                    .style(MessageStyle.PLAIN)
                    .type(MessageType.NOTIF)
                    .target(MessageTarget.CONSOLE)
                    .send();
                
                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key;
                    try {
                        key = watchService.take();
                    } catch (InterruptedException | ClosedWatchServiceException e) {
                        Thread.currentThread().interrupt();
                        
                        // let the server know
                        ChatUtils.message("Stopped watching for changes to plugin files.")
                            .style(MessageStyle.PLAIN)
                            .type(MessageType.NOTIF)
                            .target(MessageTarget.CONSOLE)
                            .send();

                        // don't continue
                        return;
                    }   

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                    
                        // This key is no longer valid, break out of the loop
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }
                    
                        // Ensure the event is of type WatchEvent<Path>
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        // Don't panic, let's handle deletion
                        if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            System.out.println("figure out if an important file was deleted here");
                        }
                    
                        // Handle changes to quest templates
                        if (filename.toString().endsWith(".json")) {
                            String questName = filename.toString().replace(".json", ""); // strip '.json' from the quest ID/filename
                            QuestRegistry questRegistry = Core.getQuestRegistry(); // get the tracking of instances of the quests in the plugin

                            if (questName == null) {
                                return;
                            }

                            switch (kind.name()) {
                                case "ENTRY_CREATE":
                                    submitQuestsToRegistry(new HashSet<>(Set.of(questName))); // submit the quest systematically
                                    break;
                                case "ENTRY_DELETE":
                                    questRegistry.remove(
                                        questRegistry.getQuest(questName) // find the quest object
                                    ); // delete it systematically
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    // Reset the key -- this step is critical if you want to receive further watch events.
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            });
            watchThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the WatchService and interrupts the watch thread.
     * Cleans up resources used by the WatchService.
     */
    private void stopWatchService() {
        if (watchService != null) {
            watchThread.interrupt();
            try {
                watchService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            watchService = null;
            watchThread = null;
        }
    }
}