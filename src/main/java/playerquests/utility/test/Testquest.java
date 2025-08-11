package playerquests.utility.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.reflections.Reflections;

import playerquests.Core;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.action.condition.ActionCondition;
import playerquests.builder.quest.action.condition.ConditionType;
import playerquests.builder.quest.action.option.NPCOption;
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.ClientDirector;
import playerquests.client.quest.QuestClient;
import playerquests.product.Quest;
import playerquests.utility.annotation.PlayerQuestsTest;
import playerquests.utility.singleton.QuestRegistry;

public class Testquest extends TestUtility {

    public Testquest(ClientDirector clientDirector) {
        super(clientDirector);
    }

    @PlayerQuestsTest(label = "None action, no NPC")
    public CompletableFuture<Boolean> testNoneAction() {
        String testNoneActionString = """
            {
                "title" : "testNoneActionNoNPC",
                "npcs" : {
                    "npc_0" : {
                    "id" : "npc_0",
                    "name" : "testNoneActionNPC",
                    "assigned" : {
                        "type" : "BlockNPC",
                        "value" : "minecraft:grass_block[snowy=false]"
                    },
                    "location" : {
                        "world" : "world",
                        "x" : 0.0,
                        "y" : 202.0,
                        "z" : 0.0,
                        "pitch" : 0.0,
                        "yaw" : 0.0
                    }
                    }
                },
                "stages" : {
                    "stage_0" : {
                    "id" : "stage_0",
                    "actions" : {
                        "action_0" : {
                        "type" : "NoneAction",
                        "data" : {
                            "id" : "action_0",
                            "next" : [ ],
                            "options" : [ {
                            "option" : "NPC",
                            "npc_id" : null
                            } ],
                            "conditions" : [ ]
                        },
                        "label" : null
                        }
                    },
                    "startpoints" : [ ],
                    "label" : null
                    }
                },
                "creator" : null,
                "id" : "testNoneActionNoNPC",
                "startpoints" : [ "stage_0.action_0" ]
            }       
            """;
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Player player = this.clientDirector.getPlayer();
        Location originalLocation = player.getLocation();
        
        // create platform for test witness
        Location platformLocation = new Location(originalLocation.getWorld(), 1, 200, 0);
        BlockData prePlatformBlockData = platformLocation.getBlock().getBlockData();
        platformLocation.getBlock().setBlockData(Material.REDSTONE_BLOCK.createBlockData());
        Location npcLocation = new Location(originalLocation.getWorld(), 0, 202, 0);

        // teleport the player onto the platform (looking towards the test spot)
        player.teleport(new Location(originalLocation.getWorld(), 1, 202, .5, 90, 0));

        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            // clean palette for test
            npcLocation.getBlock().setBlockData(Material.AIR.createBlockData()); // make sure where the NPC would be is AIR to start off with

            // create the quest
            Quest testNoneActionQuest = Quest.fromJSONString(testNoneActionString);
            testNoneActionQuest.save(); // bring to life
        });

        // Schedule cleanup after 50 ticks
        Bukkit.getScheduler().runTaskLater(Core.getPlugin(), () -> {
            // test result
            Quest quest = QuestRegistry.getInstance().getQuest("testNoneActionNoNPC");
            Boolean isNPCAir = npcLocation.getBlock().getType() == Material.AIR;
            Boolean wasQuestSaved = QuestRegistry.getInstance().hasQuest("testNoneActionNoNPC", true);

            // post-result clean-up
            player.teleport(originalLocation);
            platformLocation.getBlock().setBlockData(prePlatformBlockData);
            QuestRegistry.getInstance().delete(quest, true, false, true);

            // end test
            result.complete(
                isNPCAir &&
                wasQuestSaved
            );
        }, 50);

        return result;
    }

    @PlayerQuestsTest(label = "None action, with NPC")
    public CompletableFuture<Boolean> testNoneActionBlockNPC() {
        String testNoneActionBlockNPCString = """
            {
                "title" : "testNoneActionWithNPC",
                "npcs" : {
                    "npc_0" : {
                    "id" : "npc_0",
                    "name" : "testNoneActionNPC",
                    "assigned" : {
                        "type" : "BlockNPC",
                        "value" : "minecraft:grass_block[snowy=false]"
                    },
                    "location" : {
                        "world" : "world",
                        "x" : 0.0,
                        "y" : 202.0,
                        "z" : 0.0,
                        "pitch" : 0.0,
                        "yaw" : 0.0
                    }
                    }
                },
                "stages" : {
                    "stage_0" : {
                    "id" : "stage_0",
                    "actions" : {
                        "action_0" : {
                        "type" : "NoneAction",
                        "data" : {
                            "id" : "action_0",
                            "next" : [ ],
                            "options" : [ {
                            "option" : "NPC",
                            "npc_id" : "npc_0"
                            } ],
                            "conditions" : [ ]
                        },
                        "label" : null
                        }
                    },
                    "startpoints" : [ ],
                    "label" : null
                    }
                },
                "creator" : null,
                "id" : "testNoneActionWithNPC",
                "startpoints" : [ "stage_0.action_0" ]
            }       
            """;
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Player player = this.clientDirector.getPlayer();
        Location originalLocation = player.getLocation();
        
        // create platform for test witness
        Location platformLocation = new Location(originalLocation.getWorld(), 1, 200, 0);
        BlockData prePlatformBlockData = platformLocation.getBlock().getBlockData();
        platformLocation.getBlock().setBlockData(Material.REDSTONE_BLOCK.createBlockData());
        Location npcLocation = new Location(originalLocation.getWorld(), 0, 202, 0);

        // teleport the player onto the platform (looking towards the test spot)
        player.teleport(new Location(originalLocation.getWorld(), 1, 202, .5, 90, 0));

        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            // clean palette for test
            npcLocation.getBlock().setBlockData(Material.AIR.createBlockData()); // make sure where the NPC would be is AIR to start off with

            // create the quest
            Quest testNoneActionQuest = Quest.fromJSONString(testNoneActionBlockNPCString);
            testNoneActionQuest.save(); // bring to life
        });

        // Schedule cleanup after 50 ticks
        Bukkit.getScheduler().runTaskLater(Core.getPlugin(), () -> {
            // prep test
            Quest quest = QuestRegistry.getInstance().getQuest("testNoneActionWithNPC");
            QuestStage stage = (QuestStage) quest.getStages().values().toArray()[0];
            QuestAction<?,?> action = (QuestAction<?,?>) stage.getActions().values().toArray()[0];
            QuestClient quester = QuestRegistry.getInstance().getQuester(clientDirector.getPlayer());
            QuestNPC npc = action.getData().getOption(NPCOption.class).get().getNPC(quest);

            // test results
            Boolean isNPCGrassBlock = quester.getData().getBlockNPC(action, npc).getMaterial() == Material.GRASS_BLOCK; // detect likelihood of ghost block success
            Boolean wasQuestSaved = QuestRegistry.getInstance().hasQuest("testNoneActionWithNPC", true); // detect if quest was submitted

            // post-result clean-up
            player.teleport(originalLocation);
            platformLocation.getBlock().setBlockData(prePlatformBlockData);
            QuestRegistry.getInstance().delete(quest, true, false, true);

            // end test
            result.complete(
                isNPCGrassBlock &&
                wasQuestSaved
            );
        }, 50);

        return result;
    }

    @PlayerQuestsTest(label = "No missing conditions in ConditionType enum")
    public CompletableFuture<Boolean> allConditionsAreInEnum() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        // get all classes implementing ActionCondition
        Reflections reflections = new Reflections("playerquests.builder.quest.action.condition");
        Set<Class<? extends ActionCondition>> allConditions = 
            reflections.getSubTypesOf(ActionCondition.class);
        
        // get all enum values
        Set<Class<?>> enumConditionClasses = Arrays.stream(ConditionType.values())
            .map(ConditionType::getConditionClass)
            .collect(Collectors.toSet());
        
        // find missing classes
        Set<Class<? extends ActionCondition>> missing = allConditions.stream()
            .filter(clazz -> !enumConditionClasses.contains(clazz))
            .collect(Collectors.toSet());
        
        // submit test result
        result.complete(missing.isEmpty());

        return result;
    }
}
