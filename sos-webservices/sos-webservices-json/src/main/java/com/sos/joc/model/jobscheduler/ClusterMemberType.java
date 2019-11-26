
package com.sos.joc.model.jobscheduler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobscheduler cluster member type
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "_type",
    "precedence",
    "isActive",
    "url"
})
public class ClusterMemberType {

    /**
     * jobscheduler cluster type
     * <p>
     * Possible values are: 'standalone','active','passive'; JobScheduler doesn't run in a cluster (standalone) or is member of an active (distributed orders) or passive cluster (backup)
     * (Required)
     * 
     */
    @JsonProperty("_type")
    @JsonPropertyDescription("Possible values are: 'standalone','active','passive'; JobScheduler doesn't run in a cluster (standalone) or is member of an active (distributed orders) or passive cluster (backup)")
    private ClusterType _type;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("precedence")
    private Integer precedence;
    /**
     * (last) state of the node, ...only relevant for passive cluster
     * 
     */
    @JsonProperty("isActive")
    @JsonPropertyDescription("(last) state of the node, ...only relevant for passive cluster")
    private Boolean isActive;
    /**
     * url in cluster, used by other node for connection ...only relevant for passive cluster
     * 
     */
    @JsonProperty("url")
    @JsonPropertyDescription("url in cluster, used by other node for connection ...only relevant for passive cluster")
    private String url;

    /**
     * jobscheduler cluster type
     * <p>
     * Possible values are: 'standalone','active','passive'; JobScheduler doesn't run in a cluster (standalone) or is member of an active (distributed orders) or passive cluster (backup)
     * (Required)
     * 
     */
    @JsonProperty("_type")
    public ClusterType get_type() {
        return _type;
    }

    /**
     * jobscheduler cluster type
     * <p>
     * Possible values are: 'standalone','active','passive'; JobScheduler doesn't run in a cluster (standalone) or is member of an active (distributed orders) or passive cluster (backup)
     * (Required)
     * 
     */
    @JsonProperty("_type")
    public void set_type(ClusterType _type) {
        this._type = _type;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("precedence")
    public Integer getPrecedence() {
        return precedence;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("precedence")
    public void setPrecedence(Integer precedence) {
        this.precedence = precedence;
    }

    /**
     * (last) state of the node, ...only relevant for passive cluster
     * 
     */
    @JsonProperty("isActive")
    public Boolean getIsActive() {
        return isActive;
    }

    /**
     * (last) state of the node, ...only relevant for passive cluster
     * 
     */
    @JsonProperty("isActive")
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * url in cluster, used by other node for connection ...only relevant for passive cluster
     * 
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     * url in cluster, used by other node for connection ...only relevant for passive cluster
     * 
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("_type", _type).append("precedence", precedence).append("isActive", isActive).append("url", url).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(_type).append(isActive).append(precedence).append(url).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterMemberType) == false) {
            return false;
        }
        ClusterMemberType rhs = ((ClusterMemberType) other);
        return new EqualsBuilder().append(_type, rhs._type).append(isActive, rhs.isActive).append(precedence, rhs.precedence).append(url, rhs.url).isEquals();
    }

}
