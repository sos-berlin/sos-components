
package com.sos.webservices.order.initiator.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Order Template Filter
 * <p>
 * The filter for the list of order template for scheduling orders to JobScheduler
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "orderTemplatePath",
    "folder",
    "recursive"
})
public class OrderTemplateFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("orderTemplatePath")
    private String orderTemplatePath;
    @JsonProperty("folder")
    private String folder;
    @JsonProperty("recursive")
    private Boolean recursive;

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

    @JsonProperty("orderTemplatePath")
    public String getOrderTemplatePath() {
        return orderTemplatePath;
    }

    @JsonProperty("orderTemplatePath")
    public void setOrderTemplatePath(String orderTemplatePath) {
        this.orderTemplatePath = orderTemplatePath;
    }

    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    @JsonProperty("recursive")
    public Boolean getRecursive() {
        return recursive;
    }

    @JsonProperty("recursive")
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("orderTemplatePath", orderTemplatePath).append("folder", folder).append("recursive", recursive).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folder).append(controllerId).append(orderTemplatePath).append(recursive).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderTemplateFilter) == false) {
            return false;
        }
        OrderTemplateFilter rhs = ((OrderTemplateFilter) other);
        return new EqualsBuilder().append(folder, rhs.folder).append(controllerId, rhs.controllerId).append(orderTemplatePath, rhs.orderTemplatePath).append(recursive, rhs.recursive).isEquals();
    }

}
