
package com.sos.joc.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * workflow summary
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "notSynchronized",
    "synchronized",
    "suspended",
    "outstanding"
})
public class WorkflowsSummary {

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("notSynchronized")
    private Integer notSynchronized;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("synchronized")
    private Integer _synchronized;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("suspended")
    private Integer suspended;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("outstanding")
    private Integer outstanding;

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("notSynchronized")
    public Integer getNotSynchronized() {
        return notSynchronized;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("notSynchronized")
    public void setNotSynchronized(Integer notSynchronized) {
        this.notSynchronized = notSynchronized;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("synchronized")
    public Integer getSynchronized() {
        return _synchronized;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("synchronized")
    public void setSynchronized(Integer _synchronized) {
        this._synchronized = _synchronized;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("suspended")
    public Integer getSuspended() {
        return suspended;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("suspended")
    public void setSuspended(Integer suspended) {
        this.suspended = suspended;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("outstanding")
    public Integer getOutstanding() {
        return outstanding;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("outstanding")
    public void setOutstanding(Integer outstanding) {
        this.outstanding = outstanding;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("notSynchronized", notSynchronized).append("_synchronized", _synchronized).append("suspended", suspended).append("outstanding", outstanding).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(outstanding).append(notSynchronized).append(suspended).append(_synchronized).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowsSummary) == false) {
            return false;
        }
        WorkflowsSummary rhs = ((WorkflowsSummary) other);
        return new EqualsBuilder().append(outstanding, rhs.outstanding).append(notSynchronized, rhs.notSynchronized).append(suspended, rhs.suspended).append(_synchronized, rhs._synchronized).isEquals();
    }

}
