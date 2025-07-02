# 📖 Welcome to PlayerQuests Guide!
Hey! Thanks for being here. Let's create some quests in survival 💖🗺️

<br>

### 📍 Getting Started
If you're new and want to know how to make a quest, see [this tutorial](tutorial/README.md).

<br>

### ❔ What quests can do
I'm always planning to add more quest actions, but so far:

| Action                                  | Description                                             | Supported |
| --------------------------------------- | ------------------------------------------------------- | --------- |
| None                                    | Stalls the quest + keeps the NPC in the world (default) | ✅        |
| Speak                                   | Makes an NPC talk to the player                         | ✅        |
| Request Item                            | Waits for the player to get an item                     | ✅        |
| Reward Item                             | Gives the player an item                                | ✅        |
| Take Item                               | Takes an item from the player                           | ✅        |
| Narrate                                 | Sends a message to the player                           | ❌         |
| Walk                                    | Makes an NPC walk to a location                         | ❌         |
| Teleport                                | Makes an NPC appear at a location                       | ❌         |
| Craft                                   | Waits for the player to craft an item                   | ❌         |
| Sleep                                   | Waits for a player to sleep x amount of times           | ❌         |
| Bossbar                                 | Puts text into the boss bar                             | ❌         |

<br>

### ❔ Types of NPCs
NPCs are the main part of how quests are interacted with.

| NPC Type                                              | Supported |
| ----------------------------------------------------- | --------- |
| Block                                                 | ✅        |
| Customised Blocks                                     | ❌         |
| Entities                                              | ✅        |
| Players                                               | ❌         |

<br>

### ❔ Conditions for Actions
Sometimes you might want to change the requirements for when the action can be played.

| Action Condition                        | Description                                                     | Supported  |
| --------------------------------------- | --------------------------------------------------------------- | ---------- |
| Time                                    | Set the time of day this action can be played                   | ✅         |
| Action Completion                       | Set actions required before this action can be played           | ✅         |
| Anti Action Completion                  | Set actions that if are completed, this action cannot be played | ❌          |
| Location                                | Set the area this action can be played in                       | ❌          |
| Health                                  | Set the required health to play this action                     | ❌          |
| Hunger                                  | Set the required hunger to play this action                     | ❌          |
| Random Chance                           | Set the random chance this action can be played                 | ❌          |
| Cooldown                                | Set the required time elapsed before this action can be played  | ❌          |
| Weather                                 | Set the weather required before this action can be played       | ❌          |
| Achievement                             | Set an achievement required before this action to be played     | ❌          |
