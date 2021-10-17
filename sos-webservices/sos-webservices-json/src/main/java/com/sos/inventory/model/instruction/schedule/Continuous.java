
package com.sos.inventory.model.instruction.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Continuous
 * <p>
 * repeat with fixed property 'TYPE':'Continuous'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "pause",
    "limit"
})
public class Continuous
    extends Repeat
{

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("pause")
    private Long pause;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("limit")
    private Integer limit;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Continuous() {
    }

    /**
     * 
     * @param limit
     * @param pause
     */
    public Continuous(Long pause, Integer limit) {
        super();
        this.pause = pause;
        this.limit = limit;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("pause")
    public Long getPause() {
        return pause;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("pause")
    public void setPause(Long pause) {
        this.pause = pause;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("pause", pause).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(limit).append(pause).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Continuous) == false) {
            return false;
        }
        Continuous rhs = ((Continuous) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(limit, rhs.limit).append(pause, rhs.pause).isEquals();
    }

}
