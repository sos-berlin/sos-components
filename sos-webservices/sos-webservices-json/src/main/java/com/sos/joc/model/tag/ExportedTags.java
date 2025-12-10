
package com.sos.joc.model.tag;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Ex-/Import schema for tags
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "tags",
    "jobTags",
    "orderTags"
})
public class ExportedTags {

    @JsonProperty("tags")
    private List<ExportedTagItem> tags = new ArrayList<ExportedTagItem>();
    @JsonProperty("jobTags")
    private List<ExportedJobTagItem> jobTags = new ArrayList<ExportedJobTagItem>();
    @JsonProperty("orderTags")
    private ExportedOrderTags orderTags;

    @JsonProperty("tags")
    public List<ExportedTagItem> getTags() {
        return tags;
    }

    @JsonProperty("tags")
    public void setTags(List<ExportedTagItem> tags) {
        this.tags = tags;
    }

    @JsonProperty("jobTags")
    public List<ExportedJobTagItem> getJobTags() {
        return jobTags;
    }

    @JsonProperty("jobTags")
    public void setJobTags(List<ExportedJobTagItem> jobTags) {
        this.jobTags = jobTags;
    }

    @JsonProperty("orderTags")
    public ExportedOrderTags getOrderTags() {
        return orderTags;
    }

    @JsonProperty("orderTags")
    public void setOrderTags(ExportedOrderTags orderTags) {
        this.orderTags = orderTags;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tags", tags).append("jobTags", jobTags).append("orderTags", orderTags).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobTags).append(tags).append(orderTags).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExportedTags) == false) {
            return false;
        }
        ExportedTags rhs = ((ExportedTags) other);
        return new EqualsBuilder().append(jobTags, rhs.jobTags).append(tags, rhs.tags).append(orderTags, rhs.orderTags).isEquals();
    }

}
