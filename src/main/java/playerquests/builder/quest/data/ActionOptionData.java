package playerquests.builder.quest.data;

import java.util.HashSet; // hash table set type
import java.util.Set; // generic set type

public class ActionOptionData {

    Set<ActionOption> options = new HashSet<ActionOption>();

    public void add(ActionOption option) {
        options.add(option);
    }

    public Set<ActionOption> getOptions() {
        return options;
    }
}
