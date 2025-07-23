package playerquests.utility.listener;

import java.io.File; // Represents a file in the filesystem
import java.io.IOException; // Thrown when a file operation fails, such as reading
import java.nio.file.*; // Provides classes and methods for file I/O operations
import java.util.HashSet; // Implements a set that does not allow duplicate elements
import java.util.Set; // Interface for collections that do not allow duplicate elements
import java.util.stream.Stream; // Provides a sequence of elements supporting sequential and parallel aggregate operations

import org.bukkit.Bukkit; // Bukkit API for interacting with the server
import org.bukkit.event.EventHandler; // Annotation to mark methods as event handlers
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener; // Interface for registering event listeners
import org.bukkit.event.server.ServerLoadEvent; // Event triggered when the server is loaded or reloaded

import com.fasterxml.jackson.core.JsonProcessingException; // Thrown when JSON cannot be processed
import com.fasterxml.jackson.databind.JsonMappingException; // Thrown when JSON is malformed

import playerquests.Core; // Access to plugin singleton
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
     * files, and sets up quest clients for online players. Also starts
     * the file watcher service if it is not already running.
     * 
     * @param event the ServerLoadEvent that contains information about the server load
     */
    @EventHandler
    public void onLoad(ServerLoadEvent event) {
        // Ensure start runs in sequence
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            // ensure dirs are created
            createDirectories();
            
            // init db
            initializeDatabase();

            // find and submit to quest registry
            processQuests();

            // create questers
            Bukkit.getServer().getOnlinePlayers().stream().forEach(player -> {
                QuestRegistry.getInstance().createQuester(player);
            });

            // start fs watching
            startWatchService();
        });
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

        // Cancel all listeners
        HandlerList.unregisterAll();

        // Close/clear the plugin
        // - releases questers
        // - clears registry
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
        File dataFolder = Core.getPlugin().getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
            sendWelcomeMessage();
        }

        File questsFolder = new File(Core.getPlugin().getDataFolder(), Core.getQuestsPath());
        if (!questsFolder.exists()) {
            questsFolder.mkdirs();
        }

        Core.getPlugin().saveResource(Core.getQuestsPath() + "beans-tester-bonus.json", true);
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
        File questsDir = new File(Core.getPlugin().getDataFolder(), Core.getQuestsPath());
        Set<String> allQuests = new HashSet<>();
        
        // add from db
        allQuests.addAll(Database.getInstance().getAllQuests());

        // add from fs
        try (Stream<Path> paths = Files.walk(questsDir.toPath())) {
            paths.filter(Files::isRegularFile) // Filter to include only files
                .filter(path -> path.toString().endsWith(Core.getQuestFileExtension())) // Include only JSON files
                .forEach(path -> {
                    String questName = getQuestName(path);
                    allQuests.add(questName);
                });
        } catch (IOException e) {
            ChatUtils.message("Could not process the quest files directory/path :(. " + e)
                .target(MessageTarget.CONSOLE)
                .style(MessageStyle.PLAIN)
                .type(MessageType.ERROR)
                .send();
        }

        // submit/process collected quests
        loadQuests(allQuests, true);

        // notify the server about the newly processed quests
        ChatUtils.message("Finished submitting quests into server: " + allQuests)
            .target(MessageTarget.CONSOLE)
            .style(MessageStyle.PLAIN)
            .type(MessageType.NOTIF)
            .send();
    }

    private String getQuestName(Path path) {
        String[] questNameParts = path.toString()
            .replace(Core.getQuestFileExtension(), "")
            .split("/" + Core.getQuestsPath());

        if (questNameParts.length < 1) {
            return null;
        }

        return questNameParts[1];
    }

    /**
     * Loads quests from a list of IDs by searching the filesystem.
     * 
     * @param quests the set of quest IDs to process
     * @param overwrite whether to save over an existing
     */
    private void loadQuests(Set<String> quests, Boolean overwrite) {
        quests.forEach(id -> {
            boolean errorOccurred = true; // Assume an error occurred initially
            
            try {
                Quest newQuest = Quest.fromJSONString(FileUtils.get(Core.getQuestsPath() + id + Core.getQuestFileExtension()));

                // if creates invalid object, exit
                if (newQuest == null) {
                    return;
                }

                // if already in registry, exit
                if (QuestRegistry.getInstance().getQuest(id, false) != null) {
                    return;
                }

                // if can overwrite the file
                if (overwrite) {
                    newQuest.save(); // submit to registry
                }

                errorOccurred = false;

            } catch (JsonMappingException e) {
                ChatUtils.message("Could not map JSON string for: " + id + " to the Quest object. " + e)
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
            } catch (JsonProcessingException e) {
                ChatUtils.message("JSON in quest: " + id + " is malformed. " + e)
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
            } catch (IOException e) {
                ChatUtils.message("Could not read file: " + id + ".json. " + e)
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
            }

            // Remove the quest from the database if an error occurred
            if (errorOccurred) {
                Database.getInstance().removeQuest(id);
            }
        });
    }

    /**
     * Starts the WatchService to monitor the quest resources directory for changes.
     * This service watches for file creation, deletion, and modification events.
     */
    private void startWatchService() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path questFilesPath = new File(Core.getPlugin().getDataFolder(), Core.getQuestsPath()).toPath();
            questFilesPath.register(watchService, 
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
                    
                        // Handle changes to quest files
                        if (filename.toString().endsWith(Core.getQuestFileExtension())) {
                            String questName = filename.toString().replace(Core.getQuestFileExtension(), ""); // strip '.json' from the quest ID/filename
                            QuestRegistry questRegistry = Core.getQuestRegistry(); // get the tracking of instances of the quests in the plugin

                            if (questName == null) {
                                return;
                            }

                            switch (kind.name()) {
                                case "ENTRY_CREATE":
                                    // submit the quest systematically
                                    loadQuests(new HashSet<>(Set.of(questName)), false);
                                    break;
                                case "ENTRY_DELETE":
                                    // find the quest object
                                    Quest questToDelete = questRegistry.getQuest(questName);

                                    // exit if not found
                                    if (questToDelete == null) {
                                        return;
                                    }

                                    // delete it systematically (but non-permanent)
                                    questRegistry.delete(questToDelete, false, false, false);
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