
package com.sos.joc.model.security.permissions.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "view"
})
public class Calendars {

    @JsonProperty("view")
    private Boolean view = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Calendars() {
    }

    /**
     * 
     * @param view
     */
    public Calendars(Boolean view) {
        super();
        this.view = view;
    }

    @JsonProperty("view")
    public Boolean getView() {
        return view;
    }

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
        if ((other instanceof Calendars) == false) {
            return false;
        }
        Calendars rhs = ((Calendars) other);
        return new EqualsBuilder().append(view, rhs.view).isEquals();
    }

}
