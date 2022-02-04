
package com.sos.joc.model.dailyplan.submissions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.dailyplan.submissions.items.SubmissionItem;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * daily plan submissions response
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "submissionHistoryItems"
})
public class SubmissionsResponse {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("submissionHistoryItems")
    private List<SubmissionItem> submissionHistoryItems = new ArrayList<SubmissionItem>();

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("submissionHistoryItems")
    public List<SubmissionItem> getSubmissionHistoryItems() {
        return submissionHistoryItems;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("submissionHistoryItems")
    public void setSubmissionHistoryItems(List<SubmissionItem> submissionHistoryItems) {
        this.submissionHistoryItems = submissionHistoryItems;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("submissionHistoryItems", submissionHistoryItems).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(submissionHistoryItems).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SubmissionsResponse) == false) {
            return false;
        }
        SubmissionsResponse rhs = ((SubmissionsResponse) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(submissionHistoryItems, rhs.submissionHistoryItems).isEquals();
    }

}
