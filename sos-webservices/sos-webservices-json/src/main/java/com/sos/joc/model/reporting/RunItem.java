
package com.sos.joc.model.reporting;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * report run
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "runId",
    "state",
    "numOfReports",
    "errorText",
    "modified"
})
public class RunItem
    extends Report
{

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("runId")
    private Long runId;
    /**
     * report run state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private ReportRunState state;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfReports")
    private Long numOfReports;
    @JsonProperty("errorText")
    private String errorText;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date modified;

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("runId")
    public Long getRunId() {
        return runId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("runId")
    public void setRunId(Long runId) {
        this.runId = runId;
    }

    /**
     * report run state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public ReportRunState getState() {
        return state;
    }

    /**
     * report run state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(ReportRunState state) {
        this.state = state;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfReports")
    public Long getNumOfReports() {
        return numOfReports;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfReports")
    public void setNumOfReports(Long numOfReports) {
        this.numOfReports = numOfReports;
    }

    @JsonProperty("errorText")
    public String getErrorText() {
        return errorText;
    }

    @JsonProperty("errorText")
    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public Date getModified() {
        return modified;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public void setModified(Date modified) {
        this.modified = modified;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("runId", runId).append("state", state).append("numOfReports", numOfReports).append("errorText", errorText).append("modified", modified).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(errorText).append(modified).append(runId).append(state).append(numOfReports).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RunItem) == false) {
            return false;
        }
        RunItem rhs = ((RunItem) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(errorText, rhs.errorText).append(modified, rhs.modified).append(runId, rhs.runId).append(state, rhs.state).append(numOfReports, rhs.numOfReports).isEquals();
    }

}
