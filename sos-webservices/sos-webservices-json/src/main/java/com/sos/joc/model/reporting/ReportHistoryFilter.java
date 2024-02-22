
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
 * report history
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ids",
    "compact",
    "dateFrom",
    "dateTo"
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
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateFrom")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String dateFrom;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateTo")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String dateTo;

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

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("ids", ids).append("compact", compact).append("dateFrom", dateFrom).append("dateTo", dateTo).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dateTo).append(ids).append(compact).append(dateFrom).toHashCode();
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
        return new EqualsBuilder().append(dateTo, rhs.dateTo).append(ids, rhs.ids).append(compact, rhs.compact).append(dateFrom, rhs.dateFrom).isEquals();
    }

}
