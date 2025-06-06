# 🏗️ PlayerQuests Tutorial
##### It may seem complicated, but when you know the basics it's easy.<br>
This tutorial is structured to start at the most easy and common, to the most niche. You can stop at whatever point you are satisfied/come back another time.

###### ⚠️ If you close any of the menus without pressing Save, you’ll lose your progress!
###### 🔃 Last Updated: 20/04/2025

<br>

## 1. Opening the menu
1. Type the command ``/playerquests`` into chat

<img alt="A menu appears showing an exit button, create new quest button, view quests button and a quest diary button" src="../../assets/docs/menu.png" width="450"/>

<br>

## 2. Creating a quest
1. Click on the green **Create Quest** button

<img alt="The create quest button is lime dye in the 3rd slot" src="../../assets/docs/create-quest-button.png" width="450"/>

2. Send your quest title in the chat (or send **'exit'** to leave). I'll call mine **'Test'**. When you're happy send **'confirm'**
3. You'll see a new screen that might seem a bit overwhelming... but don't worry! Just press **Save** to create your quest. It won't have any NPCs, stages or actions yet, but that's okay! 😊

<img alt="A menu appears showing a back button, change quest title button, blanked out stages button, N.P.C's button, starting points button and a save button" src="../../assets/docs/quest-editor-menu.png" width="450"/>

<br>

## 3. Editing a quest
> Want to pick back up on working on your saved quest? Here's how:
1. Open the menu again and press **'Edit Quests'**

<img alt="The edit quests button is the painting in the 4th slot" src="../../assets/docs/edit-quests-button.png" width="450"/>

2. Find your quest title in the list and click on it
    - If it's missing, it probably means that it wasn't saved
3. Press 'Edit' to go back to the quest editor for it

<img alt="The edit quest button is the book and quill in the 3rd slot" src="../../assets/docs/edit-quest-button.png" width="450"/>

4. Now we're back to the edit screen!
    - Any edits saved to a quest will make all progress reset for people playing it

<img alt="A menu appears showing a back button, change quest title button, blanked out stages button, N.P.C's button, starting points button and a save button" src="../../assets/docs/quest-editor-menu.png" width="450"/>

<br>

## 4. Adding an NPC to your quest
> Let’s make your quest even more exciting by adding an NPC! 🌈 Here’s how:
1. Go back to the quest editor
2. Press **'Quest NPCs'**

<img alt="The quest NPCs button is the enderchest in the 5th slot" src="../../assets/docs/quest-npcs-button.png" width="450"/>

3. Press the green **'Add NPC'** button

<img alt="The add NPC button is the lime dye" src="../../assets/docs/add-npc-button.png" width="450"/>

