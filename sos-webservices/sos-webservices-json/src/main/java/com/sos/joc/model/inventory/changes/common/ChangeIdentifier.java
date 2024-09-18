
package com.sos.joc.model.inventory.changes.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    "state"
})
public class ChangeIdentifier {

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
    private ChangeState state;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("title", title).append("state", state).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(state).append(title).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ChangeIdentifier) == false) {
            return false;
        }
        ChangeIdentifier rhs = ((ChangeIdentifier) other);
        return new EqualsBuilder().append(name, rhs.name).append(state, rhs.state).append(title, rhs.title).isEquals();
    }

}
