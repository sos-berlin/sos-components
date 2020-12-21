
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter For The Deployment History
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "compactFilter",
    "detailFilter"
})
public class ShowDepHistoryFilter {

    /**
     * compact Filter For The Deployment History
     * <p>
     * 
     * 
     */
    @JsonProperty("compactFilter")
    private DepHistoryCompactFilter compactFilter;
    /**
     * detail Filter For The Deployment History
     * <p>
     * 
     * 
     */
    @JsonProperty("detailFilter")
    private DepHistoryDetailFilter detailFilter;

    /**
     * compact Filter For The Deployment History
     * <p>
     * 
     * 
     */
    @JsonProperty("compactFilter")
    public DepHistoryCompactFilter getCompactFilter() {
        return compactFilter;
    }

    /**
     * compact Filter For The Deployment History
     * <p>
     * 
     * 
     */
    @JsonProperty("compactFilter")
    public void setCompactFilter(DepHistoryCompactFilter compactFilter) {
        this.compactFilter = compactFilter;
    }

    /**
     * detail Filter For The Deployment History
     * <p>
     * 
     * 
     */
    @JsonProperty("detailFilter")
    public DepHistoryDetailFilter getDetailFilter() {
        return detailFilter;
    }

    /**
     * detail Filter For The Deployment History
     * <p>
     * 
     * 
     */
    @JsonProperty("detailFilter")
    public void setDetailFilter(DepHistoryDetailFilter detailFilter) {
        this.detailFilter = detailFilter;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("compactFilter", compactFilter).append("detailFilter", detailFilter).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(compactFilter).append(detailFilter).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ShowDepHistoryFilter) == false) {
            return false;
        }
        ShowDepHistoryFilter rhs = ((ShowDepHistoryFilter) other);
        return new EqualsBuilder().append(compactFilter, rhs.compactFilter).append(detailFilter, rhs.detailFilter).isEquals();
    }

}
