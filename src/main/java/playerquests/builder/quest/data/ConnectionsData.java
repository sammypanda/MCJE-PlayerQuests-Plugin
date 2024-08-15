package playerquests.builder.quest.data;

import com.fasterxml.jackson.annotation.JsonIgnore; // used to ignore fields in serialisation

/**
 * Represents a map of quest connections 
 * to form a network/quest plan.
 */
public class ConnectionsData {
    private StagePath next;
    private StagePath curr;
    private StagePath prev;

    /**
     * Defaut constructor (for Jackson)
     */
    public ConnectionsData() {}

    /**
     * Constructs a new Location with the specified parameters.
     *
     * @param next where to go if the stage succeeds
     * @param curr where to return to if the stage is exited
     * @param prev where to go if the stage fails
     */
    public ConnectionsData(StagePath next, StagePath curr, StagePath prev) {
        this.next = next;
        this.curr = curr;
        this.prev = prev;
    }

    /**
     * Gets the next quest stage or action.
     * @return next stage or action.
     */
    public StagePath getNext() {
        return this.next;
    }

    /**
     * Sets the next quest stage or action.
     * @param next a quest stage or action.
     */
    public void setNext(StagePath next) {
        this.next = next;
    }

    /**
     * Gets the on-exit quest stage or action.
     * @return on-exit stage or action.
     */
    public StagePath getCurr() {
        return this.curr;
    }

    /**
     * Sets the on-exit quest stage or action.
     * @param next a quest stage or action.
     */
    public void setCurr(StagePath curr) {
        this.curr = curr;
    }

    /**
     * Gets the prior quest stage or action.
     * @return prior stage or action.
     */
    public StagePath getPrev() {
        return this.prev;
    }

    /**
     * Sets the prior quest stage or action.
     * @param next a quest stage or action.
     */
    public void setPrev(StagePath prev) {
        this.prev = prev;
    }

    /**
     * Returns a string representation of the Location object.
     * @return a string representation of the Location object
     */
    @Override
    public String toString() {
        return "Connections{" +
                "next='" + next +
                ", curr=" + curr +
                ", prev=" + prev +
                '}';
    }

    @JsonIgnore
    public Boolean isEmpty() {
        return 
            this.curr == null && 
            this.next == null && 
            this.prev == null;
    }
}
