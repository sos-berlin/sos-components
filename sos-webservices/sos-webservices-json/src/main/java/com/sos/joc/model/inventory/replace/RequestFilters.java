
package com.sos.joc.model.inventory.replace;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * filter for replace
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "search",
    "replace"
})
public class RequestFilters
    extends com.sos.joc.model.inventory.common.RequestFilters
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("search")
    private String search;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("replace")
    private String replace;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("search")
    public String getSearch() {
        return search;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("search")
    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("replace")
    public String getReplace() {
        return replace;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("replace")
    public void setReplace(String replace) {
        this.replace = replace;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("search", search).append("replace", replace).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(replace).append(search).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestFilters) == false) {
            return false;
        }
        RequestFilters rhs = ((RequestFilters) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(replace, rhs.replace).append(search, rhs.search).isEquals();
    }

}
