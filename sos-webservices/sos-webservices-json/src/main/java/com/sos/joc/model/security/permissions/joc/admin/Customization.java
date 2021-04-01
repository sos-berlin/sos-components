
package com.sos.joc.model.security.permissions.joc.admin;

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
    "share"
})
public class Customization {

    @JsonProperty("view")
    private Boolean view = false;
    @JsonProperty("manage")
    private Boolean manage = false;
    /**
     * share/makePrvate
     * 
     */
    @JsonProperty("share")
    @JsonPropertyDescription("share/makePrvate")
    private Boolean share = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Customization() {
    }

    /**
     * 
     * @param view
     * @param share
     * @param manage
     */
    public Customization(Boolean view, Boolean manage, Boolean share) {
        super();
        this.view = view;
        this.manage = manage;
        this.share = share;
    }

    @JsonProperty("view")
    public Boolean getView() {
        return view;
    }

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

    /**
     * share/makePrvate
     * 
     */
    @JsonProperty("share")
    public Boolean getShare() {
        return share;
    }

    /**
     * share/makePrvate
     * 
     */
    @JsonProperty("share")
    public void setShare(Boolean share) {
        this.share = share;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("view", view).append("manage", manage).append("share", share).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(view).append(share).append(manage).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Customization) == false) {
            return false;
        }
        Customization rhs = ((Customization) other);
        return new EqualsBuilder().append(view, rhs.view).append(share, rhs.share).append(manage, rhs.manage).isEquals();
    }

}
