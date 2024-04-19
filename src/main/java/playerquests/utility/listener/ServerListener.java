package playerquests.utility.listener;

import java.io.IOException; // thrown when a file operation fails, like reading

import org.bukkit.Bukkit; // bukkit API
import org.bukkit.event.EventHandler; // indicate that a method is wanting to handle an event
import org.bukkit.event.Listener; // registering listening to Bukkit in-game events
import org.bukkit.event.server.ServerLoadEvent; // when a server is reloaded or started up

import com.fasterxml.jackson.annotation.JsonInclude.Include; // for configuring json serialisation
import com.fasterxml.jackson.core.JsonProcessingException; // thrown when json cannot be processed
import com.fasterxml.jackson.databind.JsonMappingException; // thrown when json is malformed
import com.fasterxml.jackson.databind.ObjectMapper; // used to work with with json objects
import com.fasterxml.jackson.databind.SerializationFeature; // for configuring json serialisation

import playerquests.Core; // accessing plugin singeltons
import playerquests.client.quest.QuestClient; // represents a quest player/quest tracking
import playerquests.product.Quest; // quest product class
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

    @EventHandler
    public void onLoad(ServerLoadEvent event) {
        if (event.getEventName().equals("RELOAD")) {
            Bukkit.getServer().getScheduler().cancelTasks(Core.getPlugin());
            QuestRegistry.getInstance().clear();
        }

        ObjectMapper jsonObjectMapper = new ObjectMapper(); // used to deserialise json to object
        
        // configure the mapper
        jsonObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // allow json object to be empty
        jsonObjectMapper.setSerializationInclusion(Include.NON_NULL);

        // try to submit database quests to quest registry
        Database.getInstance().getAllQuests().forEach(id -> {
            try {
                Quest newQuest = jsonObjectMapper.readValue(FileUtils.get("quest/templates/" + id + ".json"), Quest.class);
                QuestRegistry.getInstance().submit(newQuest);
            } catch (JsonMappingException e) {
                System.err.println("Could not accurately map template: " + id + ", to the Quest object. " + e);
            } catch (JsonProcessingException e) {
                System.err.println("JSON in template: " + id + ", is malformed. " + e);
            } catch (IOException e) {
                System.err.println("Could not read file: " + id + ".json. " + e);
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
    }
}
