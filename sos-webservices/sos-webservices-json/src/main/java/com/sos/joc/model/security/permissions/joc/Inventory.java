
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
    "manage",
    "deploy"
})
public class Inventory {

    @JsonProperty("view")
    private Boolean view = false;
    /**
     * edit/restore/assign documentation
     * 
     */
    @JsonProperty("manage")
    @JsonPropertyDescription("edit/restore/assign documentation")
    private Boolean manage = false;
    /**
     * publishing depoyables and releasables
     * 
     */
    @JsonProperty("deploy")
    @JsonPropertyDescription("publishing depoyables and releasables")
    private Boolean deploy = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Inventory() {
    }

    /**
     * 
     * @param view
     * @param manage
     * @param deploy
     */
    public Inventory(Boolean view, Boolean manage, Boolean deploy) {
        super();
        this.view = view;
        this.manage = manage;
        this.deploy = deploy;
    }

    @JsonProperty("view")
    public Boolean getView() {
        return view;
    }

    @JsonProperty("view")
    public void setView(Boolean view) {
        this.view = view;
    }

    /**
     * edit/restore/assign documentation
     * 
     */
    @JsonProperty("manage")
    public Boolean getManage() {
        return manage;
    }

    /**
     * edit/restore/assign documentation
     * 
     */
    @JsonProperty("manage")
    public void setManage(Boolean manage) {
        this.manage = manage;
    }

    /**
     * publishing depoyables and releasables
     * 
     */
    @JsonProperty("deploy")
    public Boolean getDeploy() {
        return deploy;
    }

    /**
     * publishing depoyables and releasables
     * 
     */
    @JsonProperty("deploy")
    public void setDeploy(Boolean deploy) {
        this.deploy = deploy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("view", view).append("manage", manage).append("deploy", deploy).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(view).append(manage).append(deploy).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Inventory) == false) {
            return false;
        }
        Inventory rhs = ((Inventory) other);
        return new EqualsBuilder().append(view, rhs.view).append(manage, rhs.manage).append(deploy, rhs.deploy).isEquals();
    }

}
