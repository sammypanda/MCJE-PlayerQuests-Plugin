package playerquests.builder.quest.action.condition;

import java.util.List;

import org.bukkit.Material;

import com.fasterxml.jackson.annotation.JsonProperty;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.gui.function.ChatPrompt;
import playerquests.builder.quest.data.ActionData;
import playerquests.builder.quest.data.QuesterData;
import playerquests.client.ClientDirector;

public class TimeCondition extends ActionCondition {

    /**
     * Start of the time bracket.
     */
    @JsonProperty("start")
    private long startTime;

    /**
     * End of the time bracket.
     */
    @JsonProperty("end")
    private long endTime;

    /**
     * Default constructor for Jackson.
     */
    public TimeCondition() {}

    public TimeCondition(ActionData actionData) {
        super(actionData);
    }

    @Override
    public Boolean isMet(QuesterData questerData) {
        // get the in-game time of day
        long worldTime = questerData.getQuester().getPlayer().getWorld().getTime(); 

        // if within the time brackets set, the condition passes
        if (worldTime >= this.getStartTime() && worldTime <= this.getEndTime()) {
            return true;
        }

        // otherwise it does not pass
        return false;
    }

    @Override
    public GUISlot createSlot(GUIDynamic screen, GUIBuilder gui, Integer slot, ClientDirector director) {
        return new GUISlot(gui, slot)
            .setLabel("Time")
            .setItem(Material.CLOCK);
    }

    public String getName() {
        return "Time";
    }

    @Override
    public void createEditorGUI(GUIDynamic screen, GUIBuilder gui, ClientDirector director) {
        new GUISlot(gui, 3)
            .setLabel(
                String.format("Set start time (%s)", this.startTime)
            )
            .setDescription(List.of("Action can only be played after this time."))
            .setItem(Material.PACKED_ICE)
            .onClick(() -> {
                new ChatPrompt(List.of("Set the start time (0 is 6am, 13000 is 6pm, 24000 is 6am).", "none"), director)
                    .onFinish((f) -> {
                        ChatPrompt chatPrompt = (ChatPrompt) f;
                        this.setStartTime(Long.parseLong(chatPrompt.getResponse()));
                        screen.refresh();
                    }
                ).execute();
            });

        new GUISlot(gui, 4)
            .setLabel(
                String.format("Set end time (%s)", this.endTime)
            )
            .setDescription(List.of("Action can only be played before this time."))
            .setItem(Material.WATER_BUCKET)
            .onClick(() -> {
                new ChatPrompt(List.of("Set the end time (0 is 6am, 13000 is 6pm, 24000 is 6am).", "none"), director)
                    .onFinish((f) -> {
                        ChatPrompt chatPrompt = (ChatPrompt) f;
                        this.setEndTime(Long.parseLong(chatPrompt.getResponse()));
                        screen.refresh();
                    }
                ).execute();
            });
    }

    /**
     * Set the time the action is completable until.
     * @param endTime the end of the time bracket
     */
    private void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * Get the time the action is completable until.
     * @return the end of the time bracket
     */
    private long getEndTime() {
        return this.endTime;
    }

    /**
     * Set the time the action is completable from.
     * @param startTime the start of the time bracket
     */
    private void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Get the time the action is completable from.
     * @return the start of the time bracket
     */
    private long getStartTime() {
        return this.startTime;
    }
}
