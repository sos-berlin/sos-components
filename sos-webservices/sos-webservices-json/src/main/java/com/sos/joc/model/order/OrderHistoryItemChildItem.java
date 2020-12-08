
package com.sos.joc.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.job.TaskHistoryItem;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order object in history collection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "task",
    "order"
})
public class OrderHistoryItemChildItem {

    /**
     * task in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("task")
    private TaskHistoryItem task;
    /**
     * order object in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("order")
    private OrderHistoryItem order;

    /**
     * task in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("task")
    public TaskHistoryItem getTask() {
        return task;
    }

    /**
     * task in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("task")
    public void setTask(TaskHistoryItem task) {
        this.task = task;
    }

    /**
     * order object in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("order")
    public OrderHistoryItem getOrder() {
        return order;
    }

    /**
     * order object in history collection
     * <p>
     * 
     * 
     */
    @JsonProperty("order")
    public void setOrder(OrderHistoryItem order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("task", task).append("order", order).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(task).append(order).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderHistoryItemChildItem) == false) {
            return false;
        }
        OrderHistoryItemChildItem rhs = ((OrderHistoryItemChildItem) other);
        return new EqualsBuilder().append(task, rhs.task).append(order, rhs.order).isEquals();
    }

}
