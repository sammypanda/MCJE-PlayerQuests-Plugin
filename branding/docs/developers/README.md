# How To Develop: 'Dependencies'
- Maven (mvn)
    - Current build command: ``mvn -f [path/to/plugin/root/directory] clean install``
- Java (JDK 22)

# How To Get Functionality: 'Actions'
###### How pre-defined but flexible functionality is actually associated with buttons and other behaviours.
Realistically 'Quest Actions' won't ever have to be called by their function names. It would just be from a list in the quest builder UI/UX. Here is a list for devs or if you're a very brave user.

###### gui functions (Functions)
| Function (How to refer to) | Parameters (How to customise)                                                                                 | Purpose (What it does)                                                    |
|----------------------------|---------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| UpdateScreen               | 1: the dynamic GUI name                                                                                       | Changes the current GUI screen to a different GUI                         |
| ChatPrompt                 | 1: the prompt to show to the user<br>2: key of the value to set (options: "none", "gui.title", "quest.title") | Prompts the user to sets a text value (by typing in the chat box)         |
| Save                       | 1. key of instance to save (options: "quest")                                                                 | Calls the defined save processes for instances                            |
| SelectMaterial             | 1. the prompt to show the user<br>2. list of denied materials<br>3. list of denied methods<br>4. if has to be a block | Prompts the user to select a block (by hitting or selecting in inventory) |
| SelectLocation             | 1. the prompt to show the user                                                                                | Prompts the user to place a block to set it as the location               |

###### Quest Actions (Actions)
TODO: Each <ins>quest</ins> is a <ins>container of stages</ins>. Each <ins>stage</ins> is a <ins>container of actions</ins> (actions can also be stacked). Stages are all the things which occur. See examples in the table (named from the quest/NPC perspective):

| Type (How to refer to) | Parameters (How to customise) | Purpose (What it does)                          |
|------------------------|-------------------------------|-------------------------------------------------|
| None                   | N/A                           | Nothing; ignored                                |
| Speak                  | 1: Text<br>2: NPC ID          | Makes an NPC say things                         |
| GatherItem             | 1: Material ENUM<br>2: Count  | Generic item + amount the quest wants           |

# How To Get Functionality: 'Templates'
###### We have Meta and Quest Actions, but how do we actually use them?
Usually you would never need this, but this is what makes it all tick. When you create a Quest: stages, npcs, actions and all; this is the format and layout it is constructing:
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
- *'Path:' means the string is represented something like: "stage_0.action_0", it can also be just like: "stage_0"*

# How It All Works: 'Specification'
###### the way to visualise/think about, and implement the program.

| Folder                           | Purpose                          |
|----------------------------------|----------------------------------|
| ../../resources/quest/templates/ | Quest templates in JSON          |
| builder/                         | Produce product instances        |
| product/                         | The product instances            |
| client/                          | Ways to control the plugin       |
| utility/                         | Tools for reducing repeated code |
| utility/annotation               | Custom code annotations          |

# How to add new quest actions
Feel free to use 'Speak' as an example to help you, alongside this brief guide:
1. Like 'None' and 'Speak' each quest action should extend the QuestAction class.
  - Then add the unimplemented methods, as required, from QuestAction.
  - Add an empty constructor for Jackson parsing, and one taking QuestStage.
2. Then after the new one is created, in QuestAction it needs to be added to the JsonSubTypes annotations and the allActionTypes() list.
3. Write the code to implement the action and add javadocs. 
  - Such as: return list of options used for this action in InitOptions, at least return an empty optional in validate (as to mean 'no error message').
  - If you need to add an ActionOption just add it to the ActionOption enum and then create a case for it in the Dynamicactioneditor.