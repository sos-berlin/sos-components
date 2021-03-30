
package com.sos.joc.model.security.permissions.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "view"
})
public class Locks {

    /**
     * show resource tab 'locks'
     * 
     */
    @JsonProperty("view")
    @JsonPropertyDescription("show resource tab 'locks'")
    private Boolean view = true;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Locks() {
    }

    /**
     * 
     * @param view
     */
    public Locks(Boolean view) {
        super();
        this.view = view;
    }

    /**
     * show resource tab 'locks'
     * 
     */
    @JsonProperty("view")
    public Boolean getView() {
        return view;
    }

    /**
     * show resource tab 'locks'
     * 
     */
    @JsonProperty("view")
    public void setView(Boolean view) {
        this.view = view;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("view", view).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(view).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Locks) == false) {
            return false;
        }
        Locks rhs = ((Locks) other);
        return new EqualsBuilder().append(view, rhs.view).isEquals();
    }

}
