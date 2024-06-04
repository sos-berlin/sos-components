
package com.sos.joc.model.reporting;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.inventory.model.report.TemplateId;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * generated reports
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "runIds",
    "compact",
    "dateFrom",
    "dateTo",
    "templateNames"
})
public class ReportHistoryFilter
    extends ReportPaths
{

    @JsonProperty("runIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Long> runIds = new LinkedHashSet<Long>();
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
    @JsonProperty("templateNames")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<TemplateId> templateNames = new LinkedHashSet<TemplateId>();

    @JsonProperty("runIds")
    public Set<Long> getRunIds() {
        return runIds;
    }

    @JsonProperty("runIds")
    public void setRunIds(Set<Long> runIds) {
        this.runIds = runIds;
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

    @JsonProperty("templateNames")
    public Set<TemplateId> getTemplateNames() {
        return templateNames;
    }

    @JsonProperty("templateNames")
    public void setTemplateNames(Set<TemplateId> templateNames) {
        this.templateNames = templateNames;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("runIds", runIds).append("compact", compact).append("dateFrom", dateFrom).append("dateTo", dateTo).append("templateNames", templateNames).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(dateTo).append(runIds).append(templateNames).append(compact).append(dateFrom).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(dateTo, rhs.dateTo).append(runIds, rhs.runIds).append(templateNames, rhs.templateNames).append(compact, rhs.compact).append(dateFrom, rhs.dateFrom).isEquals();
    }

}
