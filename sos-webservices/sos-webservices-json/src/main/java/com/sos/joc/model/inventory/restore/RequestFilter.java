
package com.sos.joc.model.inventory.restore;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * filter for restore
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "suffix",
    "prefix"
})
public class RequestFilter
    extends com.sos.joc.model.inventory.common.RequestFilter
{

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("suffix")
    private String suffix;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("prefix")
    private String prefix;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("suffix")
    public String getSuffix() {
        return suffix;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("suffix")
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("prefix")
    public String getPrefix() {
        return prefix;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("prefix")
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("suffix", suffix).append("prefix", prefix).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(suffix).append(prefix).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestFilter) == false) {
            return false;
        }
        RequestFilter rhs = ((RequestFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(suffix, rhs.suffix).append(prefix, rhs.prefix).isEquals();
    }

}
