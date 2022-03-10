
package com.sos.joc.model.security.configuration.permissions.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "view",
    "manage"
})
public class Documentations {

    /**
     * show/export
     * 
     */
    @JsonProperty("view")
    @JsonPropertyDescription("show/export")
    private Boolean view = false;
    @JsonProperty("manage")
    private Boolean manage = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Documentations() {
    }

    /**
     * 
     * @param view
     * @param manage
     */
    public Documentations(Boolean view, Boolean manage) {
        super();
        this.view = view;
        this.manage = manage;
    }

    /**
     * show/export
     * 
     */
    @JsonProperty("view")
    public Boolean getView() {
        return view;
    }

    /**
     * show/export
     * 
     */
    @JsonProperty("view")
    public void setView(Boolean view) {
        this.view = view;
    }

    @JsonProperty("manage")
    public Boolean getManage() {
        return manage;
    }

    @JsonProperty("manage")
    public void setManage(Boolean manage) {
        this.manage = manage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("view", view).append("manage", manage).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(view).append(manage).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Documentations) == false) {
            return false;
        }
        Documentations rhs = ((Documentations) other);
        return new EqualsBuilder().append(view, rhs.view).append(manage, rhs.manage).isEquals();
    }

}
