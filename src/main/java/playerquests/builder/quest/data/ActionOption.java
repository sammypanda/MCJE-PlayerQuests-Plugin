package playerquests.builder.quest.data;

public enum ActionOption {
    NPC("Set NPC", "VILLAGER_SPAWN_EGG");

    private final String label;
    private final String item;

    ActionOption(String label, String item) {
        this.label = label;
        this.item = item;
    }

    public String getLabel() {
        return this.label;
    }

    public String getItem() {
        return this.item;
    }
}
