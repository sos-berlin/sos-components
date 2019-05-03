
package com.sos.jobscheduler.model.command;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "maximum",
    "total",
    "free"
})
public class Memory {

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maximum")
    private Integer maximum;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("total")
    private Integer total;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("free")
    private Integer free;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maximum")
    public Integer getMaximum() {
        return maximum;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maximum")
    public void setMaximum(Integer maximum) {
        this.maximum = maximum;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("total")
    public Integer getTotal() {
        return total;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("total")
    public void setTotal(Integer total) {
        this.total = total;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("free")
    public Integer getFree() {
        return free;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("free")
    public void setFree(Integer free) {
        this.free = free;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("maximum", maximum).append("total", total).append("free", free).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(maximum).append(total).append(additionalProperties).append(free).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Memory) == false) {
            return false;
        }
        Memory rhs = ((Memory) other);
        return new EqualsBuilder().append(maximum, rhs.maximum).append(total, rhs.total).append(additionalProperties, rhs.additionalProperties).append(free, rhs.free).isEquals();
    }

}
