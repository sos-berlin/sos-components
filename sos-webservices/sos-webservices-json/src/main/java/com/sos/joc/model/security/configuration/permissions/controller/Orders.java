
package com.sos.joc.model.security.configuration.permissions.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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

    /**
     * No args constructor for use in serialization
     * 
     */
    public Orders() {
    }

    /**
     * 
     * @param view
     * @param create
     * @param cancel
     * @param suspendResume
     * @param resumeFailed
     * @param modify
     * @param managePositions
     * @param confirm
     */
    public Orders(Boolean view, Boolean create, Boolean cancel, Boolean suspendResume, Boolean resumeFailed, Boolean modify, Boolean managePositions, Boolean confirm) {
        super();
        this.view = view;
        this.create = create;
        this.cancel = cancel;
        this.suspendResume = suspendResume;
        this.resumeFailed = resumeFailed;
        this.modify = modify;
        this.managePositions = managePositions;
        this.confirm = confirm;
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
