
package com.sos.joc.model.job;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * running tasks
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "tasks"
})
public class RunningTaskLogsFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tasks")
    private List<RunningTaskLogFilter> tasks = new ArrayList<RunningTaskLogFilter>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tasks")
    public List<RunningTaskLogFilter> getTasks() {
        return tasks;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("tasks")
    public void setTasks(List<RunningTaskLogFilter> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("tasks", tasks).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(tasks).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RunningTaskLogsFilter) == false) {
            return false;
        }
        RunningTaskLogsFilter rhs = ((RunningTaskLogsFilter) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(tasks, rhs.tasks).isEquals();
    }

}
