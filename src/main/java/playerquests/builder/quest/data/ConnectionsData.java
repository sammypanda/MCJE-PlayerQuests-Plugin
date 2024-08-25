package playerquests.builder.quest.data;

import com.fasterxml.jackson.annotation.JsonIgnore; // used to ignore fields in serialisation

/**
 * Represents a map of quest connections to form a network/quest plan.
 * 
 * This class maintains references to the next, current, and previous stages or actions 
 * in a quest progression. It allows for defining the flow between different stages 
 * in a quest, including what happens on success, exit, or failure.
 */
public class ConnectionsData {
    private StagePath next;
    private StagePath curr;
    private StagePath prev;

    /**
     * Default constructor for Jackson serialization.
     */
    public ConnectionsData() {}

    /**
     * Constructs a new ConnectionsData with the specified parameters.
     *
     * @param next the stage or action to proceed to if the current stage succeeds
     * @param curr the stage or action to return to if the current stage is exited
     * @param prev the stage or action to go to if the current stage fails
     */
    public ConnectionsData(StagePath next, StagePath curr, StagePath prev) {
        this.next = next;
        this.curr = curr;
        this.prev = prev;
    }

    /**
     * Gets the stage or action to proceed to if the current stage succeeds.
     * 
     * @return the next stage or action
     */
    public StagePath getNext() {
        return this.next;
    }

    /**
     * Sets the stage or action to proceed to if the current stage succeeds.
     * 
     * @param next the next stage or action
     */
    public void setNext(StagePath next) {
        this.next = next;
    }

    /**
     * Gets the stage or action to return to if the current stage is exited.
     * 
     * @return the on-exit stage or action
     */
    public StagePath getCurr() {
        return this.curr;
    }

    /**
     * Sets the stage or action to return to if the current stage is exited.
     * 
     * @param curr the on-exit stage or action
     */
    public void setCurr(StagePath curr) {
        this.curr = curr;
    }

    /**
     * Gets the stage or action to go to if the current stage fails.
     * 
     * @return the prior stage or action
     */
    public StagePath getPrev() {
        return this.prev;
    }

    /**
     * Sets the stage or action to go to if the current stage fails.
     * 
     * @param prev the prior stage or action
     */
    public void setPrev(StagePath prev) {
        this.prev = prev;
    }

    /**
     * Returns a string representation of the ConnectionsData object.
     * 
     * @return a string representation of the ConnectionsData object, including
     *         the next, current, and previous stages or actions
     */
    @Override
    public String toString() {
        return "Connections{" +
                "next='" + next +
                ", curr=" + curr +
                ", prev=" + prev +
        '}';
    }

    /**
     * Checks if all the stage paths (next, current, and previous) are null.
     * 
     * @return true if all stage paths are null, false otherwise
     */
    @JsonIgnore
    public Boolean isEmpty() {
        return 
            this.curr == null && 
            this.next == null && 
            this.prev == null;
    }
}
