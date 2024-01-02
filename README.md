# Dependencies
- Maven (mvn)
    - Current build command: ``mvn -f [path/to/plugin/root/directory] clean package -U -e``
- Java (openjdk-17)

# Specification
###### the way to visualise/think about, and implement the program.
Each <ins>quest</ins> is a <ins>container of events</ins>. Each <ins>event</ins> is a <ins>container of actions</ins> from the quest/NPC perspective (actions can also be stacked). Events are all the things which occur. See examples in the table:

| Action         | Events                                |
|----------------|---------------------------------------|
| Request Item   | Finding the flower                    |
| Speak          | First interaction with NPC            |
| Listen         | Deciding a favourite                  |

<br>

###### The code follows a package-by-feature pattern.

| Folder                      | Purpose                                                     |
|-----------------------------|-------------------------------------------------------------|
| gui/                        | code related to GUIs                                        |
| chat/                       | code related to the in-game chat + commands                 |
| utils/                      | code used to help the main classes                          |
| ../../resources/gui/screen/ | GUI templates in JSON                                       |

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

###### Access to the plugin

- Configuration
    - The server admin should be able to configure the main quest command, but by default it would be /quest. i.e. it may be modified to /playerquest in case of clashes. 
        <details>
            <summary>The reason why /quest instead of /playerquest for the default</summary>
            Servers with lots of plugins where /quest may already exist suggests that the <b>server admins may be comfortable with plugins enough to do extra configuring</b>. Servers where this may be one of the only plugins though, suggest a less technical audience <b>where the intuitiveness of the command being /quest is very important</b>.
        </details>

<br>

- Commands
    - Help
        - Aliases: h, how, ?, support
    - Create
        - Aliases: c, build, new, design, make, compose, construct, forge, planner
    - Edit
        - Aliases: e, modify, adjust, patch, change