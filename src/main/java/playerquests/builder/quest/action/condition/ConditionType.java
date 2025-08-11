package playerquests.builder.quest.action.condition;

public enum ConditionType {
    COMPLETION(CompletionCondition.class),
    TIME(TimeCondition.class);

    private final Class<? extends ActionCondition> conditionClass;

    ConditionType(Class<? extends ActionCondition> conditionClass) {
        this.conditionClass = conditionClass;
    }

    public Class<? extends ActionCondition> getConditionClass() {
        return this.conditionClass;
    }
}
