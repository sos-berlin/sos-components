
package com.sos.joc.model.inventory.changes.common;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * dependencies
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "created",
    "modified",
    "closed",
    "owner",
    "lastPublishedBy",
    "configurations"
})
public class Change
    extends ChangeIdentifier
{

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("created")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date created;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date modified;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("closed")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date closed;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("owner")
    private String owner;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("lastPublishedBy")
    private String lastPublishedBy;
    @JsonProperty("configurations")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ChangeItem> configurations = new LinkedHashSet<ChangeItem>();

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("created")
    public Date getCreated() {
        return created;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("created")
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public Date getModified() {
        return modified;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("modified")
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("closed")
    public Date getClosed() {
        return closed;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("closed")
    public void setClosed(Date closed) {
        this.closed = closed;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("owner")
    public String getOwner() {
        return owner;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("owner")
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("lastPublishedBy")
    public String getLastPublishedBy() {
        return lastPublishedBy;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("lastPublishedBy")
    public void setLastPublishedBy(String lastPublishedBy) {
        this.lastPublishedBy = lastPublishedBy;
    }

    @JsonProperty("configurations")
    public Set<ChangeItem> getConfigurations() {
        return configurations;
    }

    @JsonProperty("configurations")
    public void setConfigurations(Set<ChangeItem> configurations) {
        this.configurations = configurations;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("created", created).append("modified", modified).append("closed", closed).append("owner", owner).append("lastPublishedBy", lastPublishedBy).append("configurations", configurations).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(owner).append(created).append(configurations).append(modified).append(closed).append(lastPublishedBy).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Change) == false) {
            return false;
        }
        Change rhs = ((Change) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(owner, rhs.owner).append(created, rhs.created).append(configurations, rhs.configurations).append(modified, rhs.modified).append(closed, rhs.closed).append(lastPublishedBy, rhs.lastPublishedBy).isEquals();
    }

}
