
package com.sos.joc.model.dailyplan;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * DailyPlanHistoryControllerItem
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "submissions"
})
public class DailyPlanHistoryControllerItem {

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("submissions")
    private List<DailyPlanSubmissionTimes> submissions = null;

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("submissions")
    public List<DailyPlanSubmissionTimes> getSubmissions() {
        return submissions;
    }

    @JsonProperty("submissions")
    public void setSubmissions(List<DailyPlanSubmissionTimes> submissions) {
        this.submissions = submissions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("submissions", submissions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(submissions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanHistoryControllerItem) == false) {
            return false;
        }
        DailyPlanHistoryControllerItem rhs = ((DailyPlanHistoryControllerItem) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(submissions, rhs.submissions).isEquals();
    }

}
