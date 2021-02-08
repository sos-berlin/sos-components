
package com.sos.joc.model.lock.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * lock in a workflow
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "type",
    "count"
})
public class WorkflowLock {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private String id;
    /**
     * lock type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private WorkflowLockType type;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("count")
    private Integer count;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * lock type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public WorkflowLockType getType() {
        return type;
    }

    /**
     * lock type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(WorkflowLockType type) {
        this.type = type;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("count")
    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("type", type).append("count", count).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(count).append(id).append(type).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowLock) == false) {
            return false;
        }
        WorkflowLock rhs = ((WorkflowLock) other);
        return new EqualsBuilder().append(count, rhs.count).append(id, rhs.id).append(type, rhs.type).isEquals();
    }

}
