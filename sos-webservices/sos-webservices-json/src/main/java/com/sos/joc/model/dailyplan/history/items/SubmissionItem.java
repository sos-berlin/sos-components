
package com.sos.joc.model.dailyplan.history.items;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * date object in daily plan history collection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "submissionTime",
    "countTotal",
    "countSubmitted"
})
public class SubmissionItem {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("submissionTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date submissionTime;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countTotal")
    private Long countTotal;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countSubmitted")
    private Long countSubmitted;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("submissionTime")
    public Date getSubmissionTime() {
        return submissionTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("submissionTime")
    public void setSubmissionTime(Date submissionTime) {
        this.submissionTime = submissionTime;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countTotal")
    public Long getCountTotal() {
        return countTotal;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countTotal")
    public void setCountTotal(Long countTotal) {
        this.countTotal = countTotal;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countSubmitted")
    public Long getCountSubmitted() {
        return countSubmitted;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("countSubmitted")
    public void setCountSubmitted(Long countSubmitted) {
        this.countSubmitted = countSubmitted;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("submissionTime", submissionTime).append("countTotal", countTotal).append("countSubmitted", countSubmitted).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(countTotal).append(submissionTime).append(countSubmitted).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SubmissionItem) == false) {
            return false;
        }
        SubmissionItem rhs = ((SubmissionItem) other);
        return new EqualsBuilder().append(countTotal, rhs.countTotal).append(submissionTime, rhs.submissionTime).append(countSubmitted, rhs.countSubmitted).isEquals();
    }

}
