
package com.sos.joc.model.security.configuration.permissions.controller;

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
    "create",
    "cancel",
    "suspendResume",
    "resumeFailed",
    "modify",
    "managePositions",
    "confirm"
})
public class Orders {

    @JsonIgnore
    private final String prefix;

    /**
     * show order/task widget, overview, order/task history
     * 
     */
    @JsonProperty("view")
    @JsonPropertyDescription("show order/task widget, overview, order/task history")
    private Boolean view = false;
    /**
     * add, generate, submit
     * 
     */
    @JsonProperty("create")
    @JsonPropertyDescription("add, generate, submit")
    private Boolean create = false;
    @JsonProperty("cancel")
    private Boolean cancel = false;
    /**
     * suspend, resume
     * 
     */
    @JsonProperty("suspendResume")
    @JsonPropertyDescription("suspend, resume")
    private Boolean suspendResume = false;
    @JsonProperty("resumeFailed")
    @JsonPropertyDescription("resume failed orders")
    private Boolean resumeFailed = false;
    @JsonProperty("modify")
    private Boolean modify = false;
    /**
     * skip, unskip, stop, unstop workflow jobs and add/modify order with special start-/endposition
     * 
     */
    @JsonProperty("managePositions")
    @JsonPropertyDescription("skip, unskip, stop, unstop workflow jobs and add/modify order with special start-/endposition")
    private Boolean managePositions = false;
    @JsonProperty("confirm")
    private Boolean confirm = false;

    public Orders(String prefix) {
        this.prefix = JocPermissions.getPermissionString(prefix, "orders");
    }
    
    @JsonIgnore
    public String getViewString() {
        return JocPermissions.getPermissionString(prefix, "view");
    }
    
    @JsonIgnore
    public String getCreateString() {
        return JocPermissions.getPermissionString(prefix, "create");
    }
    
    @JsonIgnore
    public String getCancelString() {
        return JocPermissions.getPermissionString(prefix, "cancel");
    }
    
    @JsonIgnore
    public String getSuspendResumeString() {
        return JocPermissions.getPermissionString(prefix, "suspend_resume");
    }
    
    @JsonIgnore
    public String getResumeFailedString() {
        return JocPermissions.getPermissionString(prefix, "resume_failed");
    }
    
    @JsonIgnore
    public String getModifyString() {
        return JocPermissions.getPermissionString(prefix, "modify");
    }
    
    @JsonIgnore
    public String getManagePositionsString() {
        return JocPermissions.getPermissionString(prefix, "manage_positions");
    }
    
    @JsonIgnore
    public String getConfirmString() {
        return JocPermissions.getPermissionString(prefix, "confirm");
    }

    /**
     * show order/task widget, overview, order/task history
     * 
     */
    @JsonProperty("view")
    public Boolean getView() {
        return view;
    }

    /**
     * show order/task widget, overview, order/task history
     * 
     */
    @JsonProperty("view")
    public void setView(Boolean view) {
        this.view = view;
    }

    /**
     * add, generate, submit
     * 
     */
    @JsonProperty("create")
    public Boolean getCreate() {
        return create;
    }

    /**
     * add, generate, submit
     * 
     */
    @JsonProperty("create")
    public void setCreate(Boolean create) {
        this.create = create;
    }

    @JsonProperty("cancel")
    public Boolean getCancel() {
        return cancel;
    }

    @JsonProperty("cancel")
    public void setCancel(Boolean cancel) {
        this.cancel = cancel;
    }

    /**
     * suspend, resume
     * 
     */
    @JsonProperty("suspendResume")
    public Boolean getSuspendResume() {
        return suspendResume;
    }

    /**
     * suspend, resume
     * 
     */
    @JsonProperty("suspendResume")
    public void setSuspendResume(Boolean suspendResume) {
        this.suspendResume = suspendResume;
    }
    
    @JsonProperty("resumeFailed")
    public Boolean getResumeFailed() {
        return resumeFailed;
    }

    @JsonProperty("resumeFailed")
    public void setResumeFailed(Boolean resumeFailed) {
        this.resumeFailed = resumeFailed;
    }

    @JsonProperty("modify")
    public Boolean getModify() {
        return modify;
    }

    @JsonProperty("modify")
    public void setModify(Boolean modify) {
        this.modify = modify;
    }

    /**
     * skip, unskip, stop, unstop workflow jobs and add/modify order with special start-/endposition
     * 
     */
    @JsonProperty("managePositions")
    public Boolean getManagePositions() {
        return managePositions;
    }

    /**
     * skip, unskip, stop, unstop workflow jobs and add/modify order with special start-/endposition
     * 
     */
    @JsonProperty("managePositions")
    public void setManagePositions(Boolean managePositions) {
        this.managePositions = managePositions;
    }
    
    @JsonProperty("confirm")
    public Boolean getConfirm() {
        return confirm;
    }

    @JsonProperty("confirm")
    public void setConfirm(Boolean confirm) {
        this.confirm = confirm;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("view", view).append("create", create).append("cancel", cancel).append("suspendResume", suspendResume).append("resumeFailed", resumeFailed).append("modify", modify).append("managePositions", managePositions).append("confirm", confirm).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(cancel).append(modify).append(view).append(suspendResume).append(resumeFailed).append(managePositions).append(create).append(confirm).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Orders) == false) {
            return false;
        }
        Orders rhs = ((Orders) other);
        return new EqualsBuilder().append(cancel, rhs.cancel).append(modify, rhs.modify).append(view, rhs.view).append(suspendResume, rhs.suspendResume).append(resumeFailed, rhs.resumeFailed).append(managePositions, rhs.managePositions).append(create, rhs.create).append(confirm, rhs.confirm).isEquals();
    }

}