4. There are lots of options here, but hang tight!
5. Let's customise this NPC
    1. Use the first option to **'Set NPC Name'**, like how we set the quest title (I'll call my NPC **'Wei'**)

    <img alt="The set NPC name button is the name tag in the 3rd slot" src="../../assets/docs/set-npc-name-button.png" width="450"/>

    2. Press **'Assign NPC to...'**

        <img alt="The assign NPC to button is the block in the 4th slot" src="../../assets/docs/assign-npc-to-button.png" width="450"/>

        1. Choose **'A Block'**

        <img alt="The 'A Block' button is the grass block in the 3rd slot" src="../../assets/docs/assign-a-block-button.png" width="450"/>

        2. Open your inventory a click a block, like a dirt block
        3. The block should show on the right side as **'Place NPC'**, click this (it will only take it from you when you save)

        <img alt="The 'Place NPC (BlockNPC)' button is the block in the far right slot" src="../../assets/docs/place-npc-blocknpc-button.png" width="450"/>

        4. It will take all your blocks, you just place it somewhere to set the location the NPC will be
        5. You can also press it again to **'Relocate NPC'**
        6. Press **'Back'**
    3. Press **'Save'**
        - If it doesn't let you save, it should say why in the chat
6. Yay! An NPC has now been added for use in your future quest!
    - You can press the NPC egg to edit your NPC more/again
7. Go **'Back'** and press **'Save'**
    - Your NPC won't show in the world until it is told to be
    - You can't interact with NPC blocks like normal blocks, if your NPC is a door you won't be able to open and close it

<br>

## 5. Putting the NPC into the world (Intro to quest actions & stages) 
> Big checkpoint! Want to see the NPC as you play the quest? here's how:
1. Go back to your quest editor
2. Press **'Quest Stages'**

<img alt="The 'Quest Stages' button is the chest in the 4th slot" src="../../assets/docs/quest-stages-button.png" width="450"/>

3. Press **'Add Stage'**, it will create **'stage_0'**

<img alt="The 'Add Stage' button is the lime dye in the rightmost slot" src="../../assets/docs/add-stage-button.png" width="450"/>

4. Press the new **'stage_0'** button, it will show a stage editor menu
    - Stages are used to organise a bunch of related actions
5. In the stage editor menu, press **'Add Action'**

<img alt="The 'Add Action' button is the lime dye in the rightmost slot" src="../../assets/docs/add-action-button.png" width="450"/>

6. Press the new **'action_0'** button, it will show an action editor menu
    - The hopper is for setting the actions that come after this one (will be talked about later)
    - The firework is for changing the action type
    - The button is for configuring the action
    - The clock is for setting conditions for the action to continue (will be talked about later)
    - The red dye is to delete the action

<img alt="A new screen appears showing a back button, a button to set next actions, a button to change the action type, a button to change action settings, a button to edit the conditions and a delete button" src="../../assets/docs/action-editor-menu.png" width="450"/>

7. Press the **'Edit action options'** button

<img alt="The 'Edit action options' button is the stone button in the 4th slot" src="../../assets/docs/edit-actions-button.png" width="450"/>

8. Here, you can set the NPC that will idle when the action starts.
    - The action is called 'None' because it very much does nothing, you can look in the 'Change action type' to see the other options. This action also halts continuation as there is no way to 'complete' nothing.
9. Press the **'Set the NPC'** configuration option. 

<img alt="The 'Edit action options' button is the stone button in the 4th slot" src="../../assets/docs/set-the-npc-option.png" width="450"/>

10. It will show you your list of NPCs, there you just pick the NPC you want to have show up in the world.

<img alt="The 'NPC Selector' menu" src="../../assets/docs/npc-selector-npc.png" width="450"/>

11. <u>**Now we just need to set this action to start when the quest starts!**</u>
12. Press **'Back'** a few times to get back to the main **'Edit Quest'** screen
13. Press **'Set start points'**

<img alt="The 'Set start points' button is the piston in the 6th slot" src="../../assets/docs/set-start-points-button.png" width="450"/>

14. Select the action we just created
15. Go **'Back'**, and press **'Save'** to save the changes you've made to the quest 
16. The NPC you created and selected should show up where you set it!
    - The NPC will have no interaction/functionality. This is because the action it's assigned to is just **'None'**, which is an action type that has no behaviour 😸. See the next part if you're ready to add behaviour!

<br>

## 6. Making the NPC say something 🌟
> Bring your NPC to life, here's how:

###### 🔔 A stage is just a big list of actions. Like a step in your quest. For example: a stage could be a conversation with the NPC with multiple 'Speak' actions.

1. Open the quest editor
2. Navigate to the ``stage_0.action_0`` we made
3. In the action editor, press the **'Change action type'** button
    - The firework inside this menu shows the currently selected action type

<img alt="The 'Available Action Types' menu shows a back button, a firework button for the currently selected item (the 'None' action type), and a oak sign button for the 'Speak' action type" src="../../assets/docs/available-action-types-menu.png" width="450"/>

4. Press the **'Speak'** action type button, to change the action type from **'None'** to **'Speak'**
5. Go **'Back'**
6. Now when we go into **'Edit action options'** we will see suitable options to configure!
7. Make sure you set both the **'Set the NPC'** and **'Set the Dialogue'**
    - ⚠️ It won't let you exit or save if these aren't completed as they are required to avoid your quest breaking!
8. Go **'Back'**
    - If it doesn't let you, the chatbox should show a message of what you need to do. Don't exit the menu though, unless you're okay with losing your progress.

    <img alt="Example showing an error showing up behind the menu in the chat box, it says 'NPC is missing, try choosing an NPC in the action options.'" src="../../assets/docs/action-config-error.png" width="450"/>

9. **'Save'** the quest
    - If it's not working, remember to set it as the starting point action. *If you're still having troubles feel free to ask for help in the [Discord server](https://discord.gg/EvWVSn9URf)!*
10. Yay! Now the NPC should sparkle and right click should show your dialogue!
    - When I interact with it, it disappears?! Don't stress ^_^ this is intended, every action continues on when it's finished. If you'd like to avoid that forever, you can set one of the actions **'Next Actions'** as itself for a infinite loop! (if you're unsure how, read the next part for help!)

<img alt="Example showing an NPC block in the world that is indicating it can be interacted with, with a yellow sparkle above it" src="../../assets/docs/sparkly-block-npc.png" width="450"/>

<br>

## 7. Go from one action to the next action!
> Two dialogues in a row!? here's how:

1. Go back to your quest editor
2. Navigate to the ``stage_0`` quest stage we made
3. Add a new action
4. Change the action to a 'Speak' type action
    - For me it'll be ``stage_0.action_1`` (the action_1 action, in the stage_0 stage)
5. Remember to set the options for the new action
6. Now let's set up our quest to continue on to this new action! Navigate to the ``stage_0.action_0`` we made before (in the previous parts)
7. In the action editor, press the **'Next Actions'** button

<img alt="THe 'Next Actions' button is the hopper in the 2nd slot" src="../../assets/docs/next-actions-button.png" width="450"/>

8. Select at least the action you just created
    - For me i'll add action_1 from in stage_0
    - Selecting **'Select this stage'** means it will look for the **Stage start points** for the next actions (which is separate to the **Quest start points**)
    - A plain rail is unselected, a detector rail is selected by being a start point, and a powered rail is selected manually! 
9. You should now be able to play your quest, with action_0 followed by action_1!
    - As you can probably tell, next actions can be from any other stages. You could have ``stage_0.action_0`` continue to ``stage_1.action_0``
10. Simple! Now you know how to do multiple step quests
    - Remember to have the action_0 set as your quest start point in the main menu of this quest editor! Otherwise nothing will happen ^_^

###### 🔔 You can use this new knowledge to make multiple branches/endings and all sorts of other complex quest structures now!

<br>

## 8. Conditions for the action to continue, like a certain time of day!
> For extra complexity, specify requirements your actions need to continue! here's how:

1. Go back to your quest editor
2. Navigate to editing an action
3. Press **'Edit conditions'**

<img alt="The 'Edit conditions' button is a clock item in the 5th slot" src="../../assets/docs/action-edit-conditions-button.png" width="450"/>

4. In this menu, you can:
    1. View and edit your existing conditions
    2. Create new conditions

<img alt="Menu with a back button, a chest for viewing added conditions, and a blue dye for adding new conditions" src="../../assets/docs/action-conditions-menu.png" width="450"/>

5. Press **'Create New'**, to create a new condition
6. Press **'Time'**, to create a new time condition (only continue the action if within the time boundary)
7. Set the start time and the end time
    - Feel free to add multiple **Time** conditions if you need more complex time.
8. Done! That easy ^_^
    - Conditions are only checked and work, if the action has tried to be started. So make sure to remember to add the action as a **'Next action'** or a quest **'Start point'**

<br>

## 9. Rewarding a potato for completing the quest!
> Use the same process as part 6, where a Speak action was created. Instead create a 'Reward Item' action with a potato as the item option.

> You should notice it say there is missing stock and auto toggles off your quest! Here's how to restock and get your quest back into the world.

1. Go back to your quest editor, but instead of clicking edit, click 'Quest Inventory'

<img alt="Menu with a back button, a potato with 196 in the stack (suggesting the current stock of potatoes in the quest inventory), and at the bottom a chest next to two glass panes" src="../../assets/docs/quest-inventory-screen-amount.png" width="450"/>

2. Near the bottom right corner, there is a 'Restock' button represented by a chest
3. Clicking the restock button opens a place to put items into, to restock the quest
    - ⚠️ To avoid this being used as a portable chest which many servers consider not survival/a cheat, you cannot freely take back out of the quest inventory. It can only be gotten back out through it being used in a quest.

<img alt="Menu with a green dye suggesting a save button in the bottom right, the player moves 5 potatoes from their inventory into the restock window and presses save. It updates the amount of potatoes by 5 in the quest inventory." src="../../assets/docs/quest-inventory-restocking.webp" width="450"/>

4. Simply exit back out of the restock window and you'll notice the amount increases.
    - 🔔 No such thing as stacks here, you can store as much of an item as you want in one slot ^_^

5. Now re-toggle your quest back on, and if you restocked everything you should see it in the world.

###### 🔔 If the reward item action doesn't work, remember to add the action as a start point OR as the 'next' action of a previous one that already is set up to show!

<br>

## 💙 Let's make more quests together!
You have the basics now! You should be able to do just about anything this plugin offers now.

If you have any questions or anything went wrong, please let me and the community know in the [Discord server](https://discord.gg/EvWVSn9URf)!