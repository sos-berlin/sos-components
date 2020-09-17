
package com.sos.joc.model.dailyplan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Daily Plan  Order Filter
 * <p>
 * To get orders from the daily plan
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "overwrite",
    "submissionHistoryId",
    "orderTemplates",
    "orderKeys",
    "dailyPlanDate",
    "orderTemplatesFolder"
})
public class DailyPlanOrderFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * overwrite parameter
     * <p>
     * controls if the order should be overwritten
     * 
     */
    @JsonProperty("overwrite")
    @JsonPropertyDescription("controls if the order should be overwritten")
    private Boolean overwrite = false;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("submissionHistoryId")
    private Long submissionHistoryId;
    @JsonProperty("orderTemplates")
    private List<String> orderTemplates = null;
    @JsonProperty("orderKeys")
    private List<String> orderKeys = null;
    @JsonProperty("dailyPlanDate")
    private String dailyPlanDate;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("orderTemplatesFolder")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String orderTemplatesFolder;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * overwrite parameter
     * <p>
     * controls if the order should be overwritten
     * 
     */
    @JsonProperty("overwrite")
    public Boolean getOverwrite() {
        return overwrite;
    }

    /**
     * overwrite parameter
     * <p>
     * controls if the order should be overwritten
     * 
     */
    @JsonProperty("overwrite")
    public void setOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("submissionHistoryId")
    public Long getSubmissionHistoryId() {
        return submissionHistoryId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("submissionHistoryId")
    public void setSubmissionHistoryId(Long submissionHistoryId) {
        this.submissionHistoryId = submissionHistoryId;
    }

    @JsonProperty("orderTemplates")
    public List<String> getOrderTemplates() {
        return orderTemplates;
    }

    @JsonProperty("orderTemplates")
    public void setOrderTemplates(List<String> orderTemplates) {
        this.orderTemplates = orderTemplates;
    }

    @JsonProperty("orderKeys")
    public List<String> getOrderKeys() {
        return orderKeys;
    }

    @JsonProperty("orderKeys")
    public void setOrderKeys(List<String> orderKeys) {
        this.orderKeys = orderKeys;
    }

    @JsonProperty("dailyPlanDate")
    public String getDailyPlanDate() {
        return dailyPlanDate;
    }

    @JsonProperty("dailyPlanDate")
    public void setDailyPlanDate(String dailyPlanDate) {
        this.dailyPlanDate = dailyPlanDate;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("orderTemplatesFolder")
    public String getOrderTemplatesFolder() {
        return orderTemplatesFolder;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("orderTemplatesFolder")
    public void setOrderTemplatesFolder(String orderTemplatesFolder) {
        this.orderTemplatesFolder = orderTemplatesFolder;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("overwrite", overwrite).append("submissionHistoryId", submissionHistoryId).append("orderTemplates", orderTemplates).append("orderKeys", orderKeys).append("dailyPlanDate", dailyPlanDate).append("orderTemplatesFolder", orderTemplatesFolder).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(orderTemplatesFolder).append(dailyPlanDate).append(controllerId).append(orderKeys).append(submissionHistoryId).append(additionalProperties).append(orderTemplates).append(overwrite).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanOrderFilter) == false) {
            return false;
        }
        DailyPlanOrderFilter rhs = ((DailyPlanOrderFilter) other);
        return new EqualsBuilder().append(orderTemplatesFolder, rhs.orderTemplatesFolder).append(dailyPlanDate, rhs.dailyPlanDate).append(controllerId, rhs.controllerId).append(orderKeys, rhs.orderKeys).append(submissionHistoryId, rhs.submissionHistoryId).append(additionalProperties, rhs.additionalProperties).append(orderTemplates, rhs.orderTemplates).append(overwrite, rhs.overwrite).isEquals();
    }

}
