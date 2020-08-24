
package com.sos.joc.model.lock;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "exclusive",
    "tasks"
})
public class LockHolders {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("exclusive")
    private Boolean exclusive;
    /**
     * Collection of tasks which are now using the lock
     * (Required)
     * 
     */
    @JsonProperty("tasks")
    @JsonPropertyDescription("Collection of tasks which are now using the lock")
    private List<LockHolder> tasks = new ArrayList<LockHolder>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("exclusive")
    public Boolean getExclusive() {
        return exclusive;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("exclusive")
    public void setExclusive(Boolean exclusive) {
        this.exclusive = exclusive;
    }

    /**
     * Collection of tasks which are now using the lock
     * (Required)
     * 
     */
    @JsonProperty("tasks")
    public List<LockHolder> getTasks() {
        return tasks;
    }

    /**
     * Collection of tasks which are now using the lock
     * (Required)
     * 
     */
    @JsonProperty("tasks")
    public void setTasks(List<LockHolder> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("exclusive", exclusive).append("tasks", tasks).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(exclusive).append(tasks).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LockHolders) == false) {
            return false;
        }
        LockHolders rhs = ((LockHolders) other);
        return new EqualsBuilder().append(exclusive, rhs.exclusive).append(tasks, rhs.tasks).isEquals();
    }

}
