
package com.sos.joc.model.security.configuration.permissions.joc;

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
public class AuditLog {

    @JsonProperty("view")
    private Boolean view = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AuditLog() {
    }

    /**
     * 
     * @param view
     */
    public AuditLog(Boolean view) {
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
        if ((other instanceof AuditLog) == false) {
            return false;
        }
        AuditLog rhs = ((AuditLog) other);
        return new EqualsBuilder().append(view, rhs.view).isEquals();
    }

}
