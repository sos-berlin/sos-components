
package com.sos.joc.model.reporting;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * dateFrom filter for loading of reporting data
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ids",
    "compact"
})
public class ReportHistoryFilter {

    @JsonProperty("ids")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Long> ids = new LinkedHashSet<Long>();
    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object's data is compact or detailed")
    private Boolean compact = false;

    @JsonProperty("ids")
    public Set<Long> getIds() {
        return ids;
    }

    @JsonProperty("ids")
    public void setIds(Set<Long> ids) {
        this.ids = ids;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("ids", ids).append("compact", compact).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(compact).append(ids).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReportHistoryFilter) == false) {
            return false;
        }
        ReportHistoryFilter rhs = ((ReportHistoryFilter) other);
        return new EqualsBuilder().append(compact, rhs.compact).append(ids, rhs.ids).isEquals();
    }

}
