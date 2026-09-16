
package com.sos.joc.model.security.configuration.permissions.joc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "view",
    "manage"
})
public class Reports {

    @JsonIgnore
    private final String prefix;
    
    /**
     * show resource tab 'reports'
     * 
     */
    @JsonProperty("view")
    @JsonPropertyDescription("show resource tab 'reports'")
    private Boolean view = false;
    /**
     * run report
     * 
     */
    @JsonProperty("manage")
    @JsonPropertyDescription("run report")
    private Boolean manage = false;

    public Reports(String prefix) {
        this.prefix = JocPermissions.getPermissionString(prefix, "reports");
    }
    
    @JsonIgnore
    public String getViewString() {
        return JocPermissions.getPermissionString(prefix, "view");
    }
    
    @JsonIgnore
    public String getManageString() {
        return JocPermissions.getPermissionString(prefix, "manage");
    }

    /**
     * show resource tab 'reports'
     * 
     */
    @JsonProperty("view")
    public Boolean getView() {
        return view;
    }

    /**
     * show resource tab 'reports'
     * 
     */
    @JsonProperty("view")
    public void setView(Boolean view) {
        this.view = view;
    }

    /**
     * run report
     * 
     */
    @JsonProperty("manage")
    public Boolean getManage() {
        return manage;
    }

    /**
     * run report
     * 
     */
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
        if ((other instanceof Reports) == false) {
            return false;
        }
        Reports rhs = ((Reports) other);
        return new EqualsBuilder().append(view, rhs.view).append(manage, rhs.manage).isEquals();
    }

}
