# How To Develop: 'Dependencies'
- Maven (mvn)
    - Build command: ``mvn -f [path/to/plugin/root/directory] clean install``
- Java (JDK 22)

<br>

# How To Get Functionality: 'Templates'
Usually you would never need this, but this is what makes it all tick. When you create a Quest: stages, npcs, actions and all; this is the data it is constructing:
```json
{
    "title": String, // label of the entire quest
    "creator": UUID, // the player who created this quest
    "id": String, // the id, composed of: [Quest Title]_[Creator UUID]
    "inventory": {
        "[a minecraft material]": Integer // for instance: "BIRCH_LOG" : 1
    },
    "npcs": { // directory of all the quest npcs
        "npc_0": { // NPC ID (automatically generated)
            "name": String, // the name of the NPC
            "assigned": {
                "type": String, // type the NPC is assigned to (options: "BlockNPC")
                "value": String // the standard minecraft block string (like: minecraft:acacia_log)
            },
            "location": {
                "x": Double,
                "y": Double,
                "z": Double,
                "pitch": Double,
                "yaw": Double
            }
        }
    },
    "stages": { // directory of all the quest stages
        "stage_0": { // Stage ID (automatically generated)
            "notable": Boolean, // if it would show up as a chapter in a book; a notable stage
            "label": String, // the label for just this stage, as if it were a chapter
        },
    }
}
```
- *It's worth noting that just because the IDs are incremental, all starting from zero, doesn't mean they are expected to be kept/used in order or in sequence.*
- *'Path:' means the string is represented something like: "stage_0.action_0". It can also look like: "stage_0"; actions cannot, they must be like stage_0.action_0. (Where 0 is any number)*

<br>

# How To Contribute: 'Specification'
## How to visualise/think about the program

| Folder                           | Purpose                          |
|----------------------------------|----------------------------------|
| ../../resources/quest/templates/ | Quest templates in JSON          |
| builder/                         | Produce product instances        |
| product/                         | The product instances            |
| client/                          | Ways to control the plugin       |
| utility/                         | Tools for reducing repeated code |
| utility/annotation               | Custom code annotations          |


<br>

## How to add new quest actions
Feel free to use 'Speak' as an example to help you, alongside this brief guide:
1. Like 'None' and 'Speak' each quest action should extend the QuestAction class.
  - Then add the unimplemented methods, as required, from QuestAction.
  - Add an empty constructor for Jackson parsing, and one taking QuestStage.
2. Then after the new one is created, in QuestAction it needs to be added to the JsonSubTypes annotations and the allActionTypes() list.
3. Write the code to implement the action and add javadocs. 
  - Such as: return list of options used for this action in InitOptions, at least return an empty optional in validate (as to mean 'no error message').
  - If you need to add an ActionOption just add it to the ActionOption enum and then create a case for it in the Dynamicactioneditor.