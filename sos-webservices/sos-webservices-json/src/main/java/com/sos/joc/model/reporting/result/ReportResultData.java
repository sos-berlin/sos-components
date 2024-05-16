
package com.sos.joc.model.reporting.result;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * report result data item
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "workflow_name",
    "count",
    "data"
})
public class ReportResultData {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflow_name")
    private String workflow_name;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("count")
    private Long count;
    @JsonProperty("data")
    private List<ReportResultDataItem> data = new ArrayList<ReportResultDataItem>();

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflow_name")
    public String getWorkflow_name() {
        return workflow_name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflow_name")
    public void setWorkflow_name(String workflow_name) {
        this.workflow_name = workflow_name;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("count")
    public Long getCount() {
        return count;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("count")
    public void setCount(Long count) {
        this.count = count;
    }

    @JsonProperty("data")
    public List<ReportResultDataItem> getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(List<ReportResultDataItem> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("workflow_name", workflow_name).append("count", count).append("data", data).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(count).append(data).append(workflow_name).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReportResultData) == false) {
            return false;
        }
        ReportResultData rhs = ((ReportResultData) other);
        return new EqualsBuilder().append(count, rhs.count).append(data, rhs.data).append(workflow_name, rhs.workflow_name).isEquals();
    }

}
