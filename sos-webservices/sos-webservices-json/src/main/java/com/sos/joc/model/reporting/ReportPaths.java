
package com.sos.joc.model.reporting;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * parameter for report runs
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "reportPaths"
})
public class ReportPaths {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("reportPaths")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> reportPaths = new LinkedHashSet<String>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("reportPaths")
    public Set<String> getReportPaths() {
        return reportPaths;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("reportPaths")
    public void setReportPaths(Set<String> reportPaths) {
        this.reportPaths = reportPaths;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("reportPaths", reportPaths).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(reportPaths).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReportPaths) == false) {
            return false;
        }
        ReportPaths rhs = ((ReportPaths) other);
        return new EqualsBuilder().append(reportPaths, rhs.reportPaths).isEquals();
    }

}
