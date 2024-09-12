
package com.sos.joc.model.inventory.changes;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.changes.common.ChangeState;
import com.sos.joc.model.inventory.changes.common.Timespan;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * changes
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "names",
    "state",
    "owner",
    "lastPublishedBy",
    "created",
    "modified",
    "closed"
})
public class ShowChangesFilter {

    @JsonProperty("names")
    private List<String> names = new ArrayList<String>();
    /**
     * state of a change
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private ChangeState state = ChangeState.fromValue(0);
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
    /**
     * time span to restrict the time period
     * <p>
     * 
     * 
     */
    @JsonProperty("created")
    private Timespan created;
    /**
     * time span to restrict the time period
     * <p>
     * 
     * 
     */
    @JsonProperty("modified")
    private Timespan modified;
    /**
     * time span to restrict the time period
     * <p>
     * 
     * 
     */
    @JsonProperty("closed")
    private Timespan closed;

    @JsonProperty("names")
    public List<String> getNames() {
        return names;
    }

    @JsonProperty("names")
    public void setNames(List<String> names) {
        this.names = names;
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

    /**
     * time span to restrict the time period
     * <p>
     * 
     * 
     */
    @JsonProperty("created")
    public Timespan getCreated() {
        return created;
    }

    /**
     * time span to restrict the time period
     * <p>
     * 
     * 
     */
    @JsonProperty("created")
    public void setCreated(Timespan created) {
        this.created = created;
    }

    /**
     * time span to restrict the time period
     * <p>
     * 
     * 
     */
    @JsonProperty("modified")
    public Timespan getModified() {
        return modified;
    }

    /**
     * time span to restrict the time period
     * <p>
     * 
     * 
     */
    @JsonProperty("modified")
    public void setModified(Timespan modified) {
        this.modified = modified;
    }

    /**
     * time span to restrict the time period
     * <p>
     * 
     * 
     */
    @JsonProperty("closed")
    public Timespan getClosed() {
        return closed;
    }

    /**
     * time span to restrict the time period
     * <p>
     * 
     * 
     */
    @JsonProperty("closed")
    public void setClosed(Timespan closed) {
        this.closed = closed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("names", names).append("state", state).append("owner", owner).append("lastPublishedBy", lastPublishedBy).append("created", created).append("modified", modified).append("closed", closed).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(owner).append(names).append(created).append(modified).append(closed).append(state).append(lastPublishedBy).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ShowChangesFilter) == false) {
            return false;
        }
        ShowChangesFilter rhs = ((ShowChangesFilter) other);
        return new EqualsBuilder().append(owner, rhs.owner).append(names, rhs.names).append(created, rhs.created).append(modified, rhs.modified).append(closed, rhs.closed).append(state, rhs.state).append(lastPublishedBy, rhs.lastPublishedBy).isEquals();
    }

}
