package playerquests.utility.enums;

import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;

public enum DependencyIssue {
    MISSING("install", "installing") {
        @Override
        public void sendMessage(String dependencyExplainer, String dependencyLink) {
            ChatUtils.message("""
                Soft Dependency Reminder! ✨\n """ + dependencyExplainer + """

                🔗 """ + dependencyLink
            .strip())
            .target(MessageTarget.WORLD)
            .style(MessageStyle.PRETTY)
            .type(MessageType.NOTIF)
            .send();
        }

        @Override
        public String getRemedyVerb() {
            return "install";
        }
    },
    OUT_OF_DATE("update", "updating") {
        @Override
        public void sendMessage(String dependencyExplainer, String dependencyLink) {
            ChatUtils.message("""
                A Dependency Requires Updating! ✨\n """ + dependencyExplainer + """

                🔗 """ + dependencyLink
            .strip())
            .target(MessageTarget.WORLD)
            .style(MessageStyle.PRETTY)
            .type(MessageType.ERROR)
            .send();
        }
    };

    private final String remedyVerb;
    private final String remedyPresentPrinciple;

    DependencyIssue(String remedyVerb, String remedyPresentPrinciple) {
        this.remedyVerb = remedyVerb;
        this.remedyPresentPrinciple = remedyPresentPrinciple;
    }

    public abstract void sendMessage(String dependencyExplainer, String dependencyLink);

    public String getRemedyVerb() {
        return this.remedyVerb;
    }

    public String getRemedyPresentPrinciple() {
        return this.remedyPresentPrinciple;
    }
}
