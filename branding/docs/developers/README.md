# How To Develop: 'Dependencies'
- Maven (mvn)
    - Current build command: ``mvn -f [path/to/plugin/root/directory] clean install``
- Java (JDK 23)

<br>

# How To Store and Remotely Edit Quests: 'Quest Files'
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
- *'Path:' means the string is represented something like: "stage_0.action_0". It can also look like: "stage_0"; actions cannot, they must be like stage_0.action_0. (Where 0 is any number)*

<br>

# How To Contribute: 'Specification'
## How to visualise/think about the program

| Folder                           | Purpose                          |
|----------------------------------|----------------------------------|
| ../../resources/quests/          | Quests stored in JSON            |
| builder/                         | Produce product instances        |
| product/                         | The product instances            |
| client/                          | Ways to control the plugin       |
| utility/                         | Tools for reducing repeated code |
| utility/annotation               | Custom code annotations          |

<br>

## All about quest actions
### What and where are quest actions?
In ``builder/quest/action`` there is the ``QuestAction`` class (similar to ``QuestStage``).

It is an extendable (inheritable) class.

They all require logic (shown in runtime order):
- ``Automated: The run method; to start the action``
- A preparation method; before registering the listener.
- A private``Listener`` class to trigger checks.
- ``Automated: The check method; to trigger the validate or finish``
- A validate method; logic to validate if was successful or not. 
- ``Automated: The stop method; to complete the action``
- onSuccess and onFailure methods; like giving rewards and other completion logic.

They should all be set up with some data:
- A list of action option objects (see below).
- A list of eligible action conditional objects (see after action options).

### What are quest action options?
Similarly the ``builder/quest/action/option`` there is an ``ActionOption`` class that is extendable. It is responsible for it's own interfaces (like GUI/commands). 

An option should not have it's own options, for example **do not** create ``Dialogue.TextMap`` and ``Dialogue.Text`` just do them as separate actions like ``TextMap`` and ``Text``. They should be reusable, like ``Text`` may be for a ``QuestAction`` of an NPC saying a statement but it also may be for a ``QuestAction``'s finish message ~ it's just about the form of the data, so for ``Text`` that would be anything that expects just a single string. 

Also meaning most field titles and wording should be customisable by the action. Then with the action it just passes in it's own customised ``ActionOption`` object.

This all means the ``QuestAction`` can just do ``QuestOption.getTextMap()`` to get the value.

Q: Now we know, ``QuestAction`` and ``ActionOption`` exist, how do they relate to each other?<br>
A: In ``QuestAction``, you  define a list of ``ActionOption``s it uses.

### What are quest action conditionals?
``QuestAction`` conditionals, can be placed as starting conditions ``startConditions`` or conditions to meet before an action can be considered finished ``finishConditions``.

These are found in ``builder/quest/action/conditions``. ``ActionCondition`` is another inheritable class that is responsible for defining it's own (GUI/command) interfaces and functionality.

An example would be ``TimeIs``: the time range it should be. Or ``HasCompleted``: a list of other actions/stages that need to have been completed beforehand.

### How does one action continue on to the next?
If no ``finishConditions`` exist or they are all satisfied, then ``QuestAction`` offers a list of ``StagePath`` objects. ``StagePath``s are just a pointer to a stage_?.action_? or stage_? ~ if no action defined it'll default to the entry point action of that stage.

