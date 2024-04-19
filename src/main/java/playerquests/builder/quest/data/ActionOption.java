package playerquests.builder.quest.data;

public enum ActionOption {
    NPC("Set NPC", "VILLAGER_SPAWN_EGG"), 
    DIALOGUE("Set Dialogue", "OAK_SIGN");

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
