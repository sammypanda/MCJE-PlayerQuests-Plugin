# Dependencies
- Maven (mvn)
    - Current build command: ``mvn -f [path/to/plugin/root/directory] clean package -U -e``
- Java (openjdk-17)

# Specification
###### the way to visualise/think about, and implement the program.
Each <ins>quest</ins> is a <ins>container of stages</ins>. Each <ins>stage</ins> is a <ins>container of actions</ins> from the quest/NPC perspective (actions can also be stacked). Stages are all the things which occur. See examples in the table:

| Action         | Stages                                |
|----------------|---------------------------------------|
| Request Item   | Finding the flower                    |
| Speak          | First interaction with NPC            |
| Listen         | Deciding a favourite                  |

<br>

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
    "title": String,
    "size": int,
    "slots": [
        {
            "slot": Integer,
            "item": String,
            "label": String,
            "functions": [
                { "name": String, "params": [] },
                { "name": String, "params": [] }
            ]
        }
    ]
  }
```
