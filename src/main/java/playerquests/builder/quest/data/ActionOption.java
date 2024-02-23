package playerquests.builder.quest.data;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;

public enum ActionOption {
    NPC("Set NPC", "VILLAGER_SPAWN_EGG", () -> System.out.println("[PlayerQuests] NPC Setter"));

    private final String label;
    private final String item;
    private Runnable onClick;

    ActionOption(String label, String item, Runnable onClick) {
        this.label = label;
        this.item = item;
        this.onClick = onClick;
    }

    public void getGUISlot(GUIBuilder gui, Integer slot) {
        new GUISlot(gui, slot)
            .setLabel(this.label)
            .setItem(this.item)
            .onClick(this.onClick);
    }
}
