
package com.sos.inventory.model.filetransfer.profile.target;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "atomicPrefix",
    "atomicSuffix"
})
public class Atomicity {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("atomicPrefix")
    private String atomicPrefix;
    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("atomicSuffix")
    private String atomicSuffix;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Atomicity() {
    }

    /**
     * 
     * @param atomicPrefix
     * @param atomicSuffix
     */
    public Atomicity(String atomicPrefix, String atomicSuffix) {
        super();
        this.atomicPrefix = atomicPrefix;
        this.atomicSuffix = atomicSuffix;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("atomicPrefix")
    public String getAtomicPrefix() {
        return atomicPrefix;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("atomicPrefix")
    public void setAtomicPrefix(String atomicPrefix) {
        this.atomicPrefix = atomicPrefix;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("atomicSuffix")
    public String getAtomicSuffix() {
        return atomicSuffix;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("atomicSuffix")
    public void setAtomicSuffix(String atomicSuffix) {
        this.atomicSuffix = atomicSuffix;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("atomicPrefix", atomicPrefix).append("atomicSuffix", atomicSuffix).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(atomicPrefix).append(atomicSuffix).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Atomicity) == false) {
            return false;
        }
        Atomicity rhs = ((Atomicity) other);
        return new EqualsBuilder().append(atomicPrefix, rhs.atomicPrefix).append(atomicSuffix, rhs.atomicSuffix).isEquals();
    }

}
