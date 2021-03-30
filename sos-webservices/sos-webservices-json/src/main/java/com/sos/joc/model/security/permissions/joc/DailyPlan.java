
package com.sos.joc.model.security.permissions.joc;

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
public class DailyPlan {

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

    /**
     * No args constructor for use in serialization
     * 
     */
    public DailyPlan() {
    }

    /**
     * 
     * @param view
     * @param manage
     */
    public DailyPlan(Boolean view, Boolean manage) {
        super();
        this.view = view;
        this.manage = manage;
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
