
package com.sos.joc.model.security.configuration.permissions.controller;

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
public class Workflows {

    /**
     * show tab
     * 
     */
    @JsonProperty("view")
    @JsonPropertyDescription("show tab")
    private Boolean view = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Workflows() {
    }

    /**
     * 
     * @param view
     */
    public Workflows(Boolean view) {
        super();
        this.view = view;
    }

    /**
     * show tab
     * 
     */
    @JsonProperty("view")
    public Boolean getView() {
        return view;
    }

    /**
     * show tab
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
        if ((other instanceof Workflows) == false) {
            return false;
        }
        Workflows rhs = ((Workflows) other);
        return new EqualsBuilder().append(view, rhs.view).isEquals();
    }

}
