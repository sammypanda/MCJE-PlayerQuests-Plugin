# How To Develop: 'Dependencies'
- Maven (mvn)
    - Current build command: ``mvn -f [path/to/plugin/root/directory] clean package -U -e``
- Java (openjdk-17)

# How To Get Functionality: 'Actions'
###### How pre-defined but flexible functionality is actually associated with buttons and other behaviours.
Realistically 'Quest Actions' won't ever have to be called by their function names. It would just be from a list in the quest builder UI/UX. But when creating GUI template files (usually with 'Meta Actions' or 'Functions') there isn't much option to select from a list of actions, so here is a list for devs or if you're a very brave user.

###### Meta Actions (Functions)
| Function (How to refer to) | Parameters (How to customise)                                                                                            | Purpose (What it does)                                    |
|----------------------------|--------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| UpdateScreenFile           | 1: the template filename (without .json)                                                                                 | Changes the current GUI screen to a different template    |
| UpdateScreenDynamic        | 1: one of the predefined dynamic screens (options: "myquests")<br>2: screen to go back to (usually just the current one) | Changes the current GUI screen to present dynamic content |
| UpdateScreen               | 1: the template expression (as json string)                                                                              | Changes the current GUI screen to a different template    |
| ChatPrompt                 | 1: the prompt to show to the user<br>2: key of the value to set (options: "gui.title")                                   | Prompts the user sets the user input result as a value    |

###### Quest Actions (Actions)
Each <ins>quest</ins> is a <ins>container of stages</ins>. Each <ins>stage</ins> is a <ins>container of actions</ins> (actions can also be stacked). Stages are all the things which occur. See examples in the table (named from the quest/NPC perspective):

| Function (How to refer to) | Parameters (How to customise) | Purpose (What it does)                          |
|----------------------------|-------------------------------|-------------------------------------------------|
| Speak                      | 1: Text<br>2: NPC ID          | Makes an NPC say things                         |
| RequestItem                | 1: Material ENUM<br>2: Count  | Generic item + amount the quest wants           |
| ChangeQuestEntry           | 1: stage ID or action ID      | Changes what stage or action the quest opens to |

# How To Get Functionality: 'Templates'
###### We have Meta and Quest Actions, but how do we actually use them?
Usually you would never need this, but this is what makes it all tick. When you create a Quest: stages, npcs, actions and all; this is the format and layout it is constructing:
```json
{
    "title": String, // label of the entire quest
    "entry": String, // (as in 'entry point') where the quest starts
    "npcs": { // directory of all the quest npcs
        "npc_0": { // NPC ID (automatically generated)
            "name": String // the name of the NPC
        }
    },
    "stages": { // directory of all the quest stages
        "stage_0": { // Stage ID (automatically generated)
            "notable": Boolean, // if it would show up as a chapter in a book; a notable stage
            "label": String, // the label for just this stage, as if it were a chapter
            "entry": String, // where the stage starts
            "actions": { // directory of all this stage's actions
                "action_0": {  // Action ID (automatically generated)
                    "name": String, // Quest Action name
                    "params": Array, // Quest Action parameters
                    "connections": { // defining where the action is in the stage
                        "next": @Nullable String, // where to go if the action succeeds
                        "curr": @Nullable String, // where to return to if the action is exited
                        "prev": @Nullable String // where to go if the actions fails
                    }
                }
            },
            "connections": { // defining where the stage is in the quest
                "next": @Nullable String, // where to go if the stage succeeds
                "curr": @Nullable String, // where to return to if the stage is exited
                "prev": @Nullable String // where to go if the stage fails
            }
        },
    }
}
```
*It's worth noting that just because the IDs are incremental, all starting from zero, doesn't mean they are expected to be kept/used in order or in sequence.*

# How It All Works: 'Specification'
###### the way to visualise/think about, and implement the program.

###### The code follows a package-by-feature pattern.

| Folder                      | Purpose                                                              |
|-----------------------------|----------------------------------------------------------------------|
| ../../resources/gui/screen/ | GUI templates in JSON                                                |
| gui/                        | code related to GUIs                                                 |
| chat/                       | code related to the in-game chat + commands                          |
| utils/                      | code used to help the main classes                                   |
| quest/                      | code related to quests                                               |
| quest/builder/              | code related to quest building/creating                              |
| quest/event/                | code related to handling quest meta-events (like quitting the quest) |
| quest/player/               | code related to playing quests                                       |
| quest/stage/                | code related to organising the stages of the quest                   |
| quest/stage/action/         | code related to the handling of quest objectives                     |

<br>

###### GUI templates are parsed as JSON strings
It is built in such a way that even if fields are missing, it should still succeed as much as possible. It does this by efficiently using the information the plugin has so far.

- The GUI class stores slots array items into a List of GUISlot instances.

| Key-Value Pair  | Behaviour When Missing                       |
|-----------------|----------------------------------------------|
| title           | Uses an empty string                         |
| size            | Uses 9 slots                                 |
| slots           | Provides all {size} slots as empty           |
| slots.slot      | Defaults to the next slot which is not taken |
| slots.item      | Uses an air or grey stained glass item/block |
| slots.label     | Uses an empty string                         |
| slots.functions | Does nothing when slot is interacted with    |

<br>

###### GUI screen templating layout (JSON)
Where all the manually generated GUI screens are (src/main/resources/gui/screens/*.json).

```json
{
    "title": String, // title of the GUI window
    "size": int, // how many slots are in the inventory GUI (multiples of 9 only) 
    "slots": [ // list of slots
        {
            "slot": Integer, // position of the GUI slot
            "item": String, // item that should show in the slot
            "label": String, // hover tooltip on the slot
            "functions": [ // list of functions
                { "name": String, "params": [] }, // a function with list of params
                { "name": String, "params": [] } // ..
            ]
        }
    ]
}
```
