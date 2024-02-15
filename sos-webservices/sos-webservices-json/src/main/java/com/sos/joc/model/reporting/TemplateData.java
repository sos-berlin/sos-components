
package com.sos.joc.model.reporting;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "chartType",
    "groupBy"
})
public class TemplateData {

    /**
     * BAR, HORIZONTAL_BAR, LINE
     * (Required)
     * 
     */
    @JsonProperty("chartType")
    @JsonPropertyDescription("BAR, HORIZONTAL_BAR, LINE")
    private String chartType;
    /**
     * START_TIME, ORDER_ID, JOB_NAME, WORKFLOW_NAME
     * (Required)
     * 
     */
    @JsonProperty("groupBy")
    @JsonPropertyDescription("START_TIME, ORDER_ID, JOB_NAME, WORKFLOW_NAME")
    private String groupBy;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * BAR, HORIZONTAL_BAR, LINE
     * (Required)
     * 
     */
    @JsonProperty("chartType")
    public String getChartType() {
        return chartType;
    }

    /**
     * BAR, HORIZONTAL_BAR, LINE
     * (Required)
     * 
     */
    @JsonProperty("chartType")
    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    /**
     * START_TIME, ORDER_ID, JOB_NAME, WORKFLOW_NAME
     * (Required)
     * 
     */
    @JsonProperty("groupBy")
    public String getGroupBy() {
        return groupBy;
    }

    /**
     * START_TIME, ORDER_ID, JOB_NAME, WORKFLOW_NAME
     * (Required)
     * 
     */
    @JsonProperty("groupBy")
    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
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
        return new ToStringBuilder(this).append("chartType", chartType).append("groupBy", groupBy).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(chartType).append(groupBy).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TemplateData) == false) {
            return false;
        }
        TemplateData rhs = ((TemplateData) other);
        return new EqualsBuilder().append(chartType, rhs.chartType).append(groupBy, rhs.groupBy).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
