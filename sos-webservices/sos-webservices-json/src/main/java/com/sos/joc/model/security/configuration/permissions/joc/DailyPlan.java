
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
public class DailyPlan {

    @JsonIgnore
    private final String prefix;
    
    /**
     * show tab, planned orders, history
     * 
     */
    @JsonProperty("view")
    @JsonPropertyDescription("show tab, planned orders, history")
    private Boolean view = false;
    /**
     * create daily plan, delete submissions
     * 
     */
    @JsonProperty("manage")
    @JsonPropertyDescription("create daily plan, delete submissions")
    private Boolean manage = false;

    public DailyPlan(String prefix) {
        this.prefix = JocPermissions.getPermissionString(prefix, "dailyplan");
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
     * show tab, planned orders, history
     * 
     */
    @JsonProperty("view")
    public Boolean getView() {
        return view;
    }

    /**
     * show tab, planned orders, history
     * 
     */
    @JsonProperty("view")
    public void setView(Boolean view) {
        this.view = view;
    }

    /**
     * create daily plan, delete submissions
     * 
     */
    @JsonProperty("manage")
    public Boolean getManage() {
        return manage;
    }

    /**
     * create daily plan, delete submissions
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
        if ((other instanceof DailyPlan) == false) {
            return false;
        }
        DailyPlan rhs = ((DailyPlan) other);
        return new EqualsBuilder().append(view, rhs.view).append(manage, rhs.manage).isEquals();
    }

}
