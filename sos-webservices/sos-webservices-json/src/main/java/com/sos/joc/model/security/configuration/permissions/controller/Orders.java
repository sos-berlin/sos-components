
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
    "view",
    "create",
    "cancel",
    "suspendResume",
    "modify"
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
    @JsonProperty("modify")
    private Boolean modify = true;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Orders() {
    }

    /**
     * 
     * @param cancel
     * @param modify
     * @param view
     * @param suspendResume
     * @param create
     */
    public Orders(Boolean view, Boolean create, Boolean cancel, Boolean suspendResume, Boolean modify) {
        super();
        this.view = view;
        this.create = create;
        this.cancel = cancel;
        this.suspendResume = suspendResume;
        this.modify = modify;
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

    @JsonProperty("modify")
    public Boolean getModify() {
        return modify;
    }

    @JsonProperty("modify")
    public void setModify(Boolean modify) {
        this.modify = modify;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("view", view).append("create", create).append("cancel", cancel).append("suspendResume", suspendResume).append("modify", modify).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(cancel).append(create).append(modify).append(view).append(suspendResume).toHashCode();
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
        return new EqualsBuilder().append(cancel, rhs.cancel).append(create, rhs.create).append(modify, rhs.modify).append(view, rhs.view).append(suspendResume, rhs.suspendResume).isEquals();
    }

}
