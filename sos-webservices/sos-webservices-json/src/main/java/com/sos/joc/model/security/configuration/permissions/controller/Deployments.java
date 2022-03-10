
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
    "deploy"
})
public class Deployments {

    /**
     * show deployment history
     * 
     */
    @JsonProperty("view")
    @JsonPropertyDescription("show deployment history")
    private Boolean view = false;
    /**
     * add/update/remove releasable and deployable objects
     * 
     */
    @JsonProperty("deploy")
    @JsonPropertyDescription("add/update/remove releasable and deployable objects")
    private Boolean deploy = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Deployments() {
    }

    /**
     * 
     * @param view
     * @param deploy
     */
    public Deployments(Boolean view, Boolean deploy) {
        super();
        this.view = view;
        this.deploy = deploy;
    }

    /**
     * show deployment history
     * 
     */
    @JsonProperty("view")
    public Boolean getView() {
        return view;
    }

    /**
     * show deployment history
     * 
     */
    @JsonProperty("view")
    public void setView(Boolean view) {
        this.view = view;
    }

    /**
     * add/update/remove releasable and deployable objects
     * 
     */
    @JsonProperty("deploy")
    public Boolean getDeploy() {
        return deploy;
    }

    /**
     * add/update/remove releasable and deployable objects
     * 
     */
    @JsonProperty("deploy")
    public void setDeploy(Boolean deploy) {
        this.deploy = deploy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("view", view).append("deploy", deploy).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(view).append(deploy).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Deployments) == false) {
            return false;
        }
        Deployments rhs = ((Deployments) other);
        return new EqualsBuilder().append(view, rhs.view).append(deploy, rhs.deploy).isEquals();
    }

}
