
package com.sos.joc.model.inventory.changes.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "name",
    "title",
    "state",
    "created",
    "modified",
    "closed",
    "owner",
    "lastPublishedBy",
    "configurations"
})
public class Change {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * state of a change
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private ChangeState state = ChangeState.fromValue(0);
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
    private List<ChangeItem> configurations = new ArrayList<ChangeItem>();

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * state of a change
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public ChangeState getState() {
        return state;
    }

    /**
     * state of a change
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(ChangeState state) {
        this.state = state;
    }

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
    public List<ChangeItem> getConfigurations() {
        return configurations;
    }

    @JsonProperty("configurations")
    public void setConfigurations(List<ChangeItem> configurations) {
        this.configurations = configurations;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("title", title).append("state", state).append("created", created).append("modified", modified).append("closed", closed).append("owner", owner).append("lastPublishedBy", lastPublishedBy).append("configurations", configurations).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(owner).append(created).append(configurations).append(name).append(modified).append(closed).append(state).append(title).append(lastPublishedBy).toHashCode();
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
        return new EqualsBuilder().append(owner, rhs.owner).append(created, rhs.created).append(configurations, rhs.configurations).append(name, rhs.name).append(modified, rhs.modified).append(closed, rhs.closed).append(state, rhs.state).append(title, rhs.title).append(lastPublishedBy, rhs.lastPublishedBy).isEquals();
    }

}
