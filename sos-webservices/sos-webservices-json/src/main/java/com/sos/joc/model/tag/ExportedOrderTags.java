
package com.sos.joc.model.tag;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "fileOrderSources",
    "schedules",
    "workflows"
})
public class ExportedOrderTags {

    @JsonProperty("fileOrderSources")
    private List<FileOrderSourceOrderTags> fileOrderSources = new ArrayList<FileOrderSourceOrderTags>();
    @JsonProperty("schedules")
    private List<ScheduleOrderTags> schedules = new ArrayList<ScheduleOrderTags>();
    @JsonProperty("workflows")
    private List<AddOrdersOrderTags> workflows = new ArrayList<AddOrdersOrderTags>();

    @JsonProperty("fileOrderSources")
    public List<FileOrderSourceOrderTags> getFileOrderSources() {
        return fileOrderSources;
    }

    @JsonProperty("fileOrderSources")
    public void setFileOrderSources(List<FileOrderSourceOrderTags> fileOrderSources) {
        this.fileOrderSources = fileOrderSources;
    }

    @JsonProperty("schedules")
    public List<ScheduleOrderTags> getSchedules() {
        return schedules;
    }

    @JsonProperty("schedules")
    public void setSchedules(List<ScheduleOrderTags> schedules) {
        this.schedules = schedules;
    }

    @JsonProperty("workflows")
    public List<AddOrdersOrderTags> getWorkflows() {
        return workflows;
    }

    @JsonProperty("workflows")
    public void setWorkflows(List<AddOrdersOrderTags> workflows) {
        this.workflows = workflows;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("fileOrderSources", fileOrderSources).append("schedules", schedules).append("workflows", workflows).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fileOrderSources).append(workflows).append(schedules).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExportedOrderTags) == false) {
            return false;
        }
        ExportedOrderTags rhs = ((ExportedOrderTags) other);
        return new EqualsBuilder().append(fileOrderSources, rhs.fileOrderSources).append(workflows, rhs.workflows).append(schedules, rhs.schedules).isEquals();
    }

}
