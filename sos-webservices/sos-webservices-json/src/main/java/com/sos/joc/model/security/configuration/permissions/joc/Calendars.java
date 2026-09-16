
package com.sos.joc.model.security.configuration.permissions.joc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "view"
})
public class Calendars {

    @JsonIgnore
    private final String prefix;
    
    @JsonProperty("view")
    private Boolean view = false;

    public Calendars(String prefix) {
        this.prefix = JocPermissions.getPermissionString(prefix, "calendars");
    }
    
    @JsonIgnore
    public String getViewString() {
        return JocPermissions.getPermissionString(prefix, "view");
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
