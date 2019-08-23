
package com.sos.joc.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "available",
    "exclusive"
})
public class LockUseV {

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("available")
    private Boolean available;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("exclusive")
    private Boolean exclusive;

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("available")
    public Boolean getAvailable() {
        return available;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("available")
    public void setAvailable(Boolean available) {
        this.available = available;
    }

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("available", available).append("exclusive", exclusive).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(available).append(path).append(exclusive).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LockUseV) == false) {
            return false;
        }
        LockUseV rhs = ((LockUseV) other);
        return new EqualsBuilder().append(available, rhs.available).append(path, rhs.path).append(exclusive, rhs.exclusive).isEquals();
    }

}
